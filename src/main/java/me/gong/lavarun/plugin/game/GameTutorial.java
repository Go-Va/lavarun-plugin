package me.gong.lavarun.plugin.game;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.logic.PointsMinerHandler;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.tutorial.usage.builder.TutorialBuilder;
import me.gong.lavarun.plugin.tutorial.usage.builder.TutorialElement;
import me.gong.lavarun.plugin.util.TimeUtils;
import org.bukkit.ChatColor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GameTutorial extends TutorialBuilder {
    public GameTutorial() {
        super(Arrays.stream(new TutorialElement[] {
            new TutorialElement(true, "&7You can't damage players on their own side", "Set a location overlooking lava", "Location set", 45),
                new TutorialElement("&7A point miner block spawns randomly", 45),
                new TutorialElement("&7every &e"+ TimeUtils.convertToString(PointsMinerHandler.RUN_EVERY)+"&7. Upon mining it, you", 35),
                new TutorialElement("&7will receive &a"+PointsMinerHandler.ADDED_POINTS+" points!", 40),
                new TutorialElement("&7To score, go to the enemy side", 45),
                new TutorialElement(true, "&7and jump into score area. You must stay in", "Set a location overlooking the score area", "Overlooking score area set.", 45),
                new TutorialElement("&7the score area for &e3 seconds&a to score.", 35),
                new TutorialElement("&7Upon scoring, your inventory will reset and", 35),
                new TutorialElement("&7You will have to buy a &aRestock Powerup", 25),
                new TutorialElement(true, "&7This is the shop area.", "Set location overlooking a shop area", "Overlooking location set.", 25),
                new TutorialElement("&7Step on the &afront pressure-plate&c to", 45),
                new TutorialElement("&7enter. Step on the &ainside pressure-plate", 45),
                new TutorialElement("&7to exit. There are many diverse powerups", 40),
                new TutorialElement("&7that you can purchase. ", 20)
        }).collect(Collectors.toList()));
        List<List<TutorialElement>> ad = InManager.get().getInstance(PowerupManager.class).getPowerups().stream()
                .map(p -> {
                    List<TutorialElement> ret = Arrays.stream(new TutorialElement[] {new TutorialElement(true, "&7This is the &3"+p.getName()+" powerup", "Set a location looking at the "+p.getName()+" powerup", "Powerup looker set.", 35)}).collect(Collectors.toList());
                    Arrays.stream(p.getHelp()).forEach(s -> ret.add(new TutorialElement(ChatColor.RED+s, 40)));
                    return ret;
                }).collect(Collectors.toList());
        ad.forEach(super::addElements);
        addElements(new TutorialElement(true, "This is the &afood area", "Set a location overlooking a food area", "Overlooking food area set.", 25),
                new TutorialElement("&7You can restock your food here.", 40),
                new TutorialElement("&7When low on food, your vision will zoom", 40),
                new TutorialElement("&7in and you will need food.", 35),
                new TutorialElement(true, "&7You have completed &athe tutorial!", "Set a location overlooking lava", "Overlooking lava set", 50));
    }
}
