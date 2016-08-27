package me.gong.lavarun.plugin.game.scoreboard;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.scoreboard.ScoreboardHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class LobbyBoard extends ScoreboardHandler {

    public LobbyBoard() {
        super("&c&lLava &6&lRun &7[Lobby]", 1);
    }
    @Override
    protected void build(Player p) {
        setScore("&l&m"+ StringUtils.repeat("-", 28));
        GameManager gm = InManager.get().getInstance(GameManager.class);
        setScore("&7Current arena: &a"+gm.getCurrentArena().getName());
        setScore(null);
        setScore("&7Players: &e"+ Bukkit.getOnlinePlayers().size());

        setScore(null);
        setScore("&3*Goal of the game is to");
        setScore("&3 get to the other side of");
        setScore("&3 the map and get in the");
        setScore("&3 capture area. Create a");
        setScore("&3 defense and kill your");
        setScore("&3 attackers.");

        setScore("&3*To get food you must get");
        setScore("&3 to the middle food area");
        setScore(null);
        setScore("&cAlpha game by &eTheMrGong");
    }
}
