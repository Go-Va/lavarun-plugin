package me.gong.lavarun.plugin.command;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public interface Cmd {
    default boolean hasPerms(Player player) {
        if(!player.isOp()) {
            player.sendMessage(ChatColor.RED+"Insufficient permissions young man");
            return false;
        }
        return true;
    }
}
