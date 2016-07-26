package net.wesjd.lavarun.plugin.region.creation;

import net.wesjd.lavarun.plugin.region.Region;
import net.wesjd.lavarun.plugin.util.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegionBuilder {
    private String name;
    private List<AxisAlignedBB> bbs;
    private UUID player;

    public RegionBuilder(Player player) {
        this.player = player.getUniqueId();
        this.bbs = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<AxisAlignedBB> getBoxes() {
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
        if(name == null) return "Name is not set";
        if(bbs.isEmpty()) return "No bounding boxes";
        return null;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBox(AxisAlignedBB bb) {
        bbs.add(bb);
    }

    public Region build() {
        if(isValid() != null) return null;
        return new Region(name, bbs);
    }
}
