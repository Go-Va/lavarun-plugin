package me.gong.lavarun.plugin.powerup;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.impl.*;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timers;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class PowerupManager {

    private static final long MIN_TIME_BETWEEN_POWERUPS = 1000 * 8;

    private List<Powerup> powerups;
    private Map<UUID, Long> lastPowerup;

    public PowerupManager() {
        powerups = new ArrayList<>();
        lastPowerup = new HashMap<>();
        Class<?>[] powerups = new Class[] {KnockerPowerup.class, SabotagePowerup.class,
                InstaKillPowerup.class, WallerPowerup.class, ResupplyPowerup.class,
                PathEmUpPowerup.class, ExplodeePowerup.class, FreezerPowerup.class}; //patheeeeee
        Arrays.stream(powerups).forEach(p -> {
            try {
                addPowerup((Powerup) p.newInstance());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        new BukkitRunnable() {
            @Override
            public void run() {
                GameManager m = InManager.get().getInstance(GameManager.class);
                List<UUID> tR = lastPowerup.keySet().stream()
                        .filter(k -> System.currentTimeMillis() - lastPowerup.get(k) >= MIN_TIME_BETWEEN_POWERUPS)
                        .collect(Collectors.toList());
                tR.forEach(lastPowerup::remove);
                if(m.isInGame()) {
                    for(Player p : m.getCurrentArena().getPlaying(true)) {
                        if(p.getLevel() < 100) InManager.get().getInstance(ShopManager.class).addPoints(p, 1);
                    }
                }
            }
        }.runTaskTimer(InManager.get().getInstance(Main.class), 0, 20 * 2);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        lastPowerup.remove(ev.getPlayer().getUniqueId());
    }

    private void addPowerup(Powerup p) {
        powerups.add(p);
        Timers.register(p);
        Bukkit.getPluginManager().registerEvents(p, InManager.get().getInstance(Main.class));
    }

    public void onGameEnd() {
        powerups.forEach(Powerup::unload);
    }

    public void givePlayerPowerup(Player player, Powerup give) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 0.0f);
        lastPowerup.put(player.getUniqueId(), System.currentTimeMillis());
        player.getInventory().setItem(1, give.getItem(InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player)));
    }

    public Powerup getActivePowerup(Player player) {
        return powerups.stream().filter(p -> p.isSelected(player)).findFirst().orElse(null);
    }

    public Powerup getPowerup(String name) {
        return powerups.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Powerup> getPowerups() {
        return powerups;
    }
}
