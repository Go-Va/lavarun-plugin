package me.gong.lavarun.plugin.beam.actions;

import me.gong.lavarun.plugin.beam.BeamAction;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.LocationUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import pro.beam.interactive.net.packet.Protocol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class SpleefAction implements BeamAction {

    private BukkitUtils.Title spleefed = new BukkitUtils.Title(ChatColor.RED+"You were spleefed!", true, 5, 20, 3);

    private Map<UUID, Long> spleefedPlayers = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(getGame().isInGame() && getGame().getCurrentArena().isPlaying(ev.getPlayer(), false) &&
                getGame().getCurrentArena().getTeam(ev.getPlayer()).getTimeBetweenLastRespawn(ev.getPlayer()) > 5000 &&
                spleefedPlayers.containsKey(ev.getPlayer().getUniqueId()) &&
                System.currentTimeMillis() - spleefedPlayers.get(ev.getPlayer().getUniqueId()) < 1000 * 4)
            ev.setCancelled(true);
    }

    @Override
    public int getButtonId() {
        return 0;
    }

    @Override
    public void handlePress(Protocol.Report.TactileInfo info) {

        Player vic = getVictim();
        if(vic == null) getManager().broadcastToStream(ChatColor.RED+"Uh oh! Nobody in spleefable area", true);
        else {
            spleefed.sendTo(vic);
            spleefPlayer(vic);
        }
    }

    @Override
    public int getCooldown() {
        return 1000 * 30;
    }

    @Override
    public Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return builder.setDisabled(getVictim() == null);
    }

    private Player getVictim() {
        List<Player> victim = getGame().getCurrentArena().getPlaying(false).stream()
                .filter(p -> getGame().getCurrentArena().getPlayArea().contains(p))
                .collect(Collectors.toList());
        if(!victim.isEmpty()) return victim.get(NumberUtils.random.nextInt(victim.size()));
        return null;
    }

    private void spleefPlayer(Player player) {
        Location loc = player.getLocation();
        spleefedPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        for(int x = loc.getBlockX() - 3; x <= loc.getBlockX() + 3; x++) {
            for(int y = loc.getBlockY() - 10; y <= loc.getBlockY() + 1; y++) {
                for(int z = loc.getBlockZ() - 3; z <= loc.getBlockZ() + 3; z++) {
                    AxisAlignedBB b = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1),
                            pl = LocationUtils.toBounding(player).expand(0, 20, 0);
                    if(pl.intersectsWith(b)) removeBlock(new Location(player.getWorld(), x, y, z));
                }
            }
        }
    }

    private void removeBlock(Location location) {
        Block b = location.getBlock();
        if(b.getType() != Material.STAINED_GLASS) return;
        if(getGame().getCurrentArena().getLavaRegion().contains(location)) b.setType(Material.LAVA);
        else b.setType(Material.AIR);
    }
}
