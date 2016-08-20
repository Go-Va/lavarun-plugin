package me.gong.lavarun.plugin.beam.actions;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.beam.BeamAction;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.region.creation.box.Box;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.BlockUtils;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import pro.beam.interactive.net.packet.Protocol;

import java.util.ArrayList;
import java.util.List;

public class ChunkDestroyAction implements BeamAction {
    @Override
    public int getButtonId() {
        return 2;
    }

    @Override
    public void handlePress(Protocol.Report.TactileInfo info) {
        Player vic = getVictim();
        if(vic != null) {
            List<Box> bes = generateBoxes();
            for (Box b : bes) {

                Location min = b.getMinimum(vic.getWorld()), max = b.getMaximum(vic.getWorld());
                BlockUtils.outlineBox(min, max, 1).forEach(l -> vic.getWorld().spawnParticle(Particle.FLAME, l, 1, 0.0, 0.0, 0.0, 0.0));
                Location mid = new Location(vic.getWorld(), (min.getX() + max.getX()) / 2, (min.getY() + max.getY()) / 2, (min.getZ() + max.getZ()) / 2);
                vic.getWorld().spawnParticle(Particle.EXPLOSION_HUGE, mid, 1, 1.0, 1.0, 1.0, 0.0);
                clearBox(b, vic);
            }
            BukkitUtils.sendGlobalAction(ChatColor.GOLD.toString()+bes.size()+" "+ChatColor.RED+"box(es) were removed!");
        }
    }

    @Override
    public int getCooldown() {
        return 1000 * 2;
    }

    @Override
    public Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return builder.setDisabled(getVictim() == null);
    }

    private Player getVictim() {
        List<Player> rets = getGame().getCurrentArena().getPlaying(false);
        return rets.isEmpty() ? null : rets.get(NumberUtils.random.nextInt(rets.size()));
    }

    private List<Box> generateBoxes() {
        int num = NumberUtils.getRandom(1, 10);
        List<Box> ret = new ArrayList<>();
        for (int i = 0; i < num; i++) ret.add(generateBox());
        return ret;
    }

    private Box generateBox() {
        Region playArea = getGame().getCurrentArena().getPlayArea();
        Location min = playArea.getRandomLocation(), max = min.clone().add(5, 5, 5);
        return new Box(AxisAlignedBB.fromLocation(min, max));
    }
    
    private void clearBox(Box box, Player player) {
        box.getAxis().toLocations(getGame().getCurrentArena().getPlayArea().getWorld()).stream().filter(l -> getGame().getCurrentArena().isBlockInteractable(l, null, player, true)).forEach(l -> {
            l.getBlock().setType(getGame().getCurrentArena().getLavaRegion().contains(l) ? Material.LAVA : Material.AIR);
        });
    }
}
