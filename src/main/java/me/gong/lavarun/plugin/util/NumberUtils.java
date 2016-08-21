package me.gong.lavarun.plugin.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NumberUtils {
    public static Random random = new Random();
    public static SecureRandom secureRandom = new SecureRandom();


    public static int getRandom(int min, int max) {
        return random.nextInt((max - min) + 1) + min;
    }

    public static int getSecureRandom(int min, int max) {
        return secureRandom.nextInt((max - min) + 1) + min;
    }

    public static void knockEntity(Vector from, Entity e, float knockback) {
        Vector vec = e.getVelocity();
        double motX = vec.getX(), motY = vec.getY(), motZ = vec.getZ();
        float f = MathHelper.sqrt_double(from.getX() * from.getX() + from.getZ() * from.getZ());
        motX /= 2;
        motZ /= 2;
        motX -= from.getX() / (double) f * (double) knockback;
        motZ -= from.getZ() / (double) f * (double) knockback;
        motX *= 0.6;
        motZ *= 0.6;

        if(e.isOnGround()) {
            motY /= 2;
            motY = Math.min(0.4000000059604645D, motY + knockback);
        }
        e.setVelocity(new Vector(motX, motY, motZ));
    }

    public static void knockEntityWithKnockback(Entity from, Entity e, float knockback) {
        knockEntity(new Vector((double) MathHelper.sin(from.getLocation().getYaw() * 0.017453292F), 0,
                (double) (-MathHelper.cos(from.getLocation().getYaw() * 0.017453292F))), e, knockback);
    }

    public static void knockEntityWithDamage(Entity from, Entity e) {
        knockEntity(new Vector(from.getLocation().getX() - e.getLocation().getX(), 0,
                from.getLocation().getZ() - e.getLocation().getZ()), e, 0.5f);
    }

    public static double trim(int level, double value) {
        StringBuilder sb = new StringBuilder("#.#");
        for(int i=0; i < level; i++) sb.append("#");
        return Double.valueOf(new DecimalFormat(sb.toString()).format(value));
    }

    public static Vector getVectorForPlayer(Player player) {
        float yaw = player.getLocation().getYaw(), pitch = player.getLocation().getPitch();
        float f = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI); //negative yaw -> radians - 180 in radians, cosine
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector((double) (f1 * f2), (double) f3, (double) (f * f2));
    }

    public static Set<Location> getSphere(Location center, int radius, int height, boolean hollow, boolean sphere, int plus_y) {
        Set<Location> locs = new HashSet<>();

        int cx = center.getBlockX();
        int cy = center.getBlockY();
        int cz = center.getBlockZ();
        for (int x = cx - radius; x <= cx + radius; x++) {
            for (int z = cz - radius; z <= cz + radius; z++) {
                for (int y = (sphere ? cy - radius : cy); y < (sphere ? cy + radius : cy + height); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < radius * radius && !(hollow && dist < (radius - 1) * (radius - 1))) {
                        Location l = new Location(center.getWorld(), x, y + plus_y, z);
                        locs.add(l);
                    }
                }
            }
        }

        return locs;
    }

    public static Set<Location> getSphere(Location center, int radius) {
        return getSphere(center, radius, 0, false, true, 0);
    }

}
