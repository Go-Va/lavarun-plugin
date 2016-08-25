package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.PreventBreakEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SabotagePowerup extends Powerup {
    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.LEVER);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+getName());
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        m.addEnchant(Enchantment.ARROW_KNOCKBACK, 1, true);
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 4;
    }

    @Override
    public String getName() {
        return "Sabotage";
    }

    @Override
    public int getCost() {
        return 40;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(PreventBreakEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena a = gm.getCurrentArena();
        Player p = ev.getPlayer();
        Block b = ev.getBlock();
        if(a.isPlaying(p, false) && isSelected(p) && a.getPlayArea().contains(b.getLocation()) && b.getType() == Material.STAINED_GLASS && b.getData() != 0) {
            ev.setCancelled(true);
            if(ev.isBreak()) {
                onUse(p);
                gm.handleBreak(ev.getPlayer(), true, b.getLocation());
            }
        }
    }
}
