package me.gong.lavarun.plugin.game;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.ArenaCreator;
import me.gong.lavarun.plugin.command.Cmd;
import me.gong.lavarun.plugin.command.annotation.Command;
import me.gong.lavarun.plugin.command.annotation.SubCommand;
import me.gong.lavarun.plugin.scoreboard.ScoreboardHandler;
import me.gong.lavarun.plugin.scoreboard.ScoreboardManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ArenaCmd implements Cmd {

    @Command(name = "game", help = "Main game command", alias = "g")
    public boolean onCmd(Player player, String[] args) {
        return !hasPerms(player);
    }

    @SubCommand(name = "createarena", alias = "ca", help = "Creates an arena", syntax = "<name>", centralCommand = "game")
    public boolean onCreate(Player player, String[] args) {
        if(hasPerms(player)) {
            if (args.length != 1) return false;
            ArenaCreator.beginPlayer(player, args[0], new ArenaCreator.CreationListener() {
                @Override
                public void onCreation(Arena arena) {
                    player.sendMessage(ChatColor.GOLD + "Arena '" + arena.getName() + "' created");
                    InManager.get().getInstance(GameManager.class).addArena(arena);
                }

                @Override
                public void onFail() {
                    player.sendMessage(ChatColor.GOLD + "Unable to create arena.");
                }
            });
            return true;
        } else return false;
    }

    @SubCommand(name = "load", alias = "l", help = "Loads an arena", syntax = "<arena>", centralCommand = "game")
    public boolean onLoad(Player player, String[] args) {
        if(hasPerms(player)) {
            if(args.length != 1) return false;
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Arena a = gm.getArenas().stream()
                    .filter(ar -> ar.getName().equalsIgnoreCase(args[0]))
                    .findFirst().orElse(null);
            if(a == null) {
                player.sendMessage(ChatColor.RED+"Invalid arena. Valids: "+
                        gm.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
            } else {
                gm.loadArena(a);
                player.sendMessage(ChatColor.GREEN+"Arena '"+a.getName()+"' loaded.");
            }
            return true;
        } else return true;
    }

    @SubCommand(name = "end", alias = "e", help = "Ends the game", centralCommand = "game")
    public boolean onEnd(Player player, String[] args) {
        if(hasPerms(player)) {
            InManager.get().getInstance(GameManager.class).stopGame(player);
            return true;
        } else return false;
    }

    @SubCommand(name = "remove", alias = "r", help = "Removes an arena", syntax = "<arena>", centralCommand = "game")
    public boolean onRemove(Player player, String[] args) {
        if(hasPerms(player)) {
            if(args.length != 1) {

                return false;
            }
            GameManager gm = InManager.get().getInstance(GameManager.class);
            Arena a = gm.getArenas().stream()
                    .filter(ar -> ar.getName().equalsIgnoreCase(args[0]))
                    .findFirst().orElse(null);
            if(a == null) {
                player.sendMessage(ChatColor.RED+"Invalid arena. Valids: "+
                        gm.getArenas().stream().map(Arena::getName).collect(Collectors.toList()));
            } else gm.removeArena(player, a);
            return true;
        } else return true;
    }

    @SubCommand(name = "test", alias = "t", help = "Tests random features", centralCommand = "game")
    public boolean onTest(Player player, String[] args) {
        if(hasPerms(player)) {
            GameManager gm = InManager.get().getInstance(GameManager.class);
            if(!gm.getCurrentArena().isPlaying(player, false)) player.sendMessage(ChatColor.RED+"You are not ingame");
            else {
                ShopManager sm = InManager.get().getInstance(ShopManager.class);
                sm.setPoints(player, 1000);
            }
            return true;
        } else return true;
    }


}
