package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.BlockRemovedEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.LocationUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class LandminePowerup extends Powerup {

    private List<LandmineData> lmD = new CopyOnWriteArrayList<>();

    @Override
    public ItemStack getItem(Team team) {
        ItemStack s = new ItemStack(Material.TRAP_DOOR);
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
        return "Freezer";
    }

    @Override
    public int getCost() {
        return 40;
    }

    @Override
    public void reset() {
        lmD.forEach(LandmineData::despawnEntity);
        lmD.clear();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(m.isInGame() && m.getCurrentArena() != null && ev.getAction() == Action.RIGHT_CLICK_BLOCK && isSelected(ev.getPlayer())) {
            if(getBlockForLocation(ev.getClickedBlock().getLocation()) != null) ev.getPlayer().sendMessage(ChatColor.RED+"That block is already trapped.");
            else if(canBePlaced(ev.getClickedBlock().getLocation())){
                onUse(ev.getPlayer());
                lmD.add(new LandmineData(ev.getPlayer(), m.getCurrentArena().getTeam(ev.getPlayer()).getName(), ev.getClickedBlock().getLocation()));
            } else ev.getPlayer().sendMessage(ChatColor.RED+"Cannot place landmine here");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent ev) {
        handleBreak(ev.getBlock());
    }

    @EventHandler
    public void onRemote(BlockRemovedEvent ev) {
        handleBreak(ev.getBlock());
    }

    public void handleBreak(Block block) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(!m.isInGame()) return;
        LandmineData d = getBlockForLocation(block.getLocation());
        if(d != null) lmD.remove(d.explode());
    }

    @EventHandler
    public void onWalk(PlayerMoveEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(!m.isInGame()) return;
        lmD.stream().filter(l -> l.isStandingOn(ev.getPlayer())).forEach(l -> lmD.remove(l.explode()));
    }

    private boolean canBePlaced(Location location) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        Arena a = m.getCurrentArena();
        Location l1 = location.clone().add(0, 1, 0), l2 = l1.clone().add(0, 1, 0);
        return (a.getPlayArea().contains(location) || a.getLavaRegion().contains(location)) &&
                location.getBlock().getType() == Material.STAINED_GLASS &&
                l1.getBlock().getType() == Material.AIR && l2.getBlock().getType() == Material.AIR;
    }

    public LandmineData getBlockForLocation(Location location) {
        return lmD.stream().filter(l -> l.blockPos.equals(location)).findFirst().orElse(null);
    }

    public class LandmineData {
        private String teamOwner;
        private Location location, blockPos;
        private UUID entity, placer;

        public LandmineData(Player placer, String team, Location at) {
            this.blockPos = at.getBlock().getLocation();
            this.placer = placer.getUniqueId();
            this.location = new Location(at.getWorld(), at.getBlockX() + 0.5, at.getBlockY() - 1.25, at.getBlockZ() + 0.5);
            this.teamOwner = team;

            spawnEntity();
        }

        private void spawnEntity() {
            if(entity != null) despawnEntity();
            ArmorStand e = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
            e.setMarker(true);
            e.setGravity(false);
            Team team = InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(teamOwner);
            e.setHelmet(team.getBaseGlass());
            e.setVisible(false);
            entity = e.getUniqueId();
        }

        private void despawnEntity() {
            Entity en = entity == null ? null :
                    location.getWorld().getEntities().stream().filter(e -> e.getUniqueId().equals(entity)).findFirst().orElse(null);
            if(en != null) {
                en.remove();
                entity = null;
            }
        }

        public boolean isStandingOn(Player player) {
            if(!canActivate(player)) return false;
            Location raw = player.getLocation().getBlock().getLocation();
            return raw.getX() == blockPos.getX() && raw.getY() == blockPos.getY() + 1 && raw.getZ() == blockPos.getZ();
        }

        public boolean canActivate(Player player) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Arena a = gm.getCurrentArena();
            Team t = a.getTeam(player);
            return a.isPlaying(player, false) && (player.getUniqueId().equals(placer) || !t.getName().equalsIgnoreCase(teamOwner));
        }

        public LandmineData explode() {
            despawnEntity();
            GameManager gm = InManager.get().getInstance(GameManager.class);
            blockPos.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, blockPos.add(0, 2, 0), 1, 0.0f, 0.0f, 0.0f, 0.0f);
            blockPos.getWorld().playSound(blockPos, Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2.0f, 0.0f);
            List<UUID> attacked = new ArrayList<>();
            NumberUtils.getSphere(blockPos, 3).forEach(bl -> {
                if(gm.getCurrentArena().getPlayArea().contains(bl) || gm.getCurrentArena().getLavaRegion().contains(bl)) removeBlock(placer, bl);
                Player attack = Bukkit.getPlayer(placer);
                blockPos.getWorld().getPlayers().stream().filter(pl -> LocationUtils.toBounding(pl)

                        .intersectsWith(new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(bl.getX(), bl.getY(), bl.getZ())))
                        .filter(b -> gm.getCurrentArena().isPlaying(b, true))
                        .forEach(b -> {
                            if(attacked.contains(b.getUniqueId()) || (attack == null || !gm.getCurrentArena().canBeDamaged(b, attack))) return;
                            attacked.add(b.getUniqueId());

                            gm.handleAttack(b, attack);
                            b.setVelocity(b.getVelocity().add(b.getLocation().subtract(blockPos).toVector().normalize()).normalize().multiply(1.2));
                            gm.attackPlayer(b, Math.max(0, 6 - ((int) b.getLocation().distance(blockPos))), attack);
                        });
            });
            return this;
        }

        private void removeBlock(UUID by, Location location) {
            Block b = location.getBlock();
            GameManager gm = InManager.get().getInstance(GameManager.class);
            int origin = b.getTypeId(), data = b.getData();
            if(!gm.handleBreak(Bukkit.getPlayer(by), false, location)) return;

            location.getWorld().playEffect(location, Effect.STEP_SOUND, origin, data);
        }
    }
}
