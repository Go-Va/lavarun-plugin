package me.gong.lavarun.plugin.game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;

public class DeathEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Player attacker;
    private EntityDamageEvent.DamageCause cause;
    private boolean cancelled;

    public DeathEvent(Player player, EntityDamageEvent.DamageCause cause) {
        this.player = player;
        this.cause = cause;
    }

    public DeathEvent(Player player, Player attacker) {
        this.player = player;
        this.attacker = attacker;
    }

    public boolean wasAttacked() {
        return attacker != null;
    }

    public Player getAttacker() {
        return attacker;
    }

    public EntityDamageEvent.DamageCause getCause() {
        return cause;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
