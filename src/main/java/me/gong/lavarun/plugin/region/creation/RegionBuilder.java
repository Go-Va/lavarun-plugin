package me.gong.lavarun.plugin.region.creation;

import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.region.creation.box.Box;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegionBuilder {
    private List<Box> bbs;
    private UUID player;

    public RegionBuilder(Player player) {
        this.player = player.getUniqueId();
        this.bbs = new ArrayList<>();
    }

    public List<Box> getBoxes() {
        return bbs;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public UUID getUUID() {
        return player;
    }

    /**
     * Checks whether the builder is ready
     *
     * @return null if valid, otherwise, the string is the error message
     */
    public String isValid() {
        if(bbs.isEmpty()) return "No bounding boxes";
        return null;
    }

    public void addBox(Box bb) {
        bbs.add(bb);
    }

    public Region build(World world) {
        if(isValid() != null) return null;
        return new Region(bbs.toArray(new Box[bbs.size()]), world.getName());
    }
}
