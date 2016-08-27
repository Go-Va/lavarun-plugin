package me.gong.lavarun.plugin.game.scoreboard;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.scoreboard.ScoreboardHandler;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

public class InGameBoard extends ScoreboardHandler {

    public InGameBoard() {
        super("&c&lLava &6&lRun &7[InGame]", 20);
    }

    public static final long flashTime = 500, niceTime = 1750;

    @Override
    protected void build(Player p) {
        GameManager m = InManager.get().getInstance(GameManager.class);
        if(!m.isInGame()) return;
        
        setScore(null);
        setScore("&7Playing: &a"+m.getCurrentArena().getName());
        setScore(null);
        boolean blue = !m.getCurrentArena().isPlaying(p, true) || m.getCurrentArena().getTeam(p).getName().equalsIgnoreCase("Blue");
        if(blue) renderBlue(m);
        else renderRed(m);

        setScore(null);

        if(blue) renderRed(m);
        else renderBlue(m);

        if(!m.getCurrentArena().isPlaying(p, true)) setScore(null);

        if(m.getCurrentArena().isPlaying(p, true) && p.getFoodLevel() <= 7) {
            setScore("&l&m"+ StringUtils.repeat("-", 4)+"&c Low Food&r&m"+StringUtils.repeat("-", 12));

        } else {
            setScore("&l&m"+ StringUtils.repeat("-", 24));
        }

        if(m.getCurrentArena().isPlaying(p, true)) {
            String color = "&";
            int amount = (m.getCurrentArena().getFoodSpawns().size() - m.getCurrentArena().getAvailableFoodSpawnAmount()),
                    outOf = m.getCurrentArena().getFoodSpawns().size();
            double percent = (amount * 1.0 / outOf) * 100d;
            if(percent < 50) color += "c";
            else if(percent == 100) color += "a";
            else color += "e";
            setScore("&6Food stock: "+color+amount+"&6/&a"+outOf+" ");
            setScore("&6Your points: &e"+p.getLevel()); //TODO replace with stored system
        } else setScore("&7&l Currently spectating");
    }

    public void renderBlue(GameManager m) {
        int left = 3 - m.getBlueCaptures();
        boolean recentCap = m.getTimeSinceBlueCap() <= niceTime;
        setScore("&b&lBlue Team: "+(recentCap ? "NICE!" : ""));
        String caps = StringUtils.repeat("✔ ", m.getBlueCaptures()) +StringUtils.repeat("✘ ", left),
                color = m.getTimeSinceRedCap() <= flashTime ? "&2" : "&a";
        setScore("&3 "+caps+color+"["+m.getBlueCaptures()+"/3]");
        if (m.getRedCaptureState() > 0) {
            int stateLeft = 5 - m.getRedCaptureState();
            setScore("  &d&l[!]&d Cap alert! &l[!]");
            setScore("        &a&l"+StringUtils.repeat("=", m.getRedCaptureState())+"&7&l"+StringUtils.repeat("=", stateLeft));
        }
    }

    public void renderRed(GameManager m) {
        int left = 3 - m.getRedCaptures();
        boolean recentCap = m.getTimeSinceRedCap() <= niceTime;
        setScore("&c&lRed Team: "+(recentCap ? "NICE!" : ""));

        String caps = StringUtils.repeat("✔ ", m.getRedCaptures()) +StringUtils.repeat("✘ ", left),
                color = m.getTimeSinceBlueCap() <= flashTime ? "&2" : "&a";
        setScore("&4 "+caps+color+"["+m.getRedCaptures()+"/3]");
        if (m.getBlueCaptureState() > 0) {
            int stateLeft = 5 - m.getBlueCaptureState();
            setScore("  &d&l[!]&d Cap alert! &l[!]");
            setScore("        &a&l"+StringUtils.repeat("=", m.getBlueCaptureState())+"&7&l"+StringUtils.repeat("=", stateLeft));
        }
    }
}
