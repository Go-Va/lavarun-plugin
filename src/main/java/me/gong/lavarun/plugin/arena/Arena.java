package me.gong.lavarun.plugin.arena;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.PreventBreakEvent;
import me.gong.lavarun.plugin.powerup.Powerup;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.util.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class Arena {
    /*
    two play buttons for each team

    team spawns
    team kit
    team capture location

    food location
     */

    private String name;
    private List<Location> foodSpawn, fireworkSpawns;
    private Team blue, red;
    private Region playArea, lavaRegion, foodRegion;
    private Location teamChooseLocation;

    private List<GameManager.RespawnData> respawnData;

    public Arena(String name, List<Location> foodSpawn, List<Location> fireworkSpawn, Location teamChooseLocation, Region playArea, Region lavaRegion, Region foodRegion, Team blue, Team red) {
        this.name = name;
        this.fireworkSpawns = fireworkSpawn;
        this.playArea = playArea;
        this.foodSpawn = foodSpawn;
        this.teamChooseLocation = teamChooseLocation;
        this.lavaRegion = lavaRegion;
        this.foodRegion = foodRegion;
        this.blue = blue;
        this.red = red;
        this.respawnData = new CopyOnWriteArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Location> getFoodSpawns() {
        return foodSpawn;
    }

    public List<Location> getFireworkSpawns() {
        return fireworkSpawns;
    }

    public boolean canFoodSpawn() {

        foods: for (Location location : foodSpawn) {
            for(Entity e : playArea.getWorld().getEntities())
                if (e instanceof Item) if (locationContainsEntity(location, e.getLocation())) continue foods;
            return true;
        }

        return false;
    }

    public int getAvailableFoodSpawnAmount() {
        int amount = 0;
        foods: for (Location location : foodSpawn) {
            for(Entity e : playArea.getWorld().getEntities())
                if (e instanceof Item) if (locationContainsEntity(location, e.getLocation())) continue foods;
            amount++;
        }

        return amount;
    }

    public Region getLavaRegion() {
        return lavaRegion;
    }

    public void resetArena(boolean fullReset, boolean playing) {
        lavaRegion.getBlocks().forEach(l -> l.getBlock().setType(Material.STATIONARY_LAVA, false));

        playArea.getBlocks().forEach(l -> {
            Block b = l.getBlock();
            if(b.getTypeId() == Material.STAINED_GLASS.getId()) b.setType(Material.AIR);
        });

        if(fullReset) {
            respawnData.clear();
            playArea.getWorld().getEntities().stream()
                    .filter(e -> e instanceof Item)
                    .forEach(Entity::remove);
            if(playing) {
                red.getShopArea().spawnEntities(red);
                blue.getShopArea().spawnEntities(blue);
            }
        }
    }

    public GameManager.RespawnData getRespawnData(UUID uuid) {
        return respawnData.stream().filter(s -> s.getUUID().equals(uuid)).findFirst().orElse(null);
    }

    public GameManager.RespawnData getRespawnData(Player player) {
        return getRespawnData(player.getUniqueId());
    }

    public GameManager.RespawnData createRespawnData(Player player, boolean isPlayer) {
        GameManager.RespawnData d;
        boolean add = false;
        if((d = getRespawnData(player)) == null) {
            add = true;
            d = new GameManager.RespawnData(player.getUniqueId(), isPlayer);
        } else d.setInit(System.currentTimeMillis());
        if(add) respawnData.add(d);
        return d;
    }

    public List<GameManager.RespawnData> getRespawnData() {
        return respawnData;
    }

    public void removeRespawnData(UUID player) {
        respawnData.remove(getRespawnData(player));
    }

    public boolean canBeDamaged(Player victim, Player attacker, boolean teams) {
        if(!isPlaying(victim, false) || !isPlaying(attacker, false)) return false;
        if(getTeam(victim).equals(getTeam(attacker)) && teams) return false;
        if(foodRegion.contains(victim) || foodRegion.contains(attacker)) {
            return false;
        }
        if(!playArea.contains(victim)) {
            if(playArea.contains(attacker)) return false;

            Location kitRed = getRed().getShopArea().getEnterArea(), kitBlue = getBlue().getShopArea().getEnterArea();

            if (victim.getLocation().distance(kitRed) > victim.getLocation().distance(kitBlue)) {
                //closer to blue
                if (getTeam(victim).equals(getBlue())) return false;
            } else if (getTeam(victim).equals(getRed())) return false;
        }

        return true;
    }

    public boolean canBeDamaged(Player victim, Player attacker) {
        return canBeDamaged(victim, attacker, true);
    }

    public boolean isBlockInteractable(Location location, ItemStack used, Player player, boolean isBreak) {
        if(!playArea.contains(location) || !isPlaying(player, true)) return false;
        Block block = location.getBlock();
        if(isBreak && (block.getType() != Material.STAINED_GLASS || getTeam(player).getGlassColor() != block.getData())) {
            PreventBreakEvent pB = new PreventBreakEvent(player, block, this, false);
            Bukkit.getPluginManager().callEvent(pB);
            if (!pB.isCancelled()) return false;
        }

        AxisAlignedBB b = new AxisAlignedBB(location.getX(), location.getY(), location.getZ(),
                location.getX() + 1, location.getY() + 1, location.getZ() + 1);

        return isBreak || !(used != null && (used.getType() != Material.STAINED_GLASS ||
                getTeam(player).getGlassColor() != used.getDurability() ||
                player.getWorld().getEntities().stream().anyMatch(e -> {
                            if (e instanceof ItemFrame) {
                                AxisAlignedBB a = new AxisAlignedBB(e.getLocation().getX() - 0.1, e.getLocation().getY() - 0.1, e.getLocation().getZ() - 0.1,
                                        e.getLocation().getX() + 0.1, e.getLocation().getY() + 0.3, e.getLocation().getZ() + 0.1);
                                return a.intersectsWith(b);
                            }
                            return false;
                        }
                )));
    }

    public Region getFoodRegion() {
        return foodRegion;
    }

    public Location getFoodSpawn() {
        if(!canFoodSpawn()) {
            return null;
        }
        List<Location> valids = foodSpawn.stream().filter(f -> !locationHasFood(f)).collect(Collectors.toList());
        return valids.get(NumberUtils.random.nextInt(valids.size()));
    }

    public boolean locationContainsEntity(Location f, Location entity) {
        AxisAlignedBB b = new AxisAlignedBB(f.getX(), f.getY(), f.getZ(), f.getX() + 1, f.getY() + 2, f.getZ() + 1);
        AxisAlignedBB a = new AxisAlignedBB(entity.getX()-0.1, entity.getY()-0.1, entity.getZ()-0.1, entity.getX() + 0.1, entity.getY()+0.3, entity.getZ()+0.1);
        return b.intersectsWith(a);
    }

    private boolean locationHasFood(Location f) {
        for(Entity e : playArea.getWorld().getEntities()) {
            if(e instanceof Item) {
                if(locationContainsEntity(f, e.getLocation())) return true;
            }
        }
        return false;
    }

    public void spawnFireworks(Team team) {
        new BukkitRunnable() {

            private int am = -1;
            @Override
            public void run() {
                am++;
                if(am == 3) return;
                if(am >= 5) {
                    cancel();
                    return;
                }
                for(Location fs : fireworkSpawns) {
                    FireworkUtils.spawnFirework(fs.clone().add(0.5, 0, 0.5), FireworkUtils.getEffect(true, team.getName().equalsIgnoreCase("Red") ?
                            Color.RED : Color.AQUA, team.getName().equalsIgnoreCase("Red") ? Color.ORANGE : Color.BLUE, FireworkEffect.Type.STAR, true), 1);
                }
            }
        }.runTaskTimer(InManager.get().getInstance(Main.class), 0, 20);
    }

    public Team getBlue() {
        return blue;
    }

    public Team getRed() {
        return red;
    }

    public void resetTeams() {
        blue.resetTeam();
        red.resetTeam();
    }

    public void removePlayer(Player player) {
        blue.removePlayer(player);
        red.removePlayer(player);
        player.teleport(teamChooseLocation);
        player.getInventory().clear();
        player.setFireTicks(0);
        player.setFoodLevel(20);
        player.setSaturation(5);
    }

    public boolean isPlaying(Player player, boolean ignoreRespawning) {
        return (blue.isOnTeam(player) || red.isOnTeam(player)) && ignoreRespawning || getRespawnData(player) == null;
    }

    public Team getTeam(Player player) {
        return isPlaying(player, true) ? blue.isOnTeam(player) ? blue : red : null;
    }

    public Team getTeam(String name) {
        switch (name.toLowerCase().replace(" ", "")) {
            case "blue":
                return blue;
            case "red":
                return red;
            default: return null;
        }
    }

    public Region getPlayArea() {
        return playArea;
    }

    public Location getTeamChooseLocation() {
        return teamChooseLocation;
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("name", name);
        JSONArray food = new JSONArray();
        foodSpawn.forEach(f -> food.add(JSONUtils.locationToJSON(f)));
        JSONArray fw = new JSONArray();
        fireworkSpawns.forEach(f -> fw.add(JSONUtils.locationToJSON(f)));
        obj.put("foodSpawn", food);
        obj.put("fireworkSpawns", fw);
        obj.put("blue", blue.toJSON());
        obj.put("red", red.toJSON());
        obj.put("playArea", playArea.toJSON());
        obj.put("lavaRegion", lavaRegion.toJSON());
        obj.put("foodRegion", foodRegion.toJSON());
        obj.put("teamChooseLocation", JSONUtils.locationToJSON(teamChooseLocation));
        return obj;
    }

    public static Arena fromJSON(JSONObject obj) {
        String name = (String) obj.get("name");
        Location teamChooseLocation = JSONUtils.locationFromJSON((JSONObject) obj.get("teamChooseLocation"));
        Team blue = Team.fromJSON((JSONObject) obj.get("blue")),
                red = Team.fromJSON((JSONObject) obj.get("red"));
        Region playArea = Region.fromJSON((JSONObject) obj.get("playArea")), lavaReigon = Region.fromJSON((JSONObject) obj.get("lavaRegion")),
                foodRegion = Region.fromJSON((JSONObject) obj.get("foodRegion"));
        JSONArray food = (JSONArray) obj.get("foodSpawn");
        List<Location> foodSpawn = (List<Location>) food.stream().map(s -> JSONUtils.locationFromJSON((JSONObject)s)).collect(Collectors.toList());
        JSONArray fw = (JSONArray) obj.get("fireworkSpawns");
        List<Location> fireworkSpawns = (List<Location>) fw.stream().map(s -> JSONUtils.locationFromJSON((JSONObject)s)).collect(Collectors.toList());

        return new Arena(name, foodSpawn, fireworkSpawns, teamChooseLocation, playArea, lavaReigon, foodRegion, blue, red);

    }

    public List<Player> getPlaying(boolean ignoreRespawning) {
        return Bukkit.getOnlinePlayers().stream().filter(p -> isPlaying(p, ignoreRespawning)).collect(Collectors.toList());
    }
}
