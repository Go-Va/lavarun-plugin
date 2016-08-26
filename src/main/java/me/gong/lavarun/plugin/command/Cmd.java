package me.gong.lavarun.plugin.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface Cmd {
    default boolean hasPerms(Player player, boolean message) {
        if(!player.isOp() && !player.hasPermission("lavarun.admin") && !player.hasPermission("*")) {
            if(message) player.sendMessage(ChatColor.RED+"Insufficient permissions young man");
            return false;
        }
        return true;
    }
    default boolean hasPerms(Player player) {
        return hasPerms(player, true);
    }
}
