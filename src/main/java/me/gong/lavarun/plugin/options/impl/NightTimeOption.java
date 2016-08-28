package me.gong.lavarun.plugin.options.impl;

import me.gong.lavarun.plugin.game.events.GameBeginEvent;
import me.gong.lavarun.plugin.game.events.GameEndEvent;
import me.gong.lavarun.plugin.options.Option;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

public class NightTimeOption extends Option {
    @Override
    public String getHelp() {
        return "Makes it night time";
    }

    @Override
    public String getName() {
        return "NightTime";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(!getGame().isInGame()) return;
        if(enabled) setNight();
        else setDay();
    }

    @EventHandler
    public void gameEnd(GameEndEvent ev) {
        updateState();
    }

    @EventHandler
    public void gameStart(GameBeginEvent ev) {
        updateState();
    }

    public void updateState() {
        if(isEnabled()) setNight();
        else setDay();
    }

    public void setNight() {
        getGame().getCurrentArena().getPlayArea().getWorld().setTime(16000);
    }

    public void setDay() {
        getGame().getCurrentArena().getPlayArea().getWorld().setTime(5000);
    }
}
