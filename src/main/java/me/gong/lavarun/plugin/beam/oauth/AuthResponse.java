package me.gong.lavarun.plugin.beam.oauth;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

import java.lang.reflect.Field;

public class AuthResponse {
    public String ip, username, port, serverIP, confirmation, token;

    public static AuthResponse fetchFrom(JSONObject object) {
        AuthResponse r = new AuthResponse();
        for(Field f : AuthResponse.class.getDeclaredFields()) {
            String s = r.attemptGet(f.getName(), object);
            if(s == null) return null;
            try {
                f.set(r, s);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return r;
    }

    public String attemptGet(String key, JSONObject object) {
        if(!object.containsKey(key)) return null;
        Object b = object.get(key);
        if(!(b instanceof String)) return null;
        return (String) b;
    }

    public void informInvalid() {
        Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
        if(player != null) player.sendMessage(ChatColor.RED+"The response received from the server didn't match up with what we were waiting for.");
    }

    public void informSuccess() {
        Player player = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(username)).findFirst().orElse(null);
        if(player != null) player.sendMessage(ChatColor.GOLD+"Successfully connected to beam!");
    }

    public boolean compareAgainst(AuthenticationWaiting waiting) {
        return waiting.getConfirmation().equals(confirmation) && waiting.getIp().equals(serverIP) && waiting.getUsername().equals(username) && waiting.getPort().equals(port);
    }
}
