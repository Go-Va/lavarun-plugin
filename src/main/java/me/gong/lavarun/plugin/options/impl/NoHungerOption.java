package me.gong.lavarun.plugin.options.impl;

import me.gong.lavarun.plugin.options.Option;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class NoHungerOption extends Option {

    @EventHandler
    public void onHunger(FoodLevelChangeEvent ev) {
        if(isEnabled() && getGame().isInGame()) {
            ev.setCancelled(true);
            ev.setFoodLevel(20);
            if(ev.getEntity() instanceof Player) ((Player)ev.getEntity()).setSaturation(15);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(isEnabled() && getGame().isInGame()) getGame().getCurrentArena().getPlaying(true).forEach(p -> {
            p.setFoodLevel(20);
            p.setSaturation(15);
        });
    }

    @Override
    public String getHelp() {
        return "Disables hunger ingame";
    }

    @Override
    public String getName() {
        return "NoHunger";
    }
}
