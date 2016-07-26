package net.wesjd.lavarun.plugin.region.creation;

import net.wesjd.lavarun.plugin.Main;
import net.wesjd.lavarun.plugin.im.InManager;
import net.wesjd.lavarun.plugin.region.Region;
import net.wesjd.lavarun.plugin.region.creation.box.BoxCreator;
import net.wesjd.lavarun.plugin.util.AxisAlignedBB;
import net.wesjd.lavarun.plugin.util.BlockUtil;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public class RegionCreator implements Listener {

    private static final Pattern VALID_CHARACTERS = Pattern.compile("[a-zA-Z0-9_]{1,16}");

    private static List<RegionCreator> creators = new CopyOnWriteArrayList<>();
    private static BukkitTask r;

    private RegionBuilder builder;
    private State state;
    private CreationListener listener;
    private BoxCreator boxCreator;

    private RegionCreator(RegionBuilder builder, CreationListener listener) {
        this.builder = builder;
        this.state = State.NAME;
        this.listener = listener;
    }

    public static void beginPlayer(Player p, CreationListener listener) {
        RegionCreator c = new RegionCreator(new RegionBuilder(p), listener);
        c.showBeginMessage(true);
        creators.add(c);
        if(r == null) {
            r = new BukkitRunnable() {
                @Override
                public void run() {
                    creators.forEach(r -> {
                        r.builder.getBoxes().forEach(b -> r.renderBox(Particle.FLAME, b, 1, 0));
                        if(r.boxCreator != null) {
                            if(r.boxCreator.getPos1() != null) r.renderLoc(Particle.VILLAGER_HAPPY, r.boxCreator.getPos1(), 1, 0);
                            if(r.boxCreator.getPos2() != null) r.renderLoc(Particle.VILLAGER_HAPPY, r.boxCreator.getPos2(), 1, 0);
                        }
                    });
                }
            }.runTaskTimer(InManager.get().getInstance(Main.class), 0, 10);
        }
        Bukkit.getPluginManager().registerEvents(c, InManager.get().getInstance(Main.class));
    }

    private boolean checkForQuit() {
        if(builder.getPlayer() == null) {
            cancel(false);
            return true;
        }
        return false;
    }

    private void showBeginMessage(boolean entrance) {
        if(!checkForQuit()) {
            Player p = builder.getPlayer();
            builder.getPlayer().sendMessage("");
            p.sendMessage(ChatColor.GREEN + (entrance ? "Created beginning region. " : "")+"Type in chat the name of the region.");
            if(entrance) p.sendMessage(ChatColor.GOLD + "At any time, you can type ':cancel' to exit creation");
        }
    }

    private void showBoxesMessage(boolean item) {
        if(!checkForQuit()) {
            Player p = builder.getPlayer();
            state = State.BOXES;
            builder.getPlayer().sendMessage("");
            p.sendMessage(ChatColor.GREEN+"Now creating boxes. Type ':finish' when you are done.");
            p.sendMessage(ChatColor.GREEN+"You can type ':setname' to rename at any time");
            if(item) {
                p.getInventory().setItem(0, getBoxCreator());
                p.sendMessage(ChatColor.GREEN+"Use the iron hoe to create boxes.");
                p.sendMessage(ChatColor.GOLD+"Drop it to cancel the current selection");
            }
        }
    }

    private ItemStack getBoxCreator() {
        ItemStack s = new ItemStack(Material.IRON_HOE);
        ItemMeta m = s.getItemMeta();
        m.setDisplayName(ChatColor.GREEN+"Box Creator");
        m.spigot().setUnbreakable(true);
        s.setItemMeta(m);
        return s;
    }

    private void remove() {
        HandlerList.unregisterAll(this);
        creators.remove(this);
    }

    private void cancel(boolean message) {
        remove();
        if(builder.getPlayer() != null && message) builder.getPlayer().sendMessage(ChatColor.GOLD+"Region creation canceled.");
        listener.onFail();
    }

    private void create() {
        remove();
        listener.onCreation(builder.build());
    }

    private void handleBox(Location location, boolean left) {
        builder.getPlayer().sendMessage("");
        if(boxCreator == null) {
            builder.getPlayer().sendMessage("");
            builder.getPlayer().sendMessage(ChatColor.GREEN+"Began box creation");
            boxCreator = new BoxCreator(bb -> {
                builder.addBox(bb);
                builder.getPlayer().sendMessage(ChatColor.GOLD+"Box created!");
                boxCreator = null;
            });
        }
        boxCreator.onClick(left, location, builder.getPlayer());
    }

    private List<Location> boxToOutline(World w, AxisAlignedBB bb) {
        Location start = new Location(w, bb.minX, bb.minY, bb.minZ),
                end = new Location(w, bb.maxX+1, bb.maxY+1, bb.maxZ+1);
        return BlockUtil.outlineBox(start, end, 0.1);
    }

    private List<Location> locationOutline(Location location) {
        double gap = 0.1;
        Location start = new Location(location.getWorld(), location.getBlockX() - (gap * 9) + 1, location.getBlockY() - (gap * 9) + 0.9, location.getBlockZ() - (gap * 9) + 1),
                end = new Location(start.getWorld(), start.getX() + (gap * 9) + gap, start.getY() + (gap * 9), start.getZ() + (gap * 9) + gap);
        return BlockUtil.outlineBox(start, end, gap);
    }



    //void spawnParticle(Particle var1, Location var2, int amount, double expandx, double expandy, double expandz);
    private void renderBox(Particle p, AxisAlignedBB bb, int particlePer, double speed) {
        if(!checkForQuit()) {
            boxToOutline(builder.getPlayer().getWorld(), bb.offset(0, 0.1, 0)).forEach(l -> builder.getPlayer().spawnParticle(p, l, particlePer, 0, 0, 0, speed));
        }
    }

    private void renderLoc(Particle p, Location loc, int particlePer, double speed) {
        if(!checkForQuit()) {
            locationOutline(loc).forEach(l -> builder.getPlayer().spawnParticle(p, l, particlePer, 0, 0, 0, speed));
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()))
            cancel(false);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()) && ev.getItemDrop().getItemStack().equals(getBoxCreator()) && state == State.BOXES) {
            ev.setCancelled(true);
            if(boxCreator != null) {
                boxCreator = null;
                ev.getPlayer().sendMessage(ChatColor.GOLD+"Selection canceled");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()) && ev.getHand() == EquipmentSlot.HAND) {

            ItemStack s = ev.getPlayer().getInventory().getItemInMainHand();
            //                                                                   don't open a gate/door/etc
            if(s != null && s.equals(getBoxCreator()) && state == State.BOXES && ev.useInteractedBlock() == Event.Result.ALLOW &&
                    ev.getAction().name().contains("BLOCK")) {
                handleBox(ev.getClickedBlock().getLocation(), ev.getAction() == Action.LEFT_CLICK_BLOCK);
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID())) {
            ev.setCancelled(true);
            if(ev.getMessage().equalsIgnoreCase(":cancel")) cancel(true);
            else {
                if(ev.getMessage().equalsIgnoreCase(":finish")) {
                    String s = builder.isValid();
                    if(s == null) {
                        create();
                        ev.getPlayer().sendMessage(ChatColor.GOLD+"Region '"+builder.getName()+"' created");
                    } else ev.getPlayer().sendMessage(ChatColor.RED+"Unable to create region: "+s);
                } else if(ev.getMessage().equalsIgnoreCase(":setname")) {
                    if(state == State.NAME) ev.getPlayer().sendMessage(ChatColor.RED+"Already setting the name");
                    else {
                        state = State.NAME;
                        if(boxCreator != null) {
                            ev.getPlayer().sendMessage(ChatColor.GOLD+"Canceled box creation");
                            boxCreator = null;
                        }
                        showBeginMessage(false);
                    }
                } else if(state == State.NAME) {
                    if(VALID_CHARACTERS.matcher(ev.getMessage()).matches()) {
                        builder.setName(ev.getMessage());
                        showBoxesMessage(true);
                    } else ev.getPlayer().sendMessage(ChatColor.GOLD+"Invalid name for a region");
                } else ev.getPlayer().sendMessage(ChatColor.GOLD+"You are currently creating boxes. Type ':cancel' to exit region creation");
            }
        }
    }

    public interface CreationListener {
        void onCreation(Region region);

        void onFail();
    }

    private enum State {
        NAME, BOXES
    }
}
