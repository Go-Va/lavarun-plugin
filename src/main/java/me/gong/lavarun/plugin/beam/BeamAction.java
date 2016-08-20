package me.gong.lavarun.plugin.beam;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.GameManager;
import org.bukkit.event.Listener;
import pro.beam.interactive.net.packet.Protocol;

public interface BeamAction extends Listener {

    int getButtonId();

    void handlePress(Protocol.Report.TactileInfo info);

    int getCooldown();

    default Protocol.ProgressUpdate.TactileUpdate.Builder updateButton(Protocol.ProgressUpdate.TactileUpdate.Builder builder) {
        return null;
    }

    default BeamManager getManager() {
        return InManager.get().getInstance(BeamManager.class);
    }

    default GameManager getGame() {
        return InManager.get().getInstance(GameManager.class);
    }
}
