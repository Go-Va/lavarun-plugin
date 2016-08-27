package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.powerup.Powerup;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ResupplyPowerup extends Powerup {
    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.CHEST);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.GREEN+getName());
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 0;
    }

    @Override
    public String getName() {
        return "Resupply";
    }

    @Override
    public int getCost() {
        return 15;
    }

    @Override
    public String[] getHelp() {
        return new String[] {"Get a resupply after either running",
        "out of blocks or capturing."};
    }


}
