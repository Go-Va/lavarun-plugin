package me.gong.lavarun.plugin.game.logic;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.PreventBreakEvent;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PointsMinerHandler implements Listener {

    public static final int ADDED_POINTS = 45;

    private Block currentBlock;
    private long lastSpawn;

    @Timer(runEvery = 20)
    public void createBlock() {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(getCurrentBlock() != null)
            currentBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, currentBlock.getLocation().add(0.5, 0.5, 0.5), 6, 0.5, 0.5, 0.5, 0.3);
        if(currentBlock == null && gm.isInGame() && System.currentTimeMillis() - lastSpawn > 1000 * 3) {
            Arena c = gm.getCurrentArena();
            World w = c.getPlayArea().getWorld();
            Location min = c.getLavaRegion().getBoxes().get(0).getMinimum(w).clone(),
                    max = c.getPlayArea().getBoxes().get(0).getMaximum(w);
            Region spawnable = new Region(new AxisAlignedBB(min, max), w.getName());
            Region outer = new Region(new AxisAlignedBB(min.clone().add(-1, -1, -1), max.clone().add(1, 1, 1)), w.getName());
            Predicate<Location> availablePred = l -> {

                int maxY = -1;
                for(int at = min.getBlockY(); at <= max.getBlockY(); at++) {
                    if(w.getBlockAt(l.getBlockX(), at, l.getBlockZ()).getType() == Material.BARRIER) {
                        maxY = at;
                        break;
                    }
                }
                return (l.getBlock().getType() == Material.AIR || l.getBlock().getType() == Material.LAVA) && l.getY() <= maxY;
            };
            List<Location> all = spawnable.getBlocks();
            Collections.shuffle(all, new Random());

            List<Location> available = all.stream().filter(availablePred).collect(Collectors.toList()),
                    unavailable = all.stream().filter(l -> l.getBlock().getType() == Material.STAINED_GLASS).collect(Collectors.toList());
            HashMap<Location, Long> values = new HashMap<>();
            available.forEach(l -> {
                long ret = 0;
                for (Location aa : unavailable) {
                    Location availableLoc = l.clone(), nonAvailable = aa.clone();
                    double dist = nonAvailable.distanceSquared(availableLoc) - (nonAvailable.distanceSquared(availableLoc) / 2);
                    for(Player p : c.getPlaying(false)) dist += p.getLocation().distanceSquared(availableLoc);
                    ret += dist;
                }
                for(Location outter : outer.getBlocks()) {
                    if(outter.getX() == min.getX() || outter.getX() == max.getX() ||
                            outter.getY() == min.getY() || outter.getY() == max.getY() ||
                            outter.getZ() == min.getZ() || outter.getZ() == max.getZ()) {
                        ret += l.distanceSquared(outter);
                    }
                }
                values.put(l, ret);
            });
            List<Location> spawnables = values.entrySet().stream()
                    .sorted((v1, v2) -> -Long.compare(v1.getValue(), v2.getValue()))
                    .map(Map.Entry::getKey).collect(Collectors.toList()), use = new ArrayList<>();


            final int totalPercent = 10;
            for(int i = 0; i < spawnables.size(); i++) {
                double percent = ((i + 1) * 1.0 / spawnables.size() * 1.0) * 100;

                if(percent <= totalPercent) use.add(spawnables.get(i));
            }
            lastSpawn = System.currentTimeMillis();
            currentBlock = spawnables.get(new Random().nextInt(use.size())).getBlock();
            currentBlock.setType(Material.LEAVES);
        }
    }

    @EventHandler
    public void onBreak(PreventBreakEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);

        if(getCurrentBlock() != null && gm.isInGame() && ev.getBlock().getLocation().equals(currentBlock.getLocation())) {
            ev.setCancelled(true);
            if(!ev.isBreak()) return;

            ShopManager m = InManager.get().getInstance(ShopManager.class);
            int points = Math.min(ShopManager.MAXIMUM_POINTS, m.getPoints(ev.getPlayer()) + ADDED_POINTS);
            m.setPoints(ev.getPlayer(), points);
            ev.getPlayer().playSound(ev.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_YES, 2.0f, 1.0f);
            Bukkit.getScheduler().runTask(InManager.get().getInstance(Main.class), this::resetBlock);
        }
    }

    public void resetBlock() {
        if(getCurrentBlock() != null) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            gm.handleBreak(null, false, currentBlock.getLocation());
        }
    }

    public Block getCurrentBlock() {
        if(currentBlock == null) return null;
        if(currentBlock.getType() != Material.LEAVES) currentBlock = null;
        return currentBlock;
    }
}
