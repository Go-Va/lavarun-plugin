package me.gong.lavarun.plugin.beam;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.command.Cmd;
import me.gong.lavarun.plugin.command.annotation.Command;
import me.gong.lavarun.plugin.command.annotation.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BeamCmd implements Cmd {
    @Command(name = "beam", help = "Main beam command")
    public boolean onCmd(Player player, String[] args) {
        return !hasPerms(player);
    }

    @SubCommand(name = "reload", help = "Reloads the config", centralCommand = "beam")
    public boolean onReload(Player player, String[] args) {
        if(hasPerms(player)) {
            InManager.get().getInstance(BeamManager.class).reloadRobot();
            player.sendMessage(ChatColor.GREEN+"Beam config reloaded.");
            return true;
        } else return true;
    }

    @SubCommand(name = "stream", help = "Toggles streaming", centralCommand = "beam")
    public boolean onStreamToggle(Player player, String[] args) {
        if(hasPerms(player)) {
            BeamManager b = InManager.get().getInstance(BeamManager.class);
            player.sendMessage(ChatColor.GOLD+"You are "+(b.toggleStreaming(player) ? "now" : "no longer")+" streaming.");
            return true;
        } else return true;
    }


}
