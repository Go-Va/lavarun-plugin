package me.gong.lavarun.plugin.beam;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.beam.oauth.AuthenticationWaiting;
import me.gong.lavarun.plugin.beam.oauth.OAuthManager;
import me.gong.lavarun.plugin.command.Cmd;
import me.gong.lavarun.plugin.command.annotation.Command;
import me.gong.lavarun.plugin.command.annotation.SubCommand;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class BeamCmd implements Cmd {
    @Command(name = "beam", alias = "b", help = "Main beam command")
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

    @SubCommand(name = "gointeractive", help = "Connects interactive streams with your stream", alias = "goi", centralCommand = "beam")
    public boolean onInteractive(Player player, String[] args) {
        if(hasPerms(player)) {
            BeamManager bm = InManager.get().getInstance(BeamManager.class);
            OAuthManager oauth = bm.getOAuthManager();
            if(oauth.getWaiting() != null) player.sendMessage(ChatColor.GOLD+"Warning: There was an existing request sent to the server");
            AuthenticationWaiting authWait = oauth.beginWaiting(player.getName());
            TextComponent t = new TextComponent(ChatColor.GREEN+ChatColor.BOLD.toString()+"Click on this message to go interactive!");
            t.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, authWait.getClickable()));
            player.spigot().sendMessage(t);
            return true;
        } else return true;
    }

    @SubCommand(name = "endinteractive", help = "Ends interactive connection", alias = "ei", centralCommand = "beam")
    public boolean onEndInter(Player player, String[] args) {
        if(hasPerms(player)) {
            BeamManager bm = InManager.get().getInstance(BeamManager.class);
            if(bm.isInteractiveConnected()) {
                bm.endInteractive();
                player.sendMessage(ChatColor.GREEN+"Interactive stream ended.");
            } else player.sendMessage(ChatColor.RED+"No stream currently running");
            return true;
        } else return true;
    }


}
