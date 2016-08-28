package me.gong.lavarun.plugin.game.events;

import me.gong.lavarun.plugin.arena.team.Team;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerJoinTeamEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private Player player;
    private Team team;

    public PlayerJoinTeamEvent(Player player, Team team) {
        this.player = player;
        this.team = team;
    }

    public Player getPlayer() {
        return player;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}