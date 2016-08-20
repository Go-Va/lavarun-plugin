package me.gong.lavarun.plugin.arena.team;

import me.gong.lavarun.plugin.arena.ArenaCreator;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.region.creation.RegionCreator;
import me.gong.lavarun.plugin.shop.ShopArea;
import me.gong.lavarun.plugin.shop.ShopAreaCreator;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCreator {


    private UUID player;

    private TeamBuilder blue, red;
    private ShopAreaCreator blueShop, redShop;
    private ArenaCreator arena;
    private final CreationListener listener;
    private Stage stage;
    public boolean arenaSetup;

    public TeamCreator(ArenaCreator arena, TeamBuilder blue, TeamBuilder red, Player player, CreationListener listener) {

        this.arena = arena;
        this.player = player.getUniqueId();
        this.blue = blue;
        this.red = red;
        this.listener = listener;
        this.stage = Stage.values()[0];
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD+"Began team setup. "+ChatColor.GREEN + stage.getInstruction());
    }

    public void onClick(Location location, BlockFace face) {
        Stage next = stage.ordinal() != Stage.values().length - 1 ? Stage.values()[stage.ordinal() + 1] : null;
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
                stage.onSet(this);
            }
            else listener.onCreation(blue.build(), red.build());
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(player);
    }

    public enum Stage {
        BLUE_SPAWN("Right click on the block blue players will spawn on", "Set blues spawn.", (c, l, f) -> {
            c.blue.setSpawnLocation(l.add(f.getModX(), f.getModY(), f.getModZ()));
            return null;
        }),
        RED_SPAWN("Right click on the block red players will spawn on", "Set reds spawn.", (c, l, f) -> {
            c.red.setSpawnLocation(l.add(f.getModX(), f.getModY(), f.getModZ()));
            return null;
        }),
        BLUE_BUTTON("Right click on the button used to mark blue as ready", "Set blues mark-ready button.", (c, l, f) -> {
            Block b = l.getBlock();
            if (b.getType() == Material.STONE_BUTTON) {
                c.blue.setPlayButton(l);
            } else return ChatColor.RED + "You must click a stone button for blues mark-ready button.";
            return null;
        }),
        RED_BUTTON("Right click on the button used to mark red as ready", "Set reds mark-ready button.", (c, l, f) -> {
            Block b = l.getBlock();
            if (b.getType() == Material.STONE_BUTTON) {
                c.red.setPlayButton(l);
            } else return "You must click a stone button for reds mark-ready button.";
            return null;
        }),
        CAPTURE_REGION_BLUE("", "", (c, l, f) -> {
            if(!c.arenaSetup) return null;
            return "";
        }, c -> {
            c.arenaSetup = true;
            c.getPlayer().sendMessage("");
            c.getPlayer().sendMessage(ChatColor.GREEN+"Create the region blue players will capture in");
            RegionCreator.beginPlayer(c.getPlayer(), new RegionCreator.CreationListener() {
                @Override
                public void onCreation(Region region) {
                    c.blue.setCaptureRegion(region);
                    c.arenaSetup = false;
                    c.onClick(null, null);
                }

                @Override
                public void onFail() {
                    c.arena.cancel(false);
                }
            });
        }),
        CAPTURE_REGION_RED("", "", (c, l, f) -> {
            if(!c.arenaSetup) return null;
            return "";
        }, c -> {
            c.arenaSetup = true;
            c.getPlayer().sendMessage("");
            c.getPlayer().sendMessage(ChatColor.GREEN+"Create the region red players will capture in");
            RegionCreator.beginPlayer(c.getPlayer(), new RegionCreator.CreationListener() {
                @Override
                public void onCreation(Region region) {
                    c.red.setCaptureRegion(region);
                    c.arenaSetup = false;
                    c.onClick(null, null);
                }

                @Override
                public void onFail() {
                    c.arena.cancel(false);
                }
            });
        }),
        BLUE_SHOP_AREA("", "Setup blue shop area", (c, l, f) -> {
            if(c.blueShop == null) return null;
            else {
                c.blueShop.onClick(l, f);
                return "";
            }
        }, c -> c.blueShop = new ShopAreaCreator(area -> {
            c.blue.setShopArea(area);
            c.blueShop = null;
            c.onClick(null, null);
        }, "Blue", c.getPlayer())),
        RED_SHOP_AREA("", "Setup red shop area", (c, l, f) -> {
            if(c.redShop == null) return null;
            else {
                c.redShop.onClick(l, f);
                return "";
            }
        }, c -> c.redShop = new ShopAreaCreator(area -> {
            c.red.setShopArea(area);
            c.redShop = null;
            c.onClick(null, null);
        }, "Red", c.getPlayer()));


        private final String instruction, finish;
        private final Directions directions;
        private SetListener setListener;

        Stage(String instruction, String finish, Directions directions, SetListener setListener) {

            this.instruction = instruction;
            this.finish = finish;
            this.directions = directions;
            this.setListener = setListener;
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

        public void onSet(TeamCreator c) {
            if(setListener != null) setListener.onSet(c);
        }
    }

    public interface CreationListener {
        void onCreation(Team blue, Team red);
    }

    public interface Directions {
        String doClick(TeamCreator c, Location location, BlockFace f);
    }

    public interface SetListener {
        void onSet(TeamCreator c);
    }
}
