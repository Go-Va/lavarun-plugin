package me.gong.lavarun.plugin.beam.actions;

import me.gong.lavarun.plugin.beam.BeamAction;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import pro.beam.interactive.net.packet.Protocol;

import java.util.List;

public class RestoreHungerAction implements BeamAction {
    @Override
    public int getButtonId() {
        return 4;
    }

    @Override
    public void handlePress(Protocol.Report.TactileInfo info) {
        Player targ = getTarget();
        if(targ != null) {
            targ.setFoodLevel(20);
            targ.setSaturation(15);
            getGame().setHunger(targ, false);
            BukkitUtils.sendActionMessage(targ, ChatColor.GREEN+"Your hunger was restored by a viewer!");
        }
    }

    @Override
    public Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return builder.setDisabled(getTarget() == null);
    }

    public List<Player> getTargets() {
        return getGame().getCurrentArena().getPlaying(true);
    }

    public Player getTarget() {
        return getGame().isInGame() && getTargets().size() > 0 ? getTargets().get(NumberUtils.random.nextInt(getTargets().size())) : null;
    }

    @Override
    public int getCooldown() {
        return 30000;
    }
}
