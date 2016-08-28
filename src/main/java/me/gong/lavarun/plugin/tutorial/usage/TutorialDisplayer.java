package me.gong.lavarun.plugin.tutorial.usage;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.timer.Timers;
import me.gong.lavarun.plugin.tutorial.data.Tutorial;
import me.gong.lavarun.plugin.tutorial.data.TutorialPoint;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.List;
import java.util.UUID;

public class TutorialDisplayer implements Listener {
    private UUID to;
    private Location begin;

    private Tutorial displaying;
    private final TutorialListener listener;
    private TutorialPoint at;
    private int displayIndex;
    private List<Timers.TimerObject> timerObjects;

    public TutorialDisplayer(Player player, Tutorial displaying, TutorialListener listener) {
        this.to = player.getUniqueId();
        this.begin = player.getLocation();
        this.displaying = displaying.copy();
        this.listener = listener;
        player.setGameMode(GameMode.SPECTATOR);
        register();
    }

    public void register() {
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
        this.timerObjects = Timers.register(this);
    }

    public void unregister() {
        HandlerList.unregisterAll(this);
        InManager.get().getInstance(Timers.class).unregister(timerObjects);
        timerObjects.clear();
        listener.onFinish(to);
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(to);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(to)) ev.setCancelled(true);
    }

    @Timer(runEvery = 1)
    public void tick() {
        Player p = getPlayer();
        if(p == null) unregister();
        else {
            if(at == null) {
                display(p);
            } else if(!at.isBeingShown()) {
                if(displayIndex >= displaying.getPoints().size() - 1) {
                    unregister();
                    p.setGameMode(GameMode.SURVIVAL);
                    p.teleport(begin);
                } else {
                    displayIndex++;
                    display(p);
                }
            }
        }
    }

    public void display(Player p) {
        at = displaying.getPoints().get(displayIndex);
        at.showTo(p);
    }
}
