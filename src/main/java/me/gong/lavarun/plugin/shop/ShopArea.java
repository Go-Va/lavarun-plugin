package me.gong.lavarun.plugin.shop;

import javafx.util.Pair;
import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.BlockUtils;
import me.gong.lavarun.plugin.util.JSONUtils;
import me.gong.lavarun.plugin.util.Vec3d;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopArea {

    private Location enterArea, exitArea;

    private Map<String, Location> powerupPurchase;
    private Map<UUID, Boolean> pressurePlateState;
    private Map<UUID, Powerup> powerupState;

    public ShopArea(Location enterArea, Location exitArea, Map<String, Location> powerupPurchase) {
        this.enterArea = enterArea;
        this.exitArea = exitArea;
        this.pressurePlateState = new HashMap<>();
        this.powerupState = new HashMap<>();
        this.powerupPurchase = powerupPurchase;
    }

    public Location getEnterArea() {
        return enterArea;
    }

    public Location getExitArea() {
        return exitArea;
    }

    public Map<Powerup, Location> getPowerupPurchase() {
        Map<Powerup, Location> ret = new HashMap<>();
        powerupPurchase.entrySet().forEach(e -> ret.put(getPowerupManager().getPowerup(e.getKey()), e.getValue()));
        return ret;
    }
    
    public Powerup getStandingOnPowerup(Player player) {
        Map.Entry<Powerup, Location> entry = getPowerupPurchase().entrySet().stream()
                .filter(p -> isStandingOn(player, p.getValue()))
                .findFirst().orElse(null);
        return entry != null ? entry.getKey() : null;
    }

    public Location getPowerupLocation(Powerup powerup) {
        return getPowerupPurchase().get(powerup);
    }
    
    public boolean isStandingOn(Player player, Location location) {

        Location at = player.getLocation();
        AxisAlignedBB b = new AxisAlignedBB(location.getX(), location.getY(), location.getZ(),
                location.getX() + 1, location.getY() + 2, location.getZ() + 1);
        return b.isVecAlmostInside(new Vec3d(at.getX(), at.getY(), at.getZ()));
    }

    public boolean isStandingOnEnter(Player player) {
        return isStandingOn(player, enterArea);
    }

    public boolean isStandingOnExit(Player player) {
        return isStandingOn(player, exitArea);
    }

    public void update(Player player) {
        boolean enter = isStandingOnEnter(player), exit = isStandingOnExit(player);

        if(enter || exit) {
            if (pressurePlateState.containsKey(player.getUniqueId())) {
                pressurePlateState.put(player.getUniqueId(), !enter);
            } else if (enter) {
                pressurePlateState.put(player.getUniqueId(), false);
                player.teleport(createTeleport(exitArea, player));
            } else {
                pressurePlateState.put(player.getUniqueId(), true);
                player.teleport(createTeleport(enterArea, player));
            }
            powerupState.remove(player.getUniqueId());
        } else {
            pressurePlateState.remove(player.getUniqueId());

            Powerup last = powerupState.get(player.getUniqueId()), now = getStandingOnPowerup(player);
            if(now == null) powerupState.remove(player.getUniqueId());
            else if(last == null || !last.getName().equals(now.getName())) {
                powerupState.put(player.getUniqueId(), now);
                Location location = powerupPurchase.get(now.getName());
                BlockUtils.outlineBox(new AxisAlignedBB(location.getX(), location.getY(), location.getZ(),
                        location.getX() + 1, location.getY() + 2, location.getZ() + 1), player.getWorld())
                        .forEach(l -> player.getWorld().spawnParticle(Particle.FLAME, l, 1, 0.0, 0.0, 0.0, 0.0));
                if(getShopManager().getPoints(player) >= now.getCost()) {
                    getShopManager().removePoints(player, now.getCost());
                    if (now.getName().equalsIgnoreCase("resupply")) {

                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 2.0f, 2.0f);
                        InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player).giveKit(player);
                        player.sendMessage(ChatColor.GOLD+"Inventory restocked");
                    } else {
                        getPowerupManager().givePlayerPowerup(player, now);
                        player.sendMessage(ChatColor.GOLD + "You have bought the " + ChatColor.YELLOW + now.getName() + ChatColor.GOLD + " powerup.");
                    }
                } else player.sendMessage(ChatColor.RED+"You don't have enough points to purchase "+ChatColor.YELLOW+now.getName()+" ["+now.getCost()+"]");
            }
        }
    }

    private Location createTeleport(Location location, Player player) {
        Location mE = location.clone();
        mE.add(0.5, 0, 0.5);
        mE.setYaw(player.getLocation().getYaw());
        mE.setPitch(player.getLocation().getPitch());
        return mE;
    }

    public void resetStates() {
        pressurePlateState.clear();
        powerupState.clear();
    }

    public void removeState(Player player) {
        pressurePlateState.remove(player.getUniqueId());
        powerupState.remove(player.getUniqueId());
    }

    public void spawnEntities(Team team) {
        powerupPurchase.entrySet().forEach(entry -> {
            Location l = entry.getValue();
            Powerup powerup = getPowerupManager().getPowerup(entry.getKey());
            Item e = l.getWorld().dropItem(l.clone().add(0.5, 0, 0.5), powerup.getItem(team));
            e.setCustomName(e.getItemStack().getItemMeta().getDisplayName()+" ["+powerup.getCost()+"]");
            e.setCustomNameVisible(true);
            e.setVelocity(new Vector());
        });
    }

    public JSONObject toJSON() {
        JSONObject ret = new JSONObject();
        ret.put("enter", JSONUtils.locationToJSON(enterArea));
        ret.put("exit", JSONUtils.locationToJSON(exitArea));
        JSONArray arr = new JSONArray();
        powerupPurchase.entrySet().forEach(e -> arr.add(powerupToJSON(e)));
        ret.put("powerups", arr);
        return ret;
    }

    public static ShopArea fromJSON(JSONObject object) {
        Location enter = JSONUtils.locationFromJSON((JSONObject) object.get("enter")),
                exit = JSONUtils.locationFromJSON((JSONObject) object.get("exit"));
        Map<String, Location> powers = new HashMap<>();
        JSONArray powerups = (JSONArray) object.get("powerups");
        for(Object o : powerups) {
            Pair<String, Location> power = jsonToPowerup((JSONObject) o);
            powers.put(power.getKey(), power.getValue());
        }
        return new ShopArea(enter, exit, powers);
    }

    private JSONObject powerupToJSON(Map.Entry<String, Location> powerup) {
        JSONObject ret = new JSONObject();
        ret.put("powerup", powerup.getKey());
        ret.put("location", JSONUtils.locationToJSON(powerup.getValue()));
        return ret;
    }

    private static Pair<String, Location> jsonToPowerup(JSONObject obj) {
        return new Pair<>((String) obj.get("powerup"), JSONUtils.locationFromJSON((JSONObject) obj.get("location")));
    }

    private PowerupManager getPowerupManager() {
        return InManager.get().getInstance(PowerupManager.class);
    }

    private ShopManager getShopManager() {
        return InManager.get().getInstance(ShopManager.class);
    }
}
