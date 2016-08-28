package me.gong.lavarun.plugin.tutorial.usage.builder;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.ArenaCreator;
import me.gong.lavarun.plugin.tutorial.data.Tutorial;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TutorialCreator implements Listener {

    private TutorialBuilder builder;
    private UUID player;
    private List<Stage> stages;
    private CreationListener listener;
    private int stageIndex;

    private TutorialCreator(CreationListener listener, TutorialBuilder builder, Player player) {
        this.builder = builder;
        this.player = player.getUniqueId();
        this.listener = listener;
        generateStages();
        player.sendMessage(ChatColor.GOLD+"Began tutorial setup. Type "+ChatColor.GREEN+":set"+ChatColor.GOLD+" to set location.");
        player.sendMessage(ChatColor.GOLD+"To exit creation, type "+ChatColor.GREEN+":cancel");
        player.sendMessage(ChatColor.GREEN+stages.get(stageIndex).getInstruction());
    }

    public static TutorialCreator beginPlayer(Player player, TutorialBuilder builder, CreationListener listener) {
        TutorialCreator ret = new TutorialCreator(listener, builder, player);
        Bukkit.getPluginManager().registerEvents(ret, InManager.get().getInstance(Main.class));
        return ret;
    }

    public void handleCreate(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f);
        Stage next = stageIndex != stages.size() - 1 ? stages.get(1 + stageIndex) : null;
        Stage stage = stages.get(stageIndex);

        stage.getCreateListener().onLocation(player.getLocation());

        String msg = ChatColor.GOLD.toString() + stage.getFinish();
        if(next != null) msg += (ChatColor.stripColor(msg).isEmpty() ? "" : " ")+ChatColor.GREEN+next.getInstruction();
        player.sendMessage(msg);

        if(next != null) {
            stageIndex++;
            next.onSet();
        } else create();

    }

    private void reset() {
        HandlerList.unregisterAll(this);
    }

    public void cancel(boolean message) {
        reset();

        if(message && getPlayer() != null) getPlayer().sendMessage(ChatColor.GREEN+"Canceled tutorial creation");
        listener.onFail();
    }

    private void create() {
        reset();
        listener.onCreation(builder.build());
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(player)) {
                ev.setCancelled(true);
                if (ev.getMessage().equalsIgnoreCase(":cancel")) cancel(true);
                else if(ev.getMessage().equalsIgnoreCase(":set")) {
                    Bukkit.getScheduler().runTask(InManager.get().getInstance(Main.class), () -> handleCreate(ev.getPlayer()));
                } else ev.getPlayer().sendMessage(ChatColor.GOLD + "You are currently setting up a tutorial. Type ':cancel' to exit.");
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(player)) {
            ev.setCancelled(true);
            ev.getPlayer().sendMessage(ChatColor.GOLD+"You are currently setting up a tutorial. Type ':cancel' to exit.");
        }
    }

    private void generateStages() {

        this.stages = builder.getElements().stream()
                .filter(TutorialElement::useLocation)
                .map(e -> new Stage(e.getInstruction(), e.getFinishMessage(), l -> builder.progress(l)))
                .collect(Collectors.toList());
    }

    private class Stage {
        private String instruction, finish;
        private CreateLocationListener createListener;
        private SetListener setListener;

        public Stage(String instruction, String finish, CreateLocationListener cL, SetListener setListener) {
            this.instruction = instruction;
            this.finish = finish;
            this.createListener = cL;
            this.setListener = setListener;
        }

        public Stage(String instruction, String finish, CreateLocationListener cL) {
            this(instruction, finish, cL, null);
        }

        public String getInstruction() {
            return instruction;
        }

        public String getFinish() {
            return finish;
        }

        public CreateLocationListener getCreateListener() {
            return createListener;
        }

        public void onSet() {
            if(setListener != null) setListener.onSet();
        }
    }


    public interface SetListener {
        void onSet();
    }
    public interface CreateLocationListener {
        void onLocation(Location location);
    }

    public interface CreationListener {
        void onCreation(Tutorial area);

        void onFail();
    }
}
