package me.gong.lavarun.plugin.arena;

import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.team.TeamCreator;
import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.TeamBuilder;
import me.gong.lavarun.plugin.game.GameTutorial;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.region.creation.RegionCreator;
import me.gong.lavarun.plugin.tutorial.data.Tutorial;
import me.gong.lavarun.plugin.tutorial.usage.builder.TutorialCreator;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ArenaCreator implements Listener {

    private ArenaBuilder builder;
    private Stage stage;

    private TeamCreator team;

    private UUID player;
    private CreationListener listener;
    private boolean wait;

    private ArenaCreator(Player player, String name, CreationListener listener) {
        this.player = player.getUniqueId();
        this.listener = listener;
        this.builder = new ArenaBuilder(player, name);
        this.stage = Stage.values()[0];
        stage.onSet(this);
        player.sendMessage(ChatColor.GREEN+stage.getInstruction());
    }

    public static ArenaCreator beginPlayer(Player player, String name, CreationListener listener) {
        ArenaCreator ret = new ArenaCreator(player, name, listener);
        Bukkit.getPluginManager().registerEvents(ret, InManager.get().getInstance(Main.class));
        return ret;
    }

    private Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public void onClick(Location location, BlockFace face) {
        Stage next = stage.ordinal() != Stage.values().length - 1 ? Stage.values()[stage.ordinal()+1] : null;
        String error = stage.getDirections().doClick(this, location, face);
        if(error == null || !error.isEmpty()) getPlayer().sendMessage("");
        if(error != null) {
            if (!error.isEmpty()) getPlayer().sendMessage(ChatColor.RED + error);
        } else {
            getPlayer().playSound(getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 2.0f);
            String msg = ChatColor.GOLD.toString();
            if(!stage.getFinish().isEmpty()) msg += stage.getFinish();
            if(next != null && !next.getInstruction().isEmpty()) msg += (ChatColor.stripColor(msg).isEmpty() ? "" : " ")+ChatColor.GREEN+next.getInstruction();
            if(!ChatColor.stripColor(msg).isEmpty()) getPlayer().sendMessage(msg);
            if(next != null) {
                stage = next;
                stage.onSet(this);
            } else {
                create();
            }
        }
    }

    private void reset() {
        HandlerList.unregisterAll(this);
    }

    public void cancel(boolean message) {
        reset();

        if(message && getPlayer() != null) getPlayer().sendMessage(ChatColor.GREEN+"Canceled arena creation");
        listener.onFail();
    }

    private void create() {
        reset();
        listener.onCreation(builder.build());
    }

    @EventHandler
    public void onClick(PlayerInteractEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(player) && ev.getHand() == EquipmentSlot.HAND) {

            ItemStack s = ev.getPlayer().getInventory().getItemInMainHand();
            //                                          don't open a gate/door/etc
            if (s.getType() == Material.AIR && ev.getAction() == Action.RIGHT_CLICK_BLOCK && ev.useInteractedBlock() == Event.Result.ALLOW &&
                    ev.getAction().name().contains("BLOCK")) {
                ev.setCancelled(true);
                onClick(ev.getClickedBlock().getLocation(), ev.getBlockFace());
            }
        }
    }

    @EventHandler
    public void onTalk(AsyncPlayerChatEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(player)) {
            if (stage == Stage.FOOD_SPAWN || stage == Stage.FIREWORK_SPAWN && ev.getMessage().equalsIgnoreCase(":finish")) {
                wait = false;
                onClick(null, null);
                ev.setCancelled(true);
                return;
            }
            if(team != null && !team.arenaSetup) {
                ev.setCancelled(true);
                if (ev.getMessage().equalsIgnoreCase("cancel")) cancel(true);
                else ev.getPlayer().sendMessage(ChatColor.GOLD + "You are currently setting up an arena. Type 'cancel' to exit.");
            }
        }
    }

    @EventHandler
    public void onCmd(PlayerCommandPreprocessEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(player) && team != null && !team.arenaSetup) {
            ev.setCancelled(true);
            ev.getPlayer().sendMessage(ChatColor.GOLD+"You are currently setting up an arena. Type 'cancel' to exit.");
        }
    }

    public enum Stage {

        PLAY_AREA("Choose the area players will be playing in.", "Set play area.", (c, l, f) -> {
            if(c.builder.getPlayArea() != null) return null;
            return "";
        }, c -> RegionCreator.beginPlayer(c.getPlayer(), new RegionCreator.CreationListener() {
            @Override
            public void onCreation(Region region) {
                c.builder.setPlayArea(region);
                c.onClick(null, null);
            }

            @Override
            public void onFail() {
                c.cancel(false);
            }
        })),
        LAVA_AREA("Choose the area lava will be set.", "Set lava area.", (c, l, f) -> {
            if(c.builder.getLavaRegion() != null) return null;
            return "";
        }, c -> RegionCreator.beginPlayer(c.getPlayer(), new RegionCreator.CreationListener() {
            @Override
            public void onCreation(Region region) {
                c.builder.setLavaRegion(region);
                c.onClick(null, null);
            }

            @Override
            public void onFail() {
                c.cancel(false);
            }
        })),
        FOOD_SPAWN("Click on a block to add a food spawn location. Type ':finish' when done.", "Set food spawn locations.", (c, l, f) -> {
            if(!c.wait) return null;
            Location a = l.clone().add(f.getModX(), f.getModY(), f.getModZ());
            if(c.builder.containsFoodLocation(a)) c.getPlayer().sendMessage(ChatColor.RED+"That location is already a food spawn");
            else {
                c.builder.addFoodSpawn(a);
                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 0.0f);
                c.getPlayer().sendMessage(ChatColor.GOLD + "Added food spawn location");
            }
            return "";
        }, c -> c.wait = true),
        FOOD_AREA("Set the area where players will be in when collecting food.", "Set food area.", (c, l, f) -> {
            if(c.builder.getFoodRegion() != null) return null;
            return "";
        }, c -> RegionCreator.beginPlayer(c.getPlayer(), new RegionCreator.CreationListener() {
            @Override
            public void onCreation(Region region) {
                c.builder.setFoodRegion(region);
                c.onClick(null, null);
            }

            @Override
            public void onFail() {
                c.cancel(false);
            }
        })),
        TEAMS("", "", (c, l, f) -> {
            if(c.team == null) return null;
            c.team.onClick(l, f);
            return "";
        }, c -> c.team = new TeamCreator(c, new TeamBuilder("Blue", ChatColor.AQUA, DyeColor.LIGHT_BLUE.getData(), c.getPlayer()),
                new TeamBuilder("Red", ChatColor.RED, DyeColor.RED.getData(), c.getPlayer()), c.getPlayer(), (blue, red) -> {
            c.builder.setRed(red);
            c.builder.setBlue(blue);
            c.team = null;
            c.onClick(null, null);

        })),
        TEAM_CHOOSE("Click on the block where players will spawn to choose their team", "Set team choose location", (c, l, f) -> {
            c.builder.setTeamChooseLocation(l.add(f.getModX(), f.getModY(), f.getModZ()));
            return null;
        }),
        FIREWORK_SPAWN("Click on a block to add a firework spawn location. Type ':finish' when done.", "Set firework spawn locations.", (c, l, f) -> {
            if(!c.wait) return null;
            Location a = l.clone().add(f.getModX(), f.getModY(), f.getModZ());
            if(c.builder.containsFireworkLocation(a)) c.getPlayer().sendMessage(ChatColor.RED+"That location is already a firework spawn");
            else {
                c.builder.addFireworkSpawn(a);
                c.getPlayer().playSound(c.getPlayer().getLocation(), Sound.BLOCK_NOTE_PLING, 2.0f, 0.0f);
                c.getPlayer().sendMessage(ChatColor.GOLD + "Added firework spawn location");
            }
            return "";
        }, c -> c.wait = true),
        TUTORIAL("", "", (c, l, f) -> {
            if(c.builder.getTutorial() == null) return "";
            return null;
        }, c -> TutorialCreator.beginPlayer(c.getPlayer(), new GameTutorial(), new TutorialCreator.CreationListener() {
            @Override
            public void onCreation(Tutorial area) {
                c.builder.setTutorial(area);
                c.onClick(null, null);
            }

            @Override
            public void onFail() {
                c.cancel(false);
            }
        }));


        private final String instruction;
        private final String finish;
        private final Directions directions;
        private SetListener set;

        Stage(String instruction, String finish, Directions directions, SetListener setListener) {

            this.set = setListener;
            this.instruction = instruction;
            this.finish = finish;
            this.directions = directions;
        }

        Stage(String instruction, String finish, Directions directions) {
            this(instruction, finish, directions, null);
        }

        public String getInstruction() {
            return instruction;
        }

        public Directions getDirections() {
            return directions;
        }

        public String getFinish() {
            return finish;
        }

        public void onSet(ArenaCreator c) {
            if(set != null) set.onSet(c);
        }
    }

    public interface Directions {
        String doClick(ArenaCreator c, Location location, BlockFace face);
    }

    public interface SetListener {
        void onSet(ArenaCreator c);
    }

    public interface CreationListener {
        void onCreation(Arena arena);

        void onFail();
    }
}
