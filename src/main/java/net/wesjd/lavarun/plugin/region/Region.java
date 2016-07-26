package net.wesjd.lavarun.plugin.region;

import net.wesjd.lavarun.plugin.util.AxisAlignedBB;
import net.wesjd.lavarun.plugin.util.Vec3d;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

//took from mcp v1.9 [so no nms needed]
public class Region {

    private String name;
    private List<AxisAlignedBB> boxes;

    public Region(String name, List<AxisAlignedBB> boxes) {
        this.name = name;
        this.boxes = boxes;
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("name", name);
        JSONArray array = new JSONArray();
        boxes.forEach(a -> array.add(boxToJSON(a)));
        ret.put("boxes", array);
        return ret;
    }

    public static Region fromJSON(JSONObject obj) {
        String name = (String) obj.get("name");
        JSONArray array = (JSONArray) obj.get("boxes");
        List<AxisAlignedBB> aa = new ArrayList<>();
        for (Object o : array) aa.add(jsonToBox((JSONObject)o));
        return new Region(name, aa);
    }

    public List<AxisAlignedBB> getBoxes() {
        return boxes;
    }

    public boolean contains(Location location) {
        return boxes.stream().anyMatch(s -> s.isVecAlmostInside(new Vec3d(location.getBlockX(), location.getBlockY(), location.getBlockZ())));
    }

    public boolean contains(AxisAlignedBB bb) {
        return boxes.stream().anyMatch(s -> s.intersectsWith(bb));
    }

    public boolean contains(Player player) {
        return contains(toBounding(player));
    }

    private AxisAlignedBB toBounding(Player player) {
        Location pos1 = player.getLocation().clone().subtract(0.3, 0, 0.3);
        Location pos2 = pos1.clone().add(0.6, 1.8, 0.6);
        return new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    private JSONObject boxToJSON(AxisAlignedBB bb) {
        JSONObject ret = new JSONObject();
        ret.put("minX", bb.minX);
        ret.put("minY", bb.minY);
        ret.put("minZ", bb.minZ);
        ret.put("maxX", bb.maxX);
        ret.put("maxX", bb.maxY);
        ret.put("maxX", bb.maxZ);
        return ret;
    }

    private static AxisAlignedBB jsonToBox(JSONObject obj) {
        return new AxisAlignedBB((double) obj.get("minX"), (double) obj.get("minY"), (double) obj.get("minZ"),
                (double) obj.get("maxX"), (double) obj.get("maxY"), (double) obj.get("maxZ"));
    }
}
