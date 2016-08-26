package me.gong.lavarun.plugin.scoreboard;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.timer.Timer;
import me.gong.lavarun.plugin.timer.Timers;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.UUID;

/**
 * @author WesJD https://github.com/WesJD
 * @modifier TheMrGong
 */
public class ScoreboardManager implements Listener {

    private final HashMap<UUID, ScoreboardHandler> scoreboardHandlers = new HashMap<>();
    private final HashMap<UUID, Long> lastRuns = new HashMap<>();
    private Scoreboard blank;

    public void onEnable() {
        blank = Bukkit.getScoreboardManager().getNewScoreboard();
        Timers.register(this);
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
    }

    public void setScoreboard(Player p, ScoreboardHandler h) {
        if(h == null) {
            resetScoreboard(p);
            return;
        }
        final Scoreboard b = p.getScoreboard();
        if(b.getObjective(ScoreboardHandler.OBJECTIVE_NAME) != null) b.getTeams().forEach(team -> {
            if(!team.getName().contains("BoardLine")) return;
            team.setPrefix("");
            team.setSuffix("");
            team.getEntries().forEach(b::resetScores);
        });
        h.update(p);
        scoreboardHandlers.put(p.getUniqueId(), h);
    }

    public ScoreboardHandler getScoreboard(Player p) {
        return scoreboardHandlers.get(p.getUniqueId());
    }

    public void resetScoreboard(Player p) {
        p.setScoreboard(blank);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerJoinEvent ev) {
        ev.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if(scoreboardHandlers.containsKey(ev.getPlayer().getUniqueId())) scoreboardHandlers.remove(ev.getPlayer().getUniqueId());
    }

    @Timer(runEvery = 5L)
    public void tick() {

        scoreboardHandlers.keySet()
                .stream()
                .filter(uuid -> scoreboardHandlers.get(uuid).isAutoUpdating())
                .forEach(uuid -> {
                    final ScoreboardHandler sb = scoreboardHandlers.get(uuid);
                    final Player player = Bukkit.getPlayer(uuid);
                    if (player != null && shouldRun(sb, player)) sb.update(player);
                });
    }

    public boolean shouldRun(ScoreboardHandler handler, Player player) {
        if(!lastRuns.containsKey(player.getUniqueId())) {
            return true;
        }
        boolean elapsed = (System.currentTimeMillis() - lastRuns.get(player.getUniqueId())) >= handler.delay;
        if(elapsed) {
            lastRuns.put(player.getUniqueId(), System.currentTimeMillis());
            return true;
        }
        return false;
    }

}