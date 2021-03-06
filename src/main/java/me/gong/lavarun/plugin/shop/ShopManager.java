package me.gong.lavarun.plugin.shop;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.shop.events.SetPointsEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager implements Listener {

    public static final int MAXIMUM_POINTS = 350;

    private Map<UUID, Integer> points;

    public ShopManager() {
        points = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
    }

    public void setPoints(Player player, int points) {

        SetPointsEvent ev = new SetPointsEvent(player, points);
        Bukkit.getPluginManager().callEvent(ev);
        if(!ev.isCancelled()) this.points.put(player.getUniqueId(), ev.getAmount());
    }

    public void addPoints(Player player, int amount) {
        setPoints(player, getPoints(player) + amount);
    }

    public void removePoints(Player player, int amount) {
        setPoints(player, Math.max(0, getPoints(player) - amount));
    }

    public void resetPoints(Player player) {
        setPoints(player, 0);
    }

    public void resetAllPlayers() {
        Bukkit.getOnlinePlayers().forEach(this::resetPoints);
    }

    public int getPoints(Player player) {
        return points.get(player.getUniqueId());
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(m.isInGame()) ev.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        points.put(ev.getPlayer().getUniqueId(), 0);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        points.remove(ev.getPlayer().getUniqueId());
    }
}
