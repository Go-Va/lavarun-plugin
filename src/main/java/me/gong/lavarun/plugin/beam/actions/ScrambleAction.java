package me.gong.lavarun.plugin.beam.actions;

import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.beam.BeamAction;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import pro.beam.interactive.net.packet.Protocol;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ScrambleAction implements BeamAction {
    @Override
    public int getButtonId() {
        return 3;
    }

    @Override
    public void handlePress(Protocol.Report.TactileInfo info) {
        int am = 0;
        for (Block b : getScrambableBlocks()) {
            if(NumberUtils.random.nextBoolean() && NumberUtils.random.nextBoolean()) {
                am++;
                swap(b);
            }
        }
        BukkitUtils.sendGlobalAction(ChatColor.GOLD.toString()+am+" block(s) were randomly swapped!");
    }

    @Override
    public Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return builder.setDisabled(!getGame().isInGame());
    }

    @Override
    public int getCooldown() {
        return 1000 * 20;
    }

    public void swap(Block block) {
        if(block.getType() != Material.STAINED_GLASS) return;
        int id = block.getData();
        Team bl = getGame().getCurrentArena().getBlue(), rd = getGame().getCurrentArena().getRed();
        boolean ch = false;
        byte from = block.getData();
        if(id == rd.getGlassColor()) {
            ch = true;
            block.setData((byte) bl.getGlassColor());
        }
        else if(id == bl.getGlassColor()) {
            ch = true;
            block.setData((byte) rd.getGlassColor());
        }
        if(ch) block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getTypeId(), from);
    }

    public List<Block> getScrambableBlocks() {
        List<Block> ret = new ArrayList<>();
        Predicate<Location> validType = b -> b.getBlock().getType().equals(Material.STAINED_GLASS);
        getGame().getCurrentArena().getPlayArea().getBlocks().stream()
                .filter(validType).map(Location::getBlock)
                .collect(Collectors.toList()).forEach(ret::add);
        getGame().getCurrentArena().getLavaRegion().getBlocks().stream()
                .filter(validType).map(Location::getBlock)
                .collect(Collectors.toList()).forEach(ret::add);
        return ret;
    }
}
