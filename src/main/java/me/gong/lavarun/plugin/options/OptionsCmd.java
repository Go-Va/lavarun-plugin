package me.gong.lavarun.plugin.options;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.command.Cmd;
import me.gong.lavarun.plugin.command.annotation.Command;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class OptionsCmd implements Cmd {

    @Command(name = "options", alias = "o", help = "Configure options for the game", syntax = "<option> <on | off>")
    public boolean onCmd(Player player, String[] args) {
        if(!hasPerms(player)) return true;
        OptionsManager om = InManager.get().getInstance(OptionsManager.class);
        if(args.length == 0) {
            player.sendMessage(ChatColor.GREEN+"Currently available options: ");
            om.getOptions().forEach(o -> player.sendMessage(" "+o));
        } else {
            Option o = om.getOptionFor(args[0]);
            if(o == null) player.sendMessage(ChatColor.RED+"No option by the name of "+ChatColor.YELLOW+args[0]);
            else if(args.length < 2) return false;
            else {
                String at = args[1];
                boolean on = at.equalsIgnoreCase("on"), set;
                if(on) set = true;
                else if(at.equalsIgnoreCase("off")) {
                    set = false;
                } else return false;
                o.setEnabled(set);
                player.sendMessage((set ? ChatColor.GREEN+"En" : ChatColor.RED+"Dis")+"abled "+o.getName());
                Bukkit.broadcastMessage(ChatColor.YELLOW+ChatColor.BOLD.toString()+player.getName()+" has "+((set ? "en" : "dis")+"abled")+" the "+ChatColor.GREEN+o.getName()+" option"+ChatColor.BOLD+ChatColor.YELLOW+"!");
            }
        }

        return true;
    }
}
