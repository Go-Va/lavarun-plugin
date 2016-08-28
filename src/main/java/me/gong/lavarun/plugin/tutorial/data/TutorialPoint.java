package me.gong.lavarun.plugin.tutorial.data;

import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.JSONUtils;
import me.gong.lavarun.plugin.util.StringUtils;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;

public class TutorialPoint {
    private final Location teleportTo;
    private final int showTime;
    private final String message;

    private long shown;

    public TutorialPoint(Location teleportTo, int showTime, String message) {
        this.teleportTo = teleportTo;
        this.showTime = showTime;
        this.message = message;
    }

    public void showTo(Player player) {
        new BukkitUtils.Title(StringUtils.format(message), true, 0, showTime, 0).sendTo(player, true, false);
        this.shown = System.currentTimeMillis();

        if(teleportTo != null) player.teleport(teleportTo);
    }

    public boolean isBeingShown() {

        if(System.currentTimeMillis() >= shown + (showTime * 50) - 50) {
            shown = 0;
            return false;
        }
        return true;
    }

    public TutorialPoint copy() {
        return new TutorialPoint(teleportTo == null ? null : teleportTo.clone(), showTime, message);
    }

    public JSONObject saveToJSON() {
        JSONObject ret = new JSONObject();
        if(teleportTo != null) ret.put("teleportTo", JSONUtils.locationToJSON(teleportTo));
        ret.put("showTime", showTime);
        ret.put("message", message);
        return ret;
    }

    public static TutorialPoint loadFromJSON(JSONObject object) {
        Location l = object.containsKey("teleportTo") ? JSONUtils.locationFromJSON((JSONObject) object.get("teleportTo")) : null;
        int showTime = (int) (long) object.get("showTime");
        String message = (String) object.get("message");
        return new TutorialPoint(l, showTime, message);
    }


}
