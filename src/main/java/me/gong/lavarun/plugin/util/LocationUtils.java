package me.gong.lavarun.plugin.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationUtils {
    public static AxisAlignedBB toBounding(Player player) {
        Location pos1 = player.getLocation().clone().subtract(0.3, 0, 0.3);
        Location pos2 = pos1.clone().add(0.6, 1.8, 0.6);
        return new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }
}
