package me.gong.lavarun.plugin.game.logic;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.*;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;

public class GameTicks {
    @Timer(runEvery = 20 * 30)
    public void foodSpawnTick() {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            Arena currentArena = gm.getCurrentArena();
            Location l = currentArena.getFoodSpawn();
            if(l != null) {

                Material m = null;
                for (int i = 0; i < currentArena.getFoodSpawns().size(); i++) {
                    if (currentArena.getFoodSpawns().get(i).equals(l)) m = gm.getSpawnFoods().get(Math.min(gm.getSpawnFoods().size() - 1, i));
                }
                if(m == null) m = gm.getSpawnFoods().get(0);

                Item e = l.getWorld().dropItem(l.clone().add(0.5, 0, 0.5), new ItemStack(m));
                e.setVelocity(new Vector(0, 0.5, 0));
            }
        }
    }

    @Timer(runEvery = 20)
    public void gameTick() {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(gm.isInGame() && currentArena != null) {

            for(Player p : currentArena.getPlaying(true)) p.setLevel(InManager.get().getInstance(ShopManager.class).getPoints(p));

            //messy cap logic
            Collection<Player> redCapurers = currentArena.getRed().getCapturers(), blueCapurers = currentArena.getBlue().getCapturers();
            if(redCapurers.isEmpty()) gm.redCaptureState = 0;
            else gm.redCaptureState++;
            if(blueCapurers.isEmpty()) gm.blueCaptureState = 0;
            else gm.blueCaptureState++;

            Collection<Player> toUse = null;
            if(gm.redCaptureState > 0) {
                toUse = redCapurers;
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !currentArena.isPlaying(p, true) || currentArena.getTeam(p).getName().equalsIgnoreCase("Red"))
                        .forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 0.4f * gm.redCaptureState));
            }
            if(gm.blueCaptureState > 0) {
                toUse = blueCapurers;
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> !currentArena.isPlaying(p, true) || currentArena.getTeam(p).getName().equalsIgnoreCase("Blue"))
                        .forEach(p -> p.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 0.4f * gm.blueCaptureState));
            }

            if(gm.redCaptureState > 5) gm.redCapAmount++;
            if(gm.blueCaptureState > 5) gm.blueCapAmount++;

            if(gm.redCaptureState > 5 || gm.blueCaptureState > 5) {
                currentArena.resetArena(false, false);
                if(toUse != null) {
                    toUse.forEach(p  -> {
                        gm.spawnPlayer(p, false);
                        p.getInventory().clear();
                    });
                    Bukkit.getScheduler().runTask(InManager.get().getInstance(Main.class), () -> {
                        String capturer = gm.redCaptureState > 5 ? "Red" : "Blue", loser = gm.redCaptureState > 5 ? "Blue" : "Red";
                        Bukkit.getOnlinePlayers().stream()
                                .filter(p -> !currentArena.isPlaying(p, true) || currentArena.getTeam(p).getName().equalsIgnoreCase(capturer))
                                .forEach(p -> {
                                    p.playSound(p.getLocation(), Sound.ENTITY_WOLF_AMBIENT, 2.0f, 1.75f);
                                });

                        Bukkit.getOnlinePlayers().stream()
                                .filter(p -> currentArena.getTeam(p).getName().equalsIgnoreCase(loser))
                                .forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_ENDERDRAGON_GROWL, 2.0f, 0.6f));
                    });
                }
                if(gm.redCaptureState > 0) gm.setLastRedCap();
                else gm.setLastBlueCap();
                gm.redCaptureState = gm.blueCaptureState = 0;

                Bukkit.broadcastMessage(ChatColor.YELLOW+"Arena reset due to capture.");
            }
            if(gm.redCapAmount >= 3 || gm.blueCapAmount >= 3) {

                boolean red;
                if(gm.redCapAmount >= 3 && gm.blueCapAmount >= 3) {
                    red = NumberUtils.random.nextBoolean();
                } else red = gm.redCapAmount >= 3;
                if(!red) gm.stopGame(currentArena.getBlue());
                else gm.stopGame(currentArena.getRed());
            }
        }
    }

    @Timer(runEvery = 1)
    public void respawnTick() {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(!gm.isInGame()) return;

        for(GameManager.RespawnData d : gm.getCurrentArena().getRespawnData()) {
            if(d.getPlayer() == null) gm.getCurrentArena().removeRespawnData(d.getUUID());
            else if(d.shouldRespawn()) {
                gm.getCurrentArena().removeRespawnData(d.getUUID());
                gm.spawnPlayer(d.getPlayer(), false);
                BukkitUtils.Title t = new BukkitUtils.Title("&a&lYou have respawned", true, 0, 15, 1);
                t.sendTo(d.getPlayer());
            } else gm.sendRespawnTitleTo(d.getPlayer());
        }
    }
}
