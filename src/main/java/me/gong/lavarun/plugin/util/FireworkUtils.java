package me.gong.lavarun.plugin.util;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

public class FireworkUtils {

    public static Firework spawnFirework(Location location, boolean flicker, Color color, Color fade, FireworkEffect.Type type,
                                                           boolean trail, int power) {
        return spawnFirework(location, getEffect(flicker, color, fade, type, trail), power);
    }

    public static Firework spawnFirework(Location location, FireworkEffect effect, int power) {
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fwm = fw.getFireworkMeta();
        fwm.addEffect(effect);
        fwm.setPower(power);
        fw.setFireworkMeta(fwm);
        return fw;
    }
    
    public static FireworkEffect getEffect(boolean flicker, Color color, Color fade, FireworkEffect.Type type, boolean trail) {
        return FireworkEffect.builder().flicker(flicker).withColor(color).withFade(fade).with(type).trail(trail).build();
    }

}