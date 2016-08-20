package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class KnockerPowerup extends Powerup {
    @Override
    public ItemStack getItem(Team team) {
        ItemStack s = new ItemStack(Material.IRON_HOE);
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+getName());
        m.spigot().setUnbreakable(true);
        s.setItemMeta(m);
        return BukkitUtils.create1_8Pvp(s);
    }

    @Override
    public String getName() {
        return "Knocker";
    }

    @Override
    public int getMaxUses() {
        return 2;
    }

    @Override
    public int getCost() {
        return 10;
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(m.getCurrentArena() != null && m.isInGame() && ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
            Arena ca = m.getCurrentArena();
            Player dm = (Player) ev.getDamager(), p = (Player) ev.getEntity();
            if(ca.isPlaying(dm, true) && ca.isPlaying(p, true) && isSelected(dm)) {
                onUse(dm);
                if(ca.canBeDamaged(p, dm)) {
                    NumberUtils.knockEntityWithKnockback(dm, p, 200);
                    dm.getWorld().playSound(p.getLocation(), Sound.ENTITY_ZOMBIE_BREAK_DOOR_WOOD, 2.0f, 2.0f);
                }
            }
        }
    }
}
