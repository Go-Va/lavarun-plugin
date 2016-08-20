package me.gong.lavarun.plugin.arena;

import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.region.Region;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ArenaBuilder {

    private UUID player;
    private String name;


    private Location teamChooseLocation;
    private List<Location> foodSpawn, fireworkSpawns;
    private Team blue, red;
    private Region playArea, lavaRegion, foodRegion;

    public ArenaBuilder(Player player, String name) {
        this.player = player.getUniqueId();
        this.name = name;
        this.foodSpawn = new ArrayList<>();
        this.fireworkSpawns = new ArrayList<>();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addFoodSpawn(Location foodSpawn) {
        this.foodSpawn.add(foodSpawn);
    }

    public void addFireworkSpawn(Location fireworkSpawn) {
        this.fireworkSpawns.add(fireworkSpawn);
    }

    public boolean containsFoodLocation(Location location) {
        return foodSpawn.contains(location);
    }

    public boolean containsFireworkLocation(Location location) {
        return fireworkSpawns.contains(location);
    }

    public void setTeamChooseLocation(Location teamChooseLocation) {
        this.teamChooseLocation = teamChooseLocation;
    }

    public void setBlue(Team blue) {
        this.blue = blue;
    }

    public void setRed(Team red) {
        this.red = red;
    }

    public void setPlayArea(Region playArea) {
        this.playArea = playArea;
    }

    public void setLavaRegion(Region lavaRegion) {
        this.lavaRegion = lavaRegion;
    }

    public void setFoodRegion(Region foodRegion) {
        this.foodRegion = foodRegion;
    }

    public Region getLavaRegion() {
        return lavaRegion;
    }

    public Region getPlayArea() {
        return playArea;
    }

    public Region getFoodRegion() {
        return foodRegion;
    }

    public Arena build() {
        return new Arena(name, foodSpawn, fireworkSpawns, teamChooseLocation, playArea, lavaRegion, foodRegion, blue, red);
    }

}
