package me.gong.lavarun.plugin.beam;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.beam.actions.*;
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
import pro.beam.api.BeamAPI;
import pro.beam.interactive.net.packet.Protocol;
import pro.beam.interactive.robot.Robot;
import pro.beam.interactive.robot.RobotBuilder;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


public class BeamManager implements Listener {

    private BeamAPI api;
    private Robot robot;
    private List<Protocol.ProgressUpdate.TactileUpdate.Builder> tasks;
    private List<UUID> streamers;
    private List<BeamAction> actions;
    private List<BeamState> states;
    private boolean disabling;

    public BeamManager() {
        api = new BeamAPI();
        actions = new ArrayList<>();
        streamers = new ArrayList<>();
        tasks = new CopyOnWriteArrayList<>();
        states = new ArrayList<>();

        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
        InManager.get().getInstance(CommandManager.class).addAllCommands(new BeamCmd());

        addActions(new SpleefAction(), new PoisonAction(), new ChunkDestroyAction(), new ScrambleAction(), new RestoreHungerAction());
        reloadRobot();
    }

    private void createRobot(Main.Config config) throws Exception {

        if(disabling) return;
        if(robot != null) {
            robot.close();
            robot = null;
        }
        robot = new RobotBuilder().username(config.getUsername()).password(config.getPassword()).channel(config.getChannel()).build(api).get();
        robot.on(Protocol.Report.class, report -> {
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
                if (progress.getTactileCount() > 0 && robot != null) robot.write(progress.build());
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

    public void reloadRobot() {
        Main.Config c = InManager.get().getInstance(Main.Config.class);
        resetButtons();
        tasks.clear();
        c.reloadConfig();
        try {
            createRobot(c);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public boolean toggleStreaming(Player player) {
        if(streamers.contains(player.getUniqueId())) {
            streamers.remove(player.getUniqueId());
            return false;
        } else return streamers.add(player.getUniqueId());
    }

    public void onDisable() {
        disabling = true;
        if(robot != null) robot.close();
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

    public class BeamConfiguration {
        private int channelId;
        private String oauth;

        public BeamConfiguration(int channelId, String oauth) {
            this.channelId = channelId;
            this.oauth = oauth;
        }
    }
}
