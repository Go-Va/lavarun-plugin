package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.LocationUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import me.gong.lavarun.plugin.util.TimeUtils;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExplodeePowerup extends Powerup {

    private Map<ItemData, Long> trackedItems = new ConcurrentHashMap<>();
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
        return 80;
    }

    @Override
    public String[] getHelp() {
        return new String[] {"When right clicked, throws a powerful bomb",
                "If the bomb hits lava, it will instantly explode",
                "If it lands on a block, it explodes in &e"+ TimeUtils.convertToString(EXPLODE_AFTER)};
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
        ItemData d = getDataFor(ev.getEntity().getUniqueId());
        if(d != null) fireOff(d);
    }

    @Timer(runEvery = 1)
    public void fireEmOff() {
        trackedItems.forEach((id, time) -> {
            if(System.currentTimeMillis() - time >= EXPLODE_AFTER) {
                trackedItems.remove(id);
                Entity e = getEntityFor(id.itemId);
                if(e != null) {
                    fireOff(id);
                }
            } else {
                long num = EXPLODE_AFTER - (System.currentTimeMillis() - time);
                if(num % 1000 <= 500 + ((500 / num) * num)) {

                    Entity e = getEntityFor(id.itemId);

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

        trackedItems.put(new ItemData(e.getUniqueId(), player.getUniqueId()), System.currentTimeMillis());
        lastWorld = e.getWorld().getName();
    }



    public void fireOff(ItemData data) {
        Entity e = getEntityFor(data.itemId);
        if(e != null) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            e.remove();
            e.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, e.getLocation().add(0, 2, 0), 1, 0.0f, 0.0f, 0.0f, 0.0f);
            e.getWorld().playSound(e.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2.0f, 0.0f);
            List<UUID> attacked = new ArrayList<>();
            NumberUtils.getSphere(e.getLocation(), 5).forEach(bl -> {
                if(gm.getCurrentArena().getPlayArea().contains(bl) || gm.getCurrentArena().getLavaRegion().contains(bl)) removeBlock(data.playerId, bl);
                Player attack = Bukkit.getPlayer(data.playerId);
                e.getWorld().getPlayers().stream().filter(pl -> LocationUtils.toBounding(pl)

                        .intersectsWith(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(bl.getX(), bl.getY(), bl.getZ())))
                        .filter(b -> gm.getCurrentArena().isPlaying(b, true))
                        .forEach(b -> {
                            if(attacked.contains(b.getUniqueId()) || (attack == null || !gm.getCurrentArena().canBeDamaged(b, attack))) return;
                            attacked.add(b.getUniqueId());

                            gm.handleAttack(b, attack);
                            b.setVelocity(b.getVelocity().add(b.getLocation().subtract(e.getLocation()).toVector().normalize()).normalize().multiply(1.2));
                            gm.attackPlayer(b, Math.max(0, 10 - ((int) b.getLocation().distance(e.getLocation()))), attack);
                        });
            });
        }
    }

    public ItemData getDataFor(UUID itemId) {
        return trackedItems.keySet().stream().filter(k -> k.itemId.equals(itemId)).findFirst().orElse(null);
    }

    private void removeBlock(UUID by, Location location) {
        Block b = location.getBlock();
        GameManager gm = InManager.get().getInstance(GameManager.class);
        int origin = b.getTypeId(), data = b.getData();
        if(!gm.handleBreak(Bukkit.getPlayer(by), false, location)) return;
        location.getWorld().playEffect(location, Effect.STEP_SOUND, origin, data);
    }

    @Override
    public void reset() {
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

    public class ItemData {
        private UUID itemId, playerId;

        public ItemData(UUID itemId, UUID playerId) {
            this.itemId = itemId;
            this.playerId = playerId;
        }
    }
}
