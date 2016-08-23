package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.BukkitUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class WallerPowerup extends Powerup {
    @Override
    public ItemStack getItem(Team team) {
        ItemStack s = new ItemStack(Material.STAINED_GLASS);
        s.setAmount(getMaxUses());
        s.setDurability(team.getGlassColor());
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+getName());
        s.setItemMeta(m);
        return s;
    }

    @Override
    public int getMaxUses() {
        return 2;
    }

    @Override
    public String getName() {
        return "Waller";
    }

    @Override
    public int getCost() {
        return 60;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(m.isInGame() && m.getCurrentArena().isPlaying(ev.getPlayer(), false) &&
                isSelected(ev.getPlayer())) {
            if(!m.getCurrentArena().getLavaRegion().contains(ev.getBlock().getLocation())) ev.setCancelled(true);
            else {
                onUse(ev.getPlayer());
                ev.getPlayer().getInventory().getItemInMainHand().setAmount(1);
                Location begin = ev.getBlock().getLocation();


                BlockFace blockFace = fromPlayer(ev.getPlayer()), otha = blockFace.getOppositeFace();
                double ma= blockFace.getModX() != 0 ?
                        m.getCurrentArena().getPlayArea().getLargestLength() :
                        m.getCurrentArena().getPlayArea().getLargestWidth();

                AxisAlignedBB a = new AxisAlignedBB(begin.getX() + (otha.getModX() * ma), begin.getY() + 1, begin.getZ() + (otha.getModZ() * ma),
                        begin.getX() + (blockFace.getModX() * ma), begin.getY() + 20, begin.getZ() + (blockFace.getModZ() * ma));
                AtomicBoolean max = new AtomicBoolean();

                List<Location> ret = new ArrayList<>();

                x: for(double x = a.minX; x <= a.maxX; x++) {
                    z: for(double z = a.minZ; z <= a.maxZ; z++) {
                        for(double y = a.minY; y <= a.maxY; y++) {
                            Location l = new Location(ev.getPlayer().getWorld(), x, y, z);
                            if(l.getBlock().getType() != Material.AIR) {
                                if(blockFace.getModX() != 0) continue x;
                                else continue z;
                            }
                            ret.add(l);
                        }
                    }
                }

                final ItemStack s = new ItemStack(Material.STAINED_GLASS, 1, m.getCurrentArena().getTeam(ev.getPlayer()).getGlassColor());

                List<Location> allUsable = ret.stream().filter(l -> {
                    if (max.get()) return false;

                    if (blockFace.getModX() != 0) {
                        int useX = 0;

                        if ((begin.getBlockX() % 2) == 0) useX = 1;
                        if ((((l.getBlockX() + useX) % 2)) == ((l.getBlockY() % 2))) return false;
                    } else {
                        int useZ = 0;

                        if ((begin.getBlockZ() % 2) != 0) useZ = 1;
                        if ((((l.getBlockZ() - useZ) % 2)) != -((l.getBlockY() % 2))) return false;
                    }
                    if (!m.getCurrentArena().isBlockInteractable(l, s, ev.getPlayer(), false)) return false;

                    if (l.getBlock().getType() != Material.AIR && l.getBlockX() == begin.getBlockX() && l.getBlockZ() == begin.getBlockZ()) {
                        max.set(true);
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
                Collections.shuffle(allUsable);
                ev.getPlayer().getInventory().setItem(3, s);
                final int totalPercent = 26; //26% off
                int use = 100 - totalPercent;
                for(int i = 0; i < allUsable.size(); i++) {
                    double percent = (Math.max(i, 1) * 1.0 / allUsable.size() * 1.0) * 100;

                    if(percent <= use) setBlock(ev.getPlayer(), allUsable.get(i));
                }
            }
        }
    }

    private void setBlock(Player player, Location l) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        l.getBlock().setTypeIdAndData(Material.STAINED_GLASS.getId(), (byte) m.getCurrentArena().getTeam(player).getGlassColor(), true);
        l.clone().getWorld().spawnParticle(Particle.BARRIER, l, 1, 0.5, 0, 0.5, 0.0);
    }

    private BlockFace fromPlayer(Player player) {
        BlockFace r = BukkitUtils.yawToFace(player.getLocation().getYaw(), false);
        if(r.getModZ() != 0) return BlockFace.EAST;
        else return BlockFace.NORTH;
    }
}
