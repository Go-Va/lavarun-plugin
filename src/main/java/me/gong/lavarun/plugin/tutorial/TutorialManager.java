package me.gong.lavarun.plugin.tutorial;

import me.gong.lavarun.plugin.tutorial.data.Tutorial;
import me.gong.lavarun.plugin.tutorial.usage.TutorialDisplayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TutorialManager {

    private List<UUID> inTutorial;

    public void onEnable() {
        inTutorial = new ArrayList<>();
    }

    public void onDisable() {
        inTutorial = null;
    }

    public void displayTutorial(Tutorial tutorial, Player player) {
        inTutorial.add(player.getUniqueId());
        new TutorialDisplayer(player, tutorial, to -> inTutorial.remove(player.getUniqueId()));
    }

    public List<UUID> getInTutorial() {
        return inTutorial;
    }
}
