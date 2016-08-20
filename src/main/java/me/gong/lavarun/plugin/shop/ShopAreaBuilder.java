package me.gong.lavarun.plugin.shop;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class ShopAreaBuilder {
    private Location enterArea, exitArea;

    private Map<String, Location> powerupPurchase;

    public ShopAreaBuilder() {
        powerupPurchase = new HashMap<>();
    }

    public Location getEnterArea() {
        return enterArea;
    }

    public void setEnterArea(Location enterArea) {
        this.enterArea = enterArea;
    }

    public Location getExitArea() {
        return exitArea;
    }

    public void setExitArea(Location exitArea) {
        this.exitArea = exitArea;
    }

    public Map<String, Location> getPowerupPurchase() {
        return powerupPurchase;
    }

    public void addPowerupLocation(String powerup, Location location) {
        powerupPurchase.put(powerup, location);
    }

    public ShopArea build() {
        return new ShopArea(enterArea, exitArea, powerupPurchase);
    }
}
