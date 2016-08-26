package me.gong.lavarun.plugin.scoreboard;

import me.gong.lavarun.plugin.util.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author WesJD https://github.com/WesJD
 * @modifier TheMrGong
 */
public abstract class ScoreboardHandler {

    public static final String OBJECTIVE_NAME = UUID.randomUUID().toString().substring(0, 15);

    protected Scoreboard currentLocalScoreboard;
    protected int delay, currentLine;

    private final List<ChatColor> colors = new ArrayList<>(Arrays.asList(ChatColor.values()));
    private UUID currentPlayer;
    private String displayName;
    private boolean autoUpdate;

    public ScoreboardHandler() {
        this("Base Scoreboard");
    }

    public ScoreboardHandler(String displayName) {
        this(displayName, 0, false);
    }

    public ScoreboardHandler(int delay) {
        this("Base Scoreboard", delay, true);
    }

    public ScoreboardHandler(String title, int delay) {
        this(title, delay, true);
    }

    public ScoreboardHandler(String displayName, int delay, boolean autoUpdate) {
        this.displayName = displayName;
        this.delay = delay;
        this.autoUpdate = autoUpdate;
    }

    private Player getCurrentPlayer() {
        return currentPlayer == null ? null : Bukkit.getPlayer(currentPlayer);
    }

    public boolean isAutoUpdating() {
        return autoUpdate;
    }

    public final void setDisplayName(String name) {
        this.displayName = name;
    }

    public final String getDisplayName() {
        return this.displayName;
    }

    protected final void setScore(String score) {
        if (currentPlayer != null) {
            if (currentLine <= 0) throw new RuntimeException("Max number of lines on scoreboard reached. (" + colors.size() + ")");
            Objective objective = currentLocalScoreboard.getObjective(OBJECTIVE_NAME);
            objective.getScore(colors.get(currentLine).toString()).setScore(currentLine);
            String prefix = "", suffix = "";
            if (score != null) {
                score = StringUtils.format(score);
                int end = 16;

                if (score.length() > end) {
                    prefix = score.substring(0, end);
                    final int cutIndex = prefix.charAt(prefix.length()-1) == ChatColor.COLOR_CHAR ? end + 1 : end;
                    if(cutIndex == end + 1) prefix = score.substring(0, end - 1);
                    String after = score.substring(cutIndex);
                    boolean attachColor = !after.trim().startsWith(ChatColor.COLOR_CHAR+"") || "abcdef0123456789".indexOf(after.trim().charAt(1)) == -1;
                    suffix = (attachColor ? ChatColor.RESET : "") +
                            (attachColor ? ChatColor.getLastColors(score.substring(0, cutIndex)) : "") + score.substring(cutIndex);
                    if (suffix.length() > end) {
                        suffix = suffix.substring(0, end);
                    }
                } else prefix = score;
            }
            Team team = objective.getScoreboard().getTeam("BoardLine"+currentLine);
            if(team != null) {
                team.setPrefix(prefix);
                team.setSuffix(suffix);
            }

            currentLine--;
        }
    }

    public final void update(Player player) {
        currentPlayer = player.getUniqueId();
        currentLocalScoreboard = player.getPlayer().getScoreboard();
        currentLine = 15;

        Objective objective = currentLocalScoreboard.getObjective(OBJECTIVE_NAME);
        if (objective == null) {
            objective = currentLocalScoreboard.registerNewObjective(OBJECTIVE_NAME, "dummy");
            objective.setDisplayName(StringUtils.format(displayName));
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            for (int i = 0; i < colors.size(); i++) {
                final ChatColor color = colors.get(i);

                org.bukkit.scoreboard.Team team = currentLocalScoreboard.getTeam("BoardLine" + i);
                if (team != null) team.unregister();
                team = currentLocalScoreboard.registerNewTeam("BoardLine" + i);

                team.addEntry(color.toString());
            }
        }

        build(getCurrentPlayer());

        if (!objective.getDisplayName().equals(StringUtils.format(displayName))) {
            objective.setDisplayName(StringUtils.format(displayName));
        }

        for (org.bukkit.scoreboard.Team team : currentLocalScoreboard.getTeams()) {
            try {
                if(!team.getName().contains("BoardLine")) continue;
                int num = Integer.parseInt(team.getName().replace("BoardLine", ""));
                if (num > currentLine) continue;

                team.setPrefix("");
                team.setSuffix("");
                for (String score : team.getEntries()) currentLocalScoreboard.resetScores(score);
            } catch (NumberFormatException ex) {
                team.unregister();
                ex.printStackTrace();
            }
        }
    }

    protected abstract void build(Player p);


}
