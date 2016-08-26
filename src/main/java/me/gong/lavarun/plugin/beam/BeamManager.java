package me.gong.lavarun.plugin.beam;

import com.google.common.util.concurrent.ListenableFuture;
import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.beam.actions.*;
import me.gong.lavarun.plugin.beam.oauth.AuthResponse;
import me.gong.lavarun.plugin.beam.oauth.OAuthManager;
import me.gong.lavarun.plugin.command.CommandManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.GameBeginEvent;
import me.gong.lavarun.plugin.game.events.GameEndEvent;
import me.gong.lavarun.plugin.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.json.simple.JSONObject;
import pro.beam.api.BeamAPI;
import pro.beam.api.resource.BeamUser;
import pro.beam.api.resource.channel.BeamChannel;
import pro.beam.api.services.AbstractHTTPService;
import pro.beam.api.services.impl.ChannelsService;
import pro.beam.api.services.impl.UsersService;
import pro.beam.interactive.net.packet.Protocol;
import pro.beam.interactive.robot.Robot;
import pro.beam.interactive.robot.RobotBuilder;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;


public class BeamManager implements Listener {

    public static final String LAVA_RUN_SERVICE = "https://server.savikin.me/lavarunlive.php?username=%username&port=%port&confirmation=%confirm&ip=%ip",
            SHARE_CODE = "vylpz7p7";

    private BeamConfiguration configuration;
    private List<Protocol.ProgressUpdate.TactileUpdate.Builder> tasks;
    private OAuthManager oauth;

    private List<UUID> streamers;

    private List<BeamAction> actions;
    private List<BeamState> states;
    private boolean disabling;

    public BeamManager() {
        actions = new ArrayList<>();
        streamers = new ArrayList<>();
        states = new ArrayList<>();

        tasks = new CopyOnWriteArrayList<>();

        oauth = new OAuthManager();
        oauth.onEnable(InManager.get().getInstance(Main.Config.class));

        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
        InManager.get().getInstance(CommandManager.class).addAllCommands(new BeamCmd());

        addActions(new SpleefAction(), new PoisonAction(), new ChunkDestroyAction(), new ScrambleAction(), new RestoreHungerAction());
        reloadRobot();
    }

    private void createRobot(BeamConfiguration configuration) throws Exception {

        if(disabling) return;
        if(this.configuration != null) this.configuration.robot.close();
        this.configuration = configuration;
        this.configuration.initialize();
        this.configuration.updateStream();
        this.configuration.robot.on(Protocol.Report.class, report -> {
            if(disabling || report == null) return;
            try {
                List<Protocol.Report.TactileInfo> tacs = report.getTactileList();
                Protocol.ProgressUpdate.Builder progress = Protocol.ProgressUpdate.newBuilder();

                for (Protocol.Report.TactileInfo i : tacs) {

                    Protocol.ProgressUpdate.TactileUpdate.Builder task = generateBuilder(i.getId());

                    if (getTask(i.getId()) != null) applyState(task, getTask(i.getId()));

                    BeamAction a = getActionForId(i.getId());
                    if (a == null) {

                        task.setDisabled(true);
                        if (i.getPressFrequency() > 0)
                            broadcastToStream(ChatColor.RED + "Uh oh! No action for that button!", true);
                        return;
                    }
                    BeamState s = getStateForId(a.getButtonId());

                    GameManager m = InManager.get().getInstance(GameManager.class);
                    if (!m.isInGame()) {
                        if (!s.isDisabled()) task.setDisabled(true);
                        if(s.hasCooldown()) task.setCooldown(0);

                        broadcastToStream(ChatColor.RED + "Buttons are ineffective! No game running.", true);
                    } else {
                        updateAction(progress, task, a);

                        if (i.getPressFrequency() > 0 && !s.hasCooldown()) {
                            runAction(a, i);
                            if(a.getCooldown() > 0) task.setCooldown(a.getCooldown());
                        }
                    }
                    if (hasUpdate(task)) {
                        updateButtonState(task.getId(), task.getDisabled(), task.getFired(), task.getCooldown());
                        progress.addTactile(task);
                    }
                }
                if (progress.getTactileCount() > 0 && this.configuration != null) this.configuration.robot.write(progress.build());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private Protocol.ProgressUpdate.TactileUpdate.Builder generateBuilder(int id) {
        Protocol.ProgressUpdate.TactileUpdate.Builder ret = Protocol.ProgressUpdate.TactileUpdate.newBuilder().setId(id);
        applyState(ret);
        return ret;
    }

    private void applyState(Protocol.ProgressUpdate.TactileUpdate.Builder update) {
        if (update == null) return;
        BeamState s = getStateForId(update.getId());
        update.setDisabled(s.isDisabled());
        update.setFired(s.isFired());
        if (s.hasCooldown()) {
            update.setCooldown(Math.max(0, s.getCooldown()));
        } else update.setCooldown(0);
    }

    private void applyState(Protocol.ProgressUpdate.TactileUpdate.Builder update, Protocol.ProgressUpdate.TactileUpdate.Builder from) {
        if(from == null || update == null) return;
        update.setDisabled(from.getDisabled());
        update.setFired(from.getFired());
    }

    private void resetButtons() {
        states.forEach(BeamState::reset);
    }

    public boolean isInteractiveConnected() {
        return configuration != null;
    }

    public void endInteractive() {
        if(configuration != null) {
            configuration.robot.close();
            configuration = null;
        }
    }

    public void useConfiguration(BeamConfiguration beamConfiguration) {
        if(configuration != null) configuration.robot.close();

        reloadRobot(beamConfiguration);
    }

    public boolean hasUpdate(Protocol.ProgressUpdate.TactileUpdate.Builder b) {
        BeamState s = getStateForId(b.getId());
        return b.getDisabled() != s.isDisabled() || b.getFired() != s.isFired() || (b.getCooldown() > 0) != s.hasCooldown();
    }

    public void updateButtonState(int id, boolean disabled, boolean fired, long cooldown) {
        BeamState s = getStateForId(id);
        s.setDisabled(disabled);
        s.setFired(fired);
        if(cooldown > 0) s.setCooldownEnd(cooldown);
    }

    private void updateAction(Protocol.ProgressUpdate.Builder builder, Protocol.ProgressUpdate.TactileUpdate.Builder prog, BeamAction action) {
        Protocol.ProgressUpdate.TactileUpdate.Builder progBuild = action.updateButton(prog);
        if(progBuild != null) builder.addTactile(progBuild.build());
    }

    private void runAction(BeamAction action, Protocol.Report.TactileInfo info) {
        Bukkit.getScheduler().runTask(InManager.get().getInstance(Main.class), () -> action.handlePress(info));
    }

    private void addActions(BeamAction... action) {
        Arrays.stream(action).forEach(this::addAction);
    }

    private void addAction(BeamAction action) {
        actions.add(action);
        states.add(new BeamState(action.getButtonId()));
        Bukkit.getPluginManager().registerEvents(action, InManager.get().getInstance(Main.class));
    }

    private BeamAction getActionForId(int id) {
        return actions.stream().filter(a -> a.getButtonId() == id).findFirst().orElse(null);
    }

    private BeamState getStateForId(int id) {
        return states.stream().filter(st -> st.getId() == id).findFirst().orElse(null);
    }

    private Protocol.ProgressUpdate.TactileUpdate.Builder getTask(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            Protocol.ProgressUpdate.TactileUpdate.Builder ba = tasks.get(i);
            if(ba.getId() == id) {
                tasks.remove(i);
                return ba;
            }
        }
        return null;
    }

    private void addTask(Protocol.ProgressUpdate.TactileUpdate.Builder task) {
        int id = -1;
        for (int i = 0; i < tasks.size(); i++) {
            Protocol.ProgressUpdate.TactileUpdate.Builder a = tasks.get(i);
            if(a.getId() == task.getId()) {
                id = i;
                break;
            }
        }
        if(id != -1) {
            tasks.remove(id);
            tasks.add(id, task);
        } else tasks.add(task);
    }

    public List<Player> getStreamers() {
        return Bukkit.getOnlinePlayers().stream().filter(p -> streamers.contains(p.getUniqueId())).collect(Collectors.toList());
    }

    public void broadcastToStream(String message, boolean action) {
        getStreamers().forEach(p -> {
            if(action) BukkitUtils.sendActionMessage(p, message);
            else p.sendMessage(message);
        });
    }

    public void reloadRobot(BeamConfiguration newConfig) {
        Main.Config c = InManager.get().getInstance(Main.Config.class);
        resetButtons();
        tasks.clear();
        c.reloadConfig();
        if(configuration != null || newConfig != null)
            try {
                createRobot(newConfig != null ? newConfig : configuration);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
    }

    public void reloadRobot() {
        reloadRobot(null);
    }

    public boolean toggleStreaming(Player player) {
        if(streamers.contains(player.getUniqueId())) {
            streamers.remove(player.getUniqueId());
            return false;
        } else return streamers.add(player.getUniqueId());
    }

    public OAuthManager getOAuthManager() {
        return oauth;
    }

    public void onDisable() {
        disabling = true;
        endInteractive();
        oauth.onDisable();
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        streamers.remove(ev.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBegin(GameBeginEvent ev) {
        List<Protocol.ProgressUpdate.TactileUpdate.Builder> updates = actions.stream()
                .filter(a -> getStateForId(a.getButtonId()).isDisabled())
                .map(a -> generateBuilder(a.getButtonId()).setDisabled(false))
                .collect(Collectors.toList());
        if(updates.size() > 0) updates.forEach(this::addTask);
    }

    @EventHandler
    public void onEnd(GameEndEvent ev) {
        List<Protocol.ProgressUpdate.TactileUpdate.Builder> updates = actions.stream()
                .filter(a -> !getStateForId(a.getButtonId()).isDisabled())
                .map(a -> generateBuilder(a.getButtonId()).setDisabled(true))
                .collect(Collectors.toList());
        if(updates.size() > 0) updates.forEach(this::addTask);
    }

    public class BeamState {
        private boolean disabled, fired;
        private long cooldownEnd;
        private final int id;

        public BeamState(int id) {
            this.id = id;
        }

        public boolean isDisabled() {
            return disabled;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isFired() {
            return fired;
        }

        public void setFired(boolean fired) {
            this.fired = fired;
        }

        public long getCooldownEnd() {
            return cooldownEnd;
        }

        public void setCooldownEnd(long cooldownEnd) {
            this.cooldownEnd = System.currentTimeMillis() + cooldownEnd;
        }

        public boolean hasCooldown() {
            return System.currentTimeMillis() <= cooldownEnd;
        }

        public int getId() {
            return id;
        }

        public void reset() {
            this.cooldownEnd = 0;
            this.disabled = false;
            this.fired = false;
        }

        public int getCooldown() {
            return (int) (cooldownEnd - System.currentTimeMillis());
        }
    }

    public static class BeamConfiguration {
        private BeamAPI beam;
        private Robot robot;
        private BeamUser user;
        private String token;

        public BeamConfiguration(AuthResponse response) {
            this.beam = new BeamAPI(response.token);
            try {
                token = response.token;
                user = beam.use(UsersService.class).getCurrent().get();
                initialize();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        public void initialize() {
            try {
                robot = new RobotBuilder().channel(user.channel.id).build(beam, false).get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        public void updateStream() {
            try {
                URL url = new URL("https://beam.pro/api/v1/channels/"+user.channel.id);
                HttpsURLConnection httpCon = (HttpsURLConnection) url.openConnection();
                httpCon.setDoOutput(true);
                httpCon.setDoInput(true);
                httpCon.setRequestProperty("Authorization", "Bearer "+token);
                httpCon.setRequestMethod("PUT");
                OutputStreamWriter out = new OutputStreamWriter(
                        httpCon.getOutputStream());
                JSONObject obj = new JSONObject();
                obj.put("interactiveGameId", 5209);
                obj.put("interactiveShareCode", SHARE_CODE);
                obj.put("interactive", true);
                out.write(obj.toJSONString());
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
