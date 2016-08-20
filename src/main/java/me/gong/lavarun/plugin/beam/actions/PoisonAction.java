package me.gong.lavarun.plugin.beam.actions;

import me.gong.lavarun.plugin.beam.BeamAction;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import pro.beam.interactive.net.packet.Protocol;

import java.util.List;

public class PoisonAction implements BeamAction {

    private BukkitUtils.Title title = new BukkitUtils.Title(ChatColor.GOLD+"You were poisoned", true, 5, 20, 1);

    @Override
    public int getButtonId() {
        return 1;
    }

    @Override
    public void handlePress(Protocol.Report.TactileInfo info) {
        if(getVictim() == null) return;
        Player vic = getVictim();
        vic.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20 * 10, 3, false, false));
        title.sendTo(vic);
    }

    @Override
    public int getCooldown() {
        return 1000 * 10;
    }

    @Override
    public Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return builder.setDisabled(getVictim() == null);
    }

    private Player getVictim() {
        List<Player> available = getGame().getCurrentArena().getPlaying(true);
        if(available.isEmpty()) return null;
        return available.get(NumberUtils.random.nextInt(available.size()));
    }
}
