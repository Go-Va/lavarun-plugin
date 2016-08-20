package me.gong.lavarun.plugin.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONUtils {

    public static JSONParser parser = new JSONParser();

    public static JSONObject locationToJSON(Location location) {
        JSONObject obj = new JSONObject();
        obj.put("x", location.getX());
        obj.put("y", location.getY());
        obj.put("z", location.getZ());
        obj.put("world", location.getWorld().getName());
        return obj;
    }

    public static Location locationFromJSON(JSONObject obj) {
        return new Location(Bukkit.getWorld((String) obj.get("world")), (double) obj.get("x"), (double) obj.get("y"), (double) obj.get("z"));
    }

    public static JSONObject boxToJSON(AxisAlignedBB bb) {
        JSONObject ret = new JSONObject();
        ret.put("minX", bb.minX);
        ret.put("minY", bb.minY);
        ret.put("minZ", bb.minZ);
        ret.put("maxX", bb.maxX);
        ret.put("maxY", bb.maxY);
        ret.put("maxZ", bb.maxZ);
        return ret;
    }

    public static AxisAlignedBB jsonToBox(JSONObject obj) {
        return new AxisAlignedBB((double) obj.get("minX"), (double) obj.get("minY"), (double) obj.get("minZ"),
                (double) obj.get("maxX"), (double) obj.get("maxY"), (double) obj.get("maxZ"));
    }
}
