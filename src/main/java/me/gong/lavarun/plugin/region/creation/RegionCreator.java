package me.gong.lavarun.plugin.region.creation;

import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.region.Region;
import me.gong.lavarun.plugin.region.RegionRenderer;
import me.gong.lavarun.plugin.region.creation.box.Box;
import me.gong.lavarun.plugin.region.creation.box.BoxCreator;
import me.gong.lavarun.plugin.util.AxisAlignedBB;
import me.gong.lavarun.plugin.util.BlockUtils;
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

import java.util.List;

public class RegionCreator implements Listener {

    private RegionBuilder builder;
    private CreationListener listener;
    private BoxCreator boxCreator;

    private RegionCreator(RegionBuilder builder, CreationListener listener) {
        this.builder = builder;
        this.listener = listener;
    }

    public static void beginPlayer(Player p, CreationListener listener) {
        RegionCreator c = new RegionCreator(new RegionBuilder(p), listener);
        c.showMessage();
        getRenderer().addCreator(c);
        Bukkit.getPluginManager().registerEvents(c, InManager.get().getInstance(Main.class));
    }

    private static RegionRenderer getRenderer() {
        return InManager.get().getInstance(RegionRenderer.class);
    }

    private boolean checkForQuit() {
        if(builder.getPlayer() == null) {
            cancel(false);
            return true;
        }
        return false;
    }

    private void showMessage() {
        if(!checkForQuit()) {
            Player p = builder.getPlayer();
            p.sendMessage("");
            p.sendMessage(ChatColor.GOLD + "At any time, you can type ':cancel' to exit creation");
            p.sendMessage(ChatColor.GREEN+"Type ':finish' when you are done.");
            p.getInventory().setItem(0, getBoxCreator());
            p.sendMessage(ChatColor.GREEN+"Use the iron hoe to create boxes.");
            p.sendMessage(ChatColor.GOLD+"Drop it to cancel the current selection");
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
        getRenderer().removeCreator(this);
    }

    private void cancel(boolean message) {
        remove();
        if(builder.getPlayer() != null && message) builder.getPlayer().sendMessage(ChatColor.GOLD+"Region creation canceled.");
        listener.onFail();
    }

    private void create(World world) {
        remove();
        listener.onCreation(builder.build(world));
    }

    private void handleBox(Location location, boolean left) {
        if(boxCreator == null) {
            builder.getPlayer().sendMessage(ChatColor.GREEN+"Began box creation");
            boxCreator = new BoxCreator(bb -> {
                builder.addBox(bb);
                builder.getPlayer().sendMessage(ChatColor.GOLD+"Box created!");
                boxCreator = null;
            });
        }
        builder.getPlayer().sendMessage("");
        boxCreator.onClick(left, location, builder.getPlayer());
    }

    private List<Location> boxToOutline(World w, AxisAlignedBB bb) {
        Location start = new Location(w, bb.minX, bb.minY, bb.minZ),
                end = new Location(w, bb.maxX+1, bb.maxY+1, bb.maxZ+1);
        return BlockUtils.outlineBox(start, end, 0.1);
    }

    private List<Location> locationOutline(Location location) {
        Location start = new Location(location.getWorld(), location.getBlockX()-0.1, location.getBlockY()-0.1, location.getBlockZ()-0.1),
                end = new Location(start.getWorld(), start.getX() + 1.2, start.getY() + 1.2, start.getZ() + 1.2);
        return BlockUtils.outlineBox(start, end, 0.1);
    }

    //void spawnParticle(Particle var1, Location var2, int amount, double expandx, double expandy, double expandz);
    private void renderBox(Box box, Particle p, int particlePer, double speed) {
        if(!checkForQuit()) {
            if(box.getRenderPoint() == null || box.getRenderPoint().isEmpty())
                box.setRenderPoint(boxToOutline(builder.getPlayer().getWorld(), box.getAxis().offset(0, 0.1, 0)));
            builder.getPlayer().spawnParticle(p, box.getToRender1(), particlePer, 0, 0, 0, speed);
            builder.getPlayer().spawnParticle(p, box.getToRender2(), particlePer, 0, 0, 0, speed);
            if(System.currentTimeMillis() % 750 > 680)
                locationOutline(new Location(builder.getPlayer().getWorld(), box.getAxis().minX, box.getAxis().minY, box.getAxis().minZ))
                        .forEach(l -> builder.getPlayer().spawnParticle(p, l, 0, 0, 0, speed));
            if(System.currentTimeMillis() % 750 > 680)
                locationOutline(new Location(builder.getPlayer().getWorld(), box.getAxis().maxX, box.getAxis().maxY, box.getAxis().maxZ))
                        .forEach(l -> builder.getPlayer().spawnParticle(p, l, 0, 0, 0, speed));
            box.scroll();

        }
    }

    private void renderLoc(Particle p, Location loc, int particlePer, double speed) {
        if(!checkForQuit()) {
            locationOutline(loc).forEach(l -> builder.getPlayer().spawnParticle(p, l, particlePer, 0, 0, 0, speed));
        }
    }

    public RegionBuilder getBuilder() {
        return builder;
    }

    public BoxCreator getCreator() {
        return boxCreator;
    }

    public void render() {
        builder.getBoxes().forEach(b -> renderBox(b, Particle.FLAME, 1, 0));
        if(boxCreator != null) {
            if(boxCreator.getPos1() != null && System.currentTimeMillis() % 750 > 680) renderLoc(Particle.VILLAGER_HAPPY, boxCreator.getPos1(), 1, 0);
            if(boxCreator.getPos2() != null && System.currentTimeMillis() % 750 > 680) renderLoc(Particle.VILLAGER_HAPPY, boxCreator.getPos2(), 1, 0);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()))
            cancel(false);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()) && ev.getItemDrop().getItemStack().equals(getBoxCreator())) {
            ev.setCancelled(true);
            if(boxCreator != null) {
                boxCreator = null;
                ev.getPlayer().sendMessage("");
                ev.getPlayer().sendMessage(ChatColor.GOLD+"Selection canceled");
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent ev) {
        if(ev.getPlayer().getUniqueId().equals(builder.getUUID()) && ev.getHand() == EquipmentSlot.HAND) {

            ItemStack s = ev.getPlayer().getInventory().getItemInMainHand();
            //                                                                   don't open a gate/door/etc
            if(s != null && s.equals(getBoxCreator()) && ev.useInteractedBlock() == Event.Result.ALLOW &&
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
                        create(ev.getPlayer().getWorld());
                        ev.getPlayer().sendMessage(ChatColor.GOLD+"Region created");
                    } else ev.getPlayer().sendMessage(ChatColor.RED+"Unable to create region: "+s);
                } else ev.getPlayer().sendMessage(ChatColor.GOLD+"You are currently creating boxes. Type ':cancel' to exit region creation");
            }
        }
    }

    public interface CreationListener {
        void onCreation(Region region);

        void onFail();
    }
}
