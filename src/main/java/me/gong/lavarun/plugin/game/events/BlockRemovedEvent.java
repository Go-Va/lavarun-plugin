package me.gong.lavarun.plugin.game.events;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BlockRemovedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Block block;
    private Player whom;
    private boolean manual;

    public BlockRemovedEvent(Block block, Player whom, boolean manual) {
        this.block = block;
        this.whom = whom;
        this.manual = manual;
    }

    public Block getBlock() {
        return block;
    }

    public Player getWhom() {
        return whom;
    }

    public boolean isManual() {
        return manual;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}