package me.gong.lavarun.plugin.arena.team;

import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.shop.ShopArea;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamBuilder {
    private Location playButton, spawnLocation;
    private Region captureRegion;
    private String name;
    private ShopArea shopArea;

    private ChatColor color;
    private short glassColor;
    private UUID player;

    public TeamBuilder(String name, ChatColor color, short glassColor, Player player) {
        this.name = name;
        this.color = color;
        this.glassColor = glassColor;
        this.player = player.getUniqueId();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public void setPlayButton(Location playButton) {
        this.playButton = playButton;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public void setCaptureRegion(Region captureRegion) {
        this.captureRegion = captureRegion;
    }

    public void setShopArea(ShopArea shopArea) {
        this.shopArea = shopArea;
    }

    public String getName() {
        return name;
    }

    public Team build() {
        return new Team(name, color, glassColor, playButton, spawnLocation, captureRegion, shopArea);
    }
}
