package net.wesjd.lavarun.plugin.region.creation.box;

import net.wesjd.lavarun.plugin.region.creation.RegionCreator;
import net.wesjd.lavarun.plugin.util.AxisAlignedBB;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BoxCreator {

    private Location pos1, pos2;
    private CreationListener listener;

    public BoxCreator(CreationListener listener) {
        this.listener = listener;
    }

    public boolean isValid() {
        return pos1 != null && pos2 != null;
    }

    public Location getPos1() {
        return pos1;
    }

    public Location getPos2() {
        return pos2;
    }

    public void reset() {
        pos1 = null;
        pos2 = null;
    }

    private void setPosition(boolean type, Location location, Player player) {
        if(type) pos1 = location.clone();
        else pos2 = location.clone();
        player.sendMessage(ChatColor.GREEN+"Position "+(type ? "1" : "2")+" set to "+location.getX()+", "+location.getY()+", "+location.getZ());
    }

    private void create(Player player) {
        listener.onCreate(new AxisAlignedBB(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ()));
    }

    public void onClick(boolean left, Location location, Player player) {
        setPosition(left, location, player);
        if(isValid()) create(player);
    }

    public interface CreationListener {
        void onCreate(AxisAlignedBB bb);
    }
}
