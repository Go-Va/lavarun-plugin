package me.gong.lavarun.plugin.powerup.impl;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.DeathEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.util.BukkitUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

public class FreezerPowerup extends Powerup {

    @Override
    public ItemStack getItem(Team team) {
        ItemStack ret = new ItemStack(Material.ICE);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(ChatColor.AQUA+getName());
        ret.setItemMeta(m);
        return ret;
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public String getName() {
        return "Freezer";
    }

    @Override
    public int getCost() {
        return 30;
    }
    @EventHandler
    public void onAttack(EntityDamageByEntityEvent ev) {
        if(ev.getDamager() instanceof Player && ev.getEntity() instanceof Player) {
            Player abuser = (Player) ev.getDamager(), victim = (Player) ev.getEntity();
            GameManager gm = InManager.get().getInstance(GameManager.class);
            if(isSelected(abuser) && gm.getCurrentArena().isPlaying(abuser, false) && gm.getCurrentArena().isPlaying(victim, false) &&
                    gm.getCurrentArena().canBeDamaged(victim, abuser) && !victim.hasPotionEffect(PotionEffectType.SLOW) && !abuser.hasPotionEffect(PotionEffectType.SLOW)) {
                freeze(victim, abuser);
                onUse(abuser);
            }
        }
    }

    public void freeze(Player player, Player by) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20 * 5, 6, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 20 * 5, 128));
        generateTitle(by).sendTo(player);
    }

    private BukkitUtils.Title generateTitle(Player by) {
        return new BukkitUtils.Title(ChatColor.AQUA+"You have been frozen by "+by.getName(), true, 0, 20, 3);
    }
}
