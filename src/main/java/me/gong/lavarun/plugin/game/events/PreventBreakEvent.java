package me.gong.lavarun.plugin.game.events;

import me.gong.lavarun.plugin.arena.Arena;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PreventBreakEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled;
    private Player player;
    private Block block;
    private Arena arena;
    private boolean isBreak;

    public PreventBreakEvent(Player player, Block block, Arena arena, boolean isBreak) {
        this.player = player;
        this.arena = arena;
        this.block = block;
        this.isBreak = isBreak;
    }

    public Player getPlayer() {
        return player;
    }

    public Arena getArena() {
        return arena;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isBreak() {
        return isBreak;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
