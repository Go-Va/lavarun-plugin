package me.gong.lavarun.plugin.options.impl;

import me.gong.lavarun.plugin.game.events.GameBeginEvent;
import me.gong.lavarun.plugin.game.events.GameEndEvent;
import me.gong.lavarun.plugin.game.events.PlayerJoinTeamEvent;
import me.gong.lavarun.plugin.options.Option;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpeedOption extends Option {
    @Override
    public String getHelp() {
        return "Makes everyone speedy";
    }

    @Override
    public String getName() {
        return "Speed";
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(isEnabled() && getGame().isInGame()) giveAllSpeed();
        if(!enabled && getGame().isInGame()) removeAllSpeed();
    }

    @EventHandler
    public void onStart(GameBeginEvent ev) {
        if(isEnabled() && getGame().isInGame()) giveAllSpeed();
    }

    @EventHandler
    public void onEnd(GameEndEvent ev) {
        removeAllSpeed();
    }

    @EventHandler
    public void onJoin(PlayerJoinTeamEvent ev) {
        if(isEnabled() && getGame().isInGame()) giveSpeed(ev.getPlayer());
    }

    public void giveAllSpeed() {
        getGame().getCurrentArena().getPlaying(true).forEach(this::giveSpeed);
    }

    public void removeAllSpeed() {
        getGame().getCurrentArena().getPlaying(true).forEach(this::removeSpeed);
    }

    public void giveSpeed(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 1000000, 1, true, true));
    }

    public void removeSpeed(Player player) {
        player.removePotionEffect(PotionEffectType.SPEED);
    }
}
