package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.LocationUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExplodeePowerup extends Powerup {

    private Map<UUID, Long> trackedItems = new ConcurrentHashMap<>();
    private String lastWorld;

    private static final long EXPLODE_AFTER = 5000;

    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.TNT);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.RED+getName());
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        m.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public String getName() {
        return "Explodee";
    }


    @Override
    public int getCost() {
        return 40;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if (ev.getAction().name().contains("RIGHT") && m.getCurrentArena() != null && m.isInGame() && isSelected(ev.getPlayer())) {
            ev.setCancelled(true);
            onUse(ev.getPlayer());
            createAndThrow(ev.getPlayer());
        }
    }

    @EventHandler
    public void onDie(EntityDamageEvent ev) {
        if(trackedItems.containsKey(ev.getEntity().getUniqueId())) fireOff(ev.getEntity().getUniqueId());
    }

    @Timer(runEvery = 1)
    public void fireEmOff() {
        trackedItems.forEach((id, time) -> {
            if(System.currentTimeMillis() - time >= EXPLODE_AFTER) {
                trackedItems.remove(id);
                Entity e = getEntityFor(id);
                if(e != null) {
                    fireOff(e.getUniqueId());
                }
            } else {
                long num = EXPLODE_AFTER - (System.currentTimeMillis() - time);
                if(num % 1000 <= 500 + ((500 / num) * num)) {

                    Entity e = getEntityFor(id);

                    if(e != null) {
                        e.getWorld().playSound(e.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 2.0f);
                        Particle b;
                        if(NumberUtils.random.nextBoolean()) {
                            b = Particle.SMOKE_NORMAL;
                        } else b = Particle.LAVA;

                        e.getWorld().spawnParticle(b, e.getLocation(), 1 + (int)((3d / EXPLODE_AFTER) * num), 0.0f, (2.0 / EXPLODE_AFTER) * num, 0.0f, 0.0f);
                    }
                }
            }
        });
    }

    public void createAndThrow(Player player) {
        ItemStack item = new ItemStack(Material.TNT, 0);
        {
            ItemMeta m = item.getItemMeta();
            m.setLore(Arrays.stream(new String[]{UUID.randomUUID().toString()}).collect(Collectors.toList()));
            item.setItemMeta(m);
        }
        Item e = player.getWorld().dropItem(player.getLocation(), item);
        e.setVelocity(NumberUtils.getVectorForPlayer(player).multiply(0.85).add(new Vector(0, 0.43, 0)));
        e.setPickupDelay(200);

        trackedItems.put(e.getUniqueId(), System.currentTimeMillis());
        lastWorld = e.getWorld().getName();
    }



    public void fireOff(UUID uuid) {
        Entity e = getEntityFor(uuid);
        if(e != null) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            e.remove();
            e.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, e.getLocation().add(0, 2, 0), 1, 0.0f, 0.0f, 0.0f, 0.0f);
            e.getWorld().playSound(e.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2.0f, 0.0f);
            NumberUtils.getSphere(e.getLocation(), 6).forEach(bl -> {
                if(gm.getCurrentArena().getPlayArea().contains(bl) || gm.getCurrentArena().getLavaRegion().contains(bl)) removeBlock(bl);
                e.getWorld().getPlayers().stream().filter(pl -> LocationUtils.toBounding(pl)
                        .intersectsWith(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(bl.getX(), bl.getY(), bl.getZ())))
                        .filter(b -> gm.getCurrentArena().isPlaying(b, true))
                        .forEach(b -> {
                            b.setVelocity(b.getVelocity().add(b.getLocation().subtract(e.getLocation()).toVector().normalize()).normalize().multiply(1.2));
                            b.damage((1 / 5) * b.getLocation().distance(e.getLocation()));
                        });
            });
        }
    }



    private void removeBlock(Location location) {
        Block b = location.getBlock();
        GameManager gm = InManager.get().getInstance(GameManager.class);
        int origin = b.getTypeId(), data = b.getData();
        if(b.getType() != Material.STAINED_GLASS) return;
        if(gm.getCurrentArena().getLavaRegion().contains(location)) b.setType(Material.LAVA);
        else b.setType(Material.AIR);
        location.getWorld().playEffect(location, Effect.STEP_SOUND, origin, data);
    }

    @Override
    public void unload() {
        if(lastWorld == null) return;
        World d = Bukkit.getWorld(lastWorld);
        if(d != null)
            trackedItems.keySet().forEach(sl -> d.getEntities().stream().filter(e -> e.getUniqueId().equals(sl)).forEach(Entity::remove));
        trackedItems.clear();
        lastWorld = null;
    }

    public Entity getEntityFor(UUID uuid) {
        if(lastWorld == null) return null;
        World d = Bukkit.getWorld(lastWorld);
        if(d != null)
        return d.getEntities().stream().filter(e -> e.getUniqueId().equals(uuid)).findFirst().orElse(null);
        else return null;
    }
}
