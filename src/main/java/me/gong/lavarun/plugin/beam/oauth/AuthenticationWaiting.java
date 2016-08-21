package me.gong.lavarun.plugin.beam.oauth;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.beam.BeamManager;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AuthenticationWaiting {
    private long initial;

    private String username, confirmation, port, ip;

    public AuthenticationWaiting(String username, String confirmation, String port, String ip) {
        this.username = username;
        this.confirmation = confirmation;
        this.port = port;
        this.ip = ip;
        this.initial = System.currentTimeMillis();
    }

    public AuthenticationWaiting(String player) {
        this.username = player;
        this.confirmation = generateConfirmation();
        Main.Config config = InManager.get().getInstance(Main.Config.class);
        this.port = config.getPort()+"";
        this.ip = InManager.get().getInstance(Main.Config.class).getServerIP();
    }

    public String generateConfirmation() {
        return NumberUtils.getSecureRandom(1, 9)+generateSequence(9, 0, 9);
    }

    public String generateSequence(int amount, int min, int max) {
        String ret = "";
        for (int i = 0; i < amount; i++) ret += NumberUtils.getSecureRandom(min, max);
        return ret;
    }

    public long getInitial() {
        return initial;
    }

    public String getUsername() {
        return username;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public String getPort() {
        return port;
    }

    public String getIp() {
        return ip;
    }

    public long getTimeSinceInitialization() {
        return System.currentTimeMillis() - initial;
    }

    public void informRemoved() {
        Player p = Bukkit.getOnlinePlayers().stream().filter(pl -> pl.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
        if(p != null) p.sendMessage(ChatColor.RED+"Took too long to get a response from oauth server.");
    }

    public String getClickable() {
        return BeamManager.LAVA_RUN_SERVICE.replace("%username", username).replace("%confirm", confirmation).replace("%port", port).replace("%ip", ip);
    }
}
