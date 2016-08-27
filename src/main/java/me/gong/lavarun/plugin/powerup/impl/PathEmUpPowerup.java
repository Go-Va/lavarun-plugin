package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.timer.Timers;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class PathEmUpPowerup extends Powerup {

    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.BLAZE_ROD);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.LIGHT_PURPLE+getName());
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        m.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 3;
    }

    @Override
    public String getName() {
        return "PathEmUp";
    }

    @Override
    public int getCost() {
        return 30;
    }

    @Override
    public String[] getHelp() {
        return new String[] {"Right click near the play area to",
        "begin making a path. Sneak to make the path go",
        "downwards, jump to make it go up."};
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(ev.getAction().name().contains("RIGHT") && m.getCurrentArena() != null && m.isInGame() && isSelected(ev.getPlayer())) {
            ev.setCancelled(true);
            new PathCreator(ev.getPlayer(), BukkitUtils.yawToFace(ev.getPlayer().getLocation().getYaw()));
            onUse(ev.getPlayer());
        }

    }

    public class PathCreator {

        private Location at;
        private UUID uuid;
        private BlockFace direction;
        private List<Timers.TimerObject> objectList;
        private int amountLeft, yDif;
        private boolean diagnal;

        public PathCreator(Player player, BlockFace direction) {
            this.at = player.getLocation().clone().add(0, -1, 0);
            this.uuid = player.getUniqueId();
            this.direction = direction.getOppositeFace();
            this.objectList = Timers.register(this);
            this.amountLeft = NumberUtils.getRandom(6, 15);
            if(!isStillValid()){
                at.add(0, -yDif, 0);
                move();
            }
            if(!player.isOnGround()) yDif = 1;
            else if(player.isSneaking()) yDif = -1;
        }

        public Player getPlayer() {
            return Bukkit.getPlayer(uuid);
        }

        public boolean isStillValid() {
            GameManager gm = InManager.get().getInstance(GameManager.class);

            return gm.isInGame() && getPlayer() != null && isReplaceable(at) &&
                    (gm.getCurrentArena().getLavaRegion().contains(at) || gm.getCurrentArena().getPlayArea().contains(at)) && amountLeft > 0;
        }

        public boolean isReplaceable(Location location) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Block b = location.getBlock();
            Predicate<Location> isValid = loc -> loc.getBlock().getType() == Material.AIR ||
                    loc.getBlock().getType() == Material.STATIONARY_LAVA || !gm.getCurrentArena().getPlayArea().contains(loc);
            if(isValid.test(location)) return true;
            if(b.getType() == Material.STAINED_GLASS) {
                return b.getData() == gm.getCurrentArena().getTeam(getPlayer()).getGlassColor() &&
                        isValid.test(b.getRelative(0, 1, 0).getLocation()) && isValid.test(b.getRelative(0, -1, 0).getLocation());
            } else return false;
        }

        public void setBlockAndMove() {
            GameManager m = InManager.get().getInstance(GameManager.class);
            Player p = getPlayer();
            Team team = m.getCurrentArena().getTeam(p);
            ItemStack use = team.getGlass(p);
            Block blockAt = at.getBlock();
            if(m.getCurrentArena().isBlockInteractable(at, use, p, false)) blockAt.setTypeIdAndData(Material.STAINED_GLASS.getId(), (byte) team.getGlassColor(), false);

            move();
        }

        public void move() {

            if(isMovingDiagnal()) {
                if(diagnal) at = at.add(getDiff(direction.getModX()), yDif, 0);
                else at = at.add(0, yDif, getDiff(direction.getModZ()));
                diagnal =! diagnal;
            } else {
                if(yDif != 0) at.add(0, yDif, 0);
                at = at.getBlock().getRelative(direction).getLocation();
            }
            amountLeft--;
        }

        public int getDiff(int am) {
            if(am > 0) {
                return Math.min(1, am);
            } else if(direction.getModX() < 0) {
                return Math.max(-1, am);
            } else return 0;
        }

        public boolean isMovingDiagnal() {
            return direction.getModX() != 0 && direction.getModZ() != 0;
        }

        @Timer(runEvery = 5)
        public void moveTick() {
            if(!isStillValid()) InManager.get().getInstance(Timers.class).unregister(objectList);
            else setBlockAndMove();
        }
    }
}
