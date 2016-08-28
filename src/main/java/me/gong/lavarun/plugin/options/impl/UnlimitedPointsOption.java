package me.gong.lavarun.plugin.options.impl;

import me.gong.lavarun.plugin.game.events.GameBeginEvent;
import me.gong.lavarun.plugin.game.events.PlayerJoinTeamEvent;
import me.gong.lavarun.plugin.options.Option;
import me.gong.lavarun.plugin.shop.events.SetPointsEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

public class UnlimitedPointsOption extends Option {
    @Override
    public String getHelp() {
        return "Makes everyone have unlimited points";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(getGame().isInGame())
            for(Player p : getGame().getCurrentArena().getPlaying(true))
                getShop().setPoints(p, isEnabled() ? 1000 : 0);
    }

    @EventHandler
    public void onLosePoints(SetPointsEvent ev) {
        if(isEnabled() && getGame().isInGame()) ev.setAmount(1000);
    }

    @EventHandler
    public void gameBegin(GameBeginEvent ev) {
        if(isEnabled()) for(Player p : getGame().getCurrentArena().getPlaying(true)) getShop().setPoints(p, 1000);
    }

    @EventHandler
    public void onJoinTeam(PlayerJoinTeamEvent ev) {
        if(isEnabled()) getShop().setPoints(ev.getPlayer(), 1000);
    }

    @Override
    public String getName() {
        return "UnlimitedPoints";
    }
}
