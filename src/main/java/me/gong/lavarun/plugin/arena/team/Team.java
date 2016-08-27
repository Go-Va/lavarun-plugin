package me.gong.lavarun.plugin.arena.team;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.shop.ShopArea;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.JSONUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class Team {
    private Location playButton, spawnLocation;
    private Region captureRegion;
    private String name;

    private List<UUID> players;
    private Map<UUID, Long> lastRespawn;

    private ChatColor color;
    private short glassColor;
    private ShopArea shopArea;

    public Team(String name, ChatColor color, short glassColor,
                Location playButton, Location spawnLocation, Region captureRegion, ShopArea shopArea) {
        this.name = name;
        this.shopArea = shopArea;
        this.color = color;
        this.glassColor = glassColor;
        this.playButton = playButton;
        this.spawnLocation = spawnLocation;
        this.captureRegion = captureRegion;
        this.players = new ArrayList<>();
        this.lastRespawn = new HashMap<>();
    }

    public static ItemStack getEmptySlot() {
        ItemStack s = new ItemStack(Material.THIN_GLASS);
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(ChatColor.GOLD+"No Powerup");
        s.setItemMeta(m);
        return s;
    }

    public void giveKit(Player player) {
        player.getInventory().setItem(0, getSword(player));
        for(int i = 2; i < 9; i++) player.getInventory().setItem(i, getGlass(player));
        if(player.getInventory().getItem(1) == null || player.getInventory().getItem(1).getType() == Material.AIR) player.getInventory().setItem(1, getEmptySlot());
    }

    private ItemStack getSword(Player player) {
        ItemStack s = new ItemStack(Material.IRON_SWORD);
        ItemMeta m = s.getItemMeta();
        m.spigot().setUnbreakable(true);
        m.addEnchant(Enchantment.KNOCKBACK, 1, true);
        m.setDisplayName(color+player.getName()+"'s whacker");
        s.setItemMeta(m);
        return BukkitUtils.create1_8Pvp(s);
    }

    private org.bukkit.scoreboard.Team getOrRegister(Player player) {
        Scoreboard s = player.getScoreboard();
        org.bukkit.scoreboard.Team t;
        if((t = s.getTeam(getName())) == null) {
            t = s.registerNewTeam(getName());
            t.setPrefix(getColor().toString());
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.FOR_OWN_TEAM);

        }
        return t;
    }

    public int getTeamSize() {
        return players.size();
    }

    public ItemStack getBaseGlass() {
        return new ItemStack(Material.STAINED_GLASS, 1, glassColor);
    }

    public ItemStack getGlass(Player player) {
        ItemStack ret = getBaseGlass();
        ret.setAmount(64);
        ItemMeta m = ret.getItemMeta();
        m.setDisplayName(color+player.getName()+"'s Building Blocks");
        ret.setItemMeta(m);
        return ret;
    }

    public Location getPlayButton() {
        return playButton;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public ShopArea getShopArea() {
        return shopArea;
    }

    public boolean canCapture(Player player) {
        return isOnTeam(player) && captureRegion.contains(player.getLocation()) &&
                InManager.get().getInstance(GameManager.class).getCurrentArena().getRespawnData(player) == null;
    }

    public List<Player> getCapturers() {
        return Bukkit.getOnlinePlayers().stream().filter(this::canCapture).collect(Collectors.toList());
    }

    public Region getCaptureRegion() {
        return captureRegion;
    }

    public void addDamageCooldown(Player player) {
        if(!isOnTeam(player)) return;
        lastRespawn.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public long getTimeBetweenLastRespawn(Player player) {
        if(!isOnTeam(player)) return System.currentTimeMillis();
        return lastRespawn.containsKey(player.getUniqueId()) ? System.currentTimeMillis() - lastRespawn.get(player.getUniqueId()) : System.currentTimeMillis();
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public short getGlassColor() {
        return glassColor;
    }

    public boolean isOnTeam(Player player) {
        return players.contains(player.getUniqueId());
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        addToTeam(player.getName());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        lastRespawn.remove(player.getUniqueId());
        removeFromTeam(player.getName());
    }

    public void refeshAdded() {
        players.forEach(u -> {
            Player p = Bukkit.getPlayer(u);
            if(p != null) addToTeam(p.getName());
        });
    }

    public void addToTeam(String player) {
        Bukkit.getOnlinePlayers().forEach(o -> {
            org.bukkit.scoreboard.Team team = getOrRegister(o);
            team.addEntry(player);
        });
    }

    public void removeFromTeam(String player) {
        Bukkit.getOnlinePlayers().forEach(o -> {
            org.bukkit.scoreboard.Team team = getOrRegister(o);
            team.removeEntry(player);
        });
    }

    public void updateShop(Player player) {
        if(!isOnTeam(player)) {
            shopArea.removeState(player);
            return;
        }
        shopArea.update(player);
    }

    public boolean handleBlockClick(Player player, Location at) {
        return isOnTeam(player) && shopArea.handleRightClick(player, at);
    }

    public void resetTeam() {

        players.forEach(p -> {
            Player pl = Bukkit.getPlayer(p);
            if(pl != null) removeFromTeam(pl.getName());
        });
        players.clear();
        shopArea.resetStates();
        lastRespawn.clear();
    }

    public JSONObject toJSON() {
        JSONObject obj = new JSONObject();
        obj.put("playButton", JSONUtils.locationToJSON(playButton));
        obj.put("spawnLocation", JSONUtils.locationToJSON(spawnLocation));
        obj.put("shopArea", shopArea.toJSON());
        obj.put("captureRegion", captureRegion.toJSON());
        obj.put("name", name);
        return obj;
    }

    public static Team fromJSON(JSONObject object) {
        Location playButton = JSONUtils.locationFromJSON((JSONObject) object.get("playButton")),
                spawnLocation = JSONUtils.locationFromJSON((JSONObject) object.get("spawnLocation"));
        ShopArea shopArea = ShopArea.fromJSON((JSONObject) object.get("shopArea"));
        Region captureRegion = Region.fromJSON((JSONObject) object.get("captureRegion"));
        String name = (String) object.get("name");
        ChatColor color;
        short glassColor;

        if(name.equalsIgnoreCase("Blue")) {
            color = ChatColor.AQUA;
            glassColor = DyeColor.LIGHT_BLUE.getData();
        } else {
            color = ChatColor.RED;
            glassColor = DyeColor.RED.getData();
        }
        return new Team(name, color, glassColor, playButton, spawnLocation, captureRegion, shopArea);
    }
}
