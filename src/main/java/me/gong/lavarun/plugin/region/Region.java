package me.gong.lavarun.plugin.region;

import me.gong.lavarun.plugin.util.*;
import me.gong.lavarun.plugin.region.creation.box.Box;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

//took from mcp v1.9 [so no nms needed]
public class Region {

    private List<Box> boxes;
    private String world;

    public Region(List<AxisAlignedBB> boxes, String world) {
        this.boxes = boxes.stream().map(Box::new).collect(Collectors.toList());
        this.world = world;
    }

    public Region(Box[] boxes, String world) {
        this.boxes = Arrays.stream(boxes).collect(Collectors.toList());
        this.world = world;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        JSONArray array = new JSONArray();
        boxes.forEach(a -> array.add(JSONUtils.boxToJSON(a.getAxis())));
        ret.put("boxes", array);
        ret.put("world", world);
        return ret;
    }

    public static Region fromJSON(JSONObject obj) {
        JSONArray array = (JSONArray) obj.get("boxes");
        List<AxisAlignedBB> aa = new ArrayList<>();
        for (Object o : array) aa.add(JSONUtils.jsonToBox((JSONObject)o));
        return new Region(aa, (String) obj.get("world"));
    }

    public List<Location> getBlocks() {
        List<Location> ret = new ArrayList<>();
        boxes.forEach(b -> ret.addAll(b.getAxis().toLocations(getWorld())));
        return ret;
    }

    public double getLargestLength() {
        double largest = -1;
        for(Box b : boxes) {
            double length = b.getAxis().getLength();
            if(length > largest) largest = length;
        }
        return largest;
    }

    public double getLargestWidth() {
        double largest = -1;
        for(Box b : boxes) {
            double width = b.getAxis().getWidth();
            if(width > largest) largest = width;
        }
        return largest;
    }

    public double getLargestHeight() {
        double largest = -1;
        for(Box b : boxes) {
            double height = b.getAxis().getHeight();
            if(height > largest) largest = height;
        }
        return largest;
    }

    public Location getRandomLocation() {
        Location n = getMinimum(), x = getMaximum();
        return new Location(getWorld(), NumberUtils.getRandom(n.getBlockX(), x.getBlockX()),
                NumberUtils.getRandom(n.getBlockY(), x.getBlockY()),
                NumberUtils.getRandom(n.getBlockZ(), x.getBlockZ()));
    }
    
    public Location getMinimum() {
        Location min = null;
        for(Box b : boxes) {
            Location l = b.getMinimum(getWorld());
            if(min == null || (l.getX() < min.getX() && l.getZ() < min.getZ() && l.getY() < min.getY()))
                min = l;
        }
        return min;
    }

    public Location getMaximum() {
        Location max = null;
        for(Box b : boxes) {
            Location l = b.getMaximum(getWorld());
            if(max == null || (l.getX() > max.getX() && l.getZ() > max.getZ() && l.getY() > max.getY()))
                max = l;
        }
        return max;
    }

    public List<Box> getBoxes() {
        return boxes;
    }

    public boolean contains(Location location) {
        return boxes.stream().anyMatch(s -> s.getAxis().isVecAlmostInside(new Vec3d(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
    }

    public boolean contains(AxisAlignedBB bb) {
        return boxes.stream().anyMatch(s -> s.getAxis().intersectsWith(bb));
    }

    public boolean contains(Player player) {
        return contains(player.getLocation());
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }
}
