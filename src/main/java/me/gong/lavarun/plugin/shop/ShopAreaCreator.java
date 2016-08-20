package me.gong.lavarun.plugin.shop;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopAreaCreator {
    private final CreationListener listener;
    private ShopAreaBuilder builder;
    private UUID player;
    private List<Stage> stages;
    private int stageIndex;
    private String team;
    
    public ShopAreaCreator(CreationListener listener, String team, Player player) {
        this.listener = listener;
        this.team = team;
        this.player = player.getUniqueId();
        this.builder = new ShopAreaBuilder();
        generateStages();
        player.sendMessage(ChatColor.GOLD+"Began shop area creation for "+team+". "+ChatColor.GREEN+stages.get(0).getInstruction());
    }

    private void generateStages() {
        stages = new ArrayList<>();
        stages.add(new Stage("Right click on the pressure plate used to enter the shop area", "Set the enter location", (c, l, f) -> {
            Block b = l.getBlock();
            if(!b.getType().name().contains("PLATE")) return "Must click on a pressure plate";
            else c.builder.setEnterArea(l);
            return null;
        }));
        stages.add(new Stage("Right click on the pressure plate used to exit the shop area", "Set the exit location", (c, l, f) -> {
            Block b = l.getBlock();
            if(!b.getType().name().contains("PLATE")) return "Must click on a pressure plate";
            else c.builder.setExitArea(l);
            return null;
        }));
        stages.addAll(InManager.get().getInstance(PowerupManager.class).getPowerups().stream()
                .map(this::generateStage)
                .collect(Collectors.toList()));
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(player);
    }
    
    private Stage generateStage(Powerup powerup) {
        String fT = "the "+powerup.getName()+" powerup. ["+team+"]";
        return new Stage("Right click "+ powerup.getName()+"'s purchase location" + fT, "Set the purchase location for " + fT, (c, location, face) -> {
            Block b = location.getBlock();
            if(!b.getType().name().contains("PLATE")) return "Must click on a pressure plate";
            else c.builder.addPowerupLocation(powerup.getName(), location);
            return null;
        });
    }
    
    public void onClick(Location location, BlockFace face) {
        Stage next = stageIndex != stages.size() - 1 ? stages.get(1 + stageIndex) : null;
        Stage stage = stages.get(stageIndex);
        String error = stage.getDirections().doClick(this, location, face);
        if(error == null || !error.isEmpty()) getPlayer().sendMessage("");

        if (error != null) {
            if (!error.isEmpty()) getPlayer().sendMessage(ChatColor.RED + error);
        } else {
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f);
            String msg = ChatColor.GOLD.toString();
            if(!stage.getFinish().isEmpty()) msg += stage.getFinish();
            if(next != null && !next.getInstruction().isEmpty()) msg += (ChatColor.stripColor(msg).isEmpty() ? "" : " ")+ChatColor.GREEN+next.getInstruction();
            if(!ChatColor.stripColor(msg).isEmpty()) getPlayer().sendMessage(msg);
            if (next != null) {
                stage = next;
                stageIndex++;
                stage.onSet();
            } else listener.onCreation(builder.build());
        }
    }
    
    private class Stage {
        private String instruction, finish;
        private Directions directions;
        private SetListener setListener;

        public Stage(String instruction, String finish, Directions directions, SetListener setListener) {
            this.instruction = instruction;
            this.finish = finish;
            this.directions = directions;
            this.setListener = setListener;
        }

        public Stage(String instruction, String finish, Directions directions) {
            this(instruction, finish, directions, null);
        }

        public String getInstruction() {
            return instruction;
        }

        public String getFinish() {
            return finish;
        }

        public Directions getDirections() {
            return directions;
        }

        public void onSet() {
            if(setListener != null) setListener.onSet();
        }
    }

    public interface Directions {
        String doClick(ShopAreaCreator c, Location location, BlockFace face);
    }

    public interface SetListener {
        void onSet();
    }

    public interface CreationListener {
        void onCreation(ShopArea area);
    }
}
