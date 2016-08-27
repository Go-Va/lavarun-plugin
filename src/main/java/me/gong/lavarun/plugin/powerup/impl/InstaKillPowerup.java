package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.PreventBreakEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class InstaKillPowerup extends Powerup {
    @Override
    public ItemStack getItem(Team team) {
        ItemStack s = new ItemStack(Material.FIREBALL);
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(ChatColor.RED+getName());
        s.setItemMeta(m);
        return s;
    }

    @Override
    public String getName() {
        return "InstaKill";
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public int getCost() {
        return 50;
    }

    @Override
    public String[] getHelp() {
        return new String[] {"Instantly kills players upon hit.",
        "Only works on players that aren't on their side."};
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent ev) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(m.getCurrentArena() != null && m.isInGame() && ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
            Arena ca = m.getCurrentArena();
            Player dm = (Player) ev.getDamager(), p = (Player) ev.getEntity();
            if(ca.isPlaying(dm, true) && ca.isPlaying(p, true) && isSelected(dm)) {

                if(ca.canBeDamaged(p, dm)) {
                    onUse(dm);
                    ev.setCancelled(true);
                    m.handleAttack(p, dm);
                    m.handleKill(p);

                    dm.getWorld().playSound(p.getLocation(), Sound.BLOCK_ANVIL_LAND, 2.0f, 0.0f);
                }
            }
        }
    }
}
