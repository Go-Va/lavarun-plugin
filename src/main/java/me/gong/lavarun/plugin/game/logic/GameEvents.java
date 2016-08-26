package me.gong.lavarun.plugin.game.logic;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.arena.Arena;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.game.events.DeathEvent;
import me.gong.lavarun.plugin.game.events.PreventBreakEvent;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.util.BukkitUtils;
import me.gong.lavarun.plugin.util.NumberUtils;
import me.gong.lavarun.plugin.util.TimeUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

public class GameEvents implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent ev) {
        ev.getPlayer().spigot().setCollidesWithEntities(false);
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena == null) return;
        currentArena.addPlayer(ev.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena != null) currentArena.removePlayer(ev.getPlayer());
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            Arena currentArena = gm.getCurrentArena();
            if(!currentArena.isPlaying(ev.getPlayer(), true)) {
                ev.setCancelled(true);
                return;
            }
            if(ev.getPlayer().getInventory().getHeldItemSlot() == 1) {
                ev.getItemDrop().remove();
                ev.getPlayer().getInventory().setItem(1, Team.getEmptySlot());
            }
            else ev.setCancelled(true);
            ev.getPlayer().updateInventory();

        }
    }

    @EventHandler
    public void onInventory(InventoryClickEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            if(ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Player) {
                Arena currentArena = gm.getCurrentArena();
                Player p = (Player) ev.getInventory().getHolder();
                if (p.getGameMode() == GameMode.CREATIVE || !currentArena.isPlaying(p, true)) return;
                ev.setCancelled(true);
                p.closeInventory();
                ev.setCursor(null);
                ev.setResult(Event.Result.DENY);

                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            if(ev.getInventory().getHolder() != null && ev.getInventory().getHolder() instanceof Player) {
                Arena currentArena = gm.getCurrentArena();
                Player p = (Player) ev.getInventory().getHolder();
                if (p.getGameMode() == GameMode.CREATIVE || !currentArena.isPlaying(p, true)) return;
                ev.setCancelled(true);
                p.closeInventory();
                ev.setCursor(null);
                ev.setResult(Event.Result.DENY);

                p.updateInventory();
            }
        }
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(NumberUtils.random.nextBoolean()) {
            ev.setCancelled(true);
            return;
        }
        Arena currentArena = gm.getCurrentArena();
        if(ev.getFoodLevel() <= 6 || (!gm.isInGame() && currentArena != null)) ev.setCancelled(true);
        if(ev.getFoodLevel() <= 7) gm.setHunger((Player) ev.getEntity(), true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame()) {
            Arena currentArena = gm.getCurrentArena();
            if(!currentArena.isPlaying(ev.getPlayer(), true)) ev.setCancelled(true);
            else if(gm.getSpawnFoods().contains(ev.getItem().getItemStack().getType())) {
                ev.setCancelled(true);
                if(ev.getPlayer().getFoodLevel() < 20) {
                    ev.getItem().remove();
                    ev.getPlayer().setFoodLevel(20);
                    gm.setHunger(ev.getPlayer(), false);
                    ev.getPlayer().setSaturation(15);
                    ev.getPlayer().setFireTicks(0);
                    ev.getPlayer().playSound(ev.getPlayer().getLocation(), Sound.ENTITY_PLAYER_BURP, 2.0f, 1.0f);
                }
            } else ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntity(PlayerInteractEntityEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(!gm.isInGame() || ev.getPlayer().getGameMode() == GameMode.CREATIVE) return;
        ev.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena == null || ev.getPlayer().getGameMode() == GameMode.CREATIVE || !gm.isInGame() || !currentArena.isPlaying(ev.getPlayer(), true)) return;
        Block b = ev.getBlock();
        if(b.getType() != Material.STAINED_GLASS || currentArena.getTeam(ev.getPlayer()).getGlassColor() != b.getData()) {
            PreventBreakEvent pB = new PreventBreakEvent(ev.getPlayer(), b, currentArena, true);
            Bukkit.getPluginManager().callEvent(pB);
            if (!pB.isCancelled()) ev.setCancelled(true);
        } else if(currentArena.getLavaRegion().contains(b.getLocation())) {
            Bukkit.getScheduler().runTask(InManager.get().getInstance(Main.class), () -> b.setType(Material.LAVA));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena == null) return;
        if(!gm.isInGame()) {
            if(ev.getPlayer().getGameMode() != GameMode.CREATIVE) ev.setCancelled(true);

            if(ev.getClickedBlock() == null) return;

            if(ev.getClickedBlock().getLocation().equals(currentArena.getBlue().getPlayButton())) {
                gm.toggleBlueReady();
                if(!gm.isRedReady() || !gm.isBlueReady()) Bukkit.broadcastMessage(ChatColor.AQUA+"Blue is "+(gm.isBlueReady() ? "now" : "no longer")+" ready");
            } else if(ev.getClickedBlock().getLocation().equals(currentArena.getRed().getPlayButton())) {
                gm.toggleRedReady();
                if(!gm.isRedReady() || !gm.isBlueReady()) Bukkit.broadcastMessage(ChatColor.RED+"Red is "+(gm.isRedReady() ? "now" : "no longer")+" ready");
            }
            if(gm.isRedReady() && gm.isBlueReady()) {
                gm.beginGame();
            }
        }
        if(gm.isInGame() && ev.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            Location to = ev.getClickedBlock().getLocation();

            if(ev.getAction().name().contains("BLOCK")) {

                if(!ev.getAction().name().contains("LEFT")) to = to.clone().add(ev.getBlockFace().getModX(), ev.getBlockFace().getModY(), ev.getBlockFace().getModZ());
                if(!currentArena.isBlockInteractable(to, ev.getItem(), ev.getPlayer(), ev.getAction().name().contains("LEFT"))) {
                    ev.setCancelled(true);
                    ev.getPlayer().updateInventory();
                    return;
                }
            }
            if(ev.getAction() == Action.PHYSICAL) {
                currentArena.getTeam(ev.getPlayer()).updateShop(ev.getPlayer());
                ev.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena == null) return;
        if(ev.getEntity() instanceof Player) {
            Player p = (Player) ev.getEntity();
            if(currentArena.isPlaying(p, true)) {

                if(currentArena.getTeam(p).getTimeBetweenLastRespawn(p) < 1000) {
                    ev.setCancelled(true);
                    return;
                }
                Player o;
                if(ev.getDamager() instanceof Player) {
                    o = (Player) ev.getDamager();
                    if(!currentArena.isPlaying(o, true) || currentArena.getTeam(o).getCaptureRegion().contains(o.getLocation())) {
                        ev.setCancelled(true);
                        return;
                    }
                    if(currentArena.getPlayArea().contains(p)) {
                        ev.setDamage(0);
                        gm.attackPlayer(p, 1, o);
                    }
                    if(currentArena.getTeam(o).equals(currentArena.getTeam(p)) || !currentArena.canBeDamaged(p, o)) {
                        ev.setCancelled(true);
                        if(currentArena.isPlaying(p, true) && !currentArena.getFoodRegion().contains(p) && !currentArena.getFoodRegion().contains(o) && !currentArena.getTeam(p).getCaptureRegion().contains(p) &&
                                !currentArena.getTeam(o).equals(currentArena.getTeam(p))) {
                            gm.attackPlayer(p, 1, o);
                            NumberUtils.knockEntityWithDamage(o, p);
                        }
                        return;
                    }
                } else {
                    ev.setCancelled(true);
                    return;
                }
                gm.handleDamage(p);
                if(p.getHealth() - ev.getFinalDamage() <= 0) {
                    DeathEvent pd = new DeathEvent(p, o);
                    Bukkit.getPluginManager().callEvent(pd);
                    ev.setCancelled(true);
                    if(!pd.isCancelled()) {
                        gm.handleAttack(p, (Player) ev.getDamager());
                        gm.handleKill(p);
                    }
                } else gm.handleAttack(p, (Player) ev.getDamager());
            } else ev.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        if(gm.isInGame() && ev.getPlayer().getGameMode() == GameMode.SURVIVAL) ev.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDeath(EntityDamageEvent ev) {
        GameManager gm = InManager.get().getInstance(GameManager.class);
        Arena currentArena = gm.getCurrentArena();
        if(currentArena == null) return;
        if(ev.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;
        if(ev.getEntity() instanceof Player) {
            Player p = (Player) ev.getEntity();
            if(currentArena.isPlaying(p, true)) {

                if(currentArena.getTeam(p).getTimeBetweenLastRespawn(p) < 1000) {
                    ev.setCancelled(true);
                    return;
                }
                if(p.getHealth() - ev.getFinalDamage() <= 0) {
                    DeathEvent pd = new DeathEvent(p, ev.getCause());
                    Bukkit.getPluginManager().callEvent(pd);
                    ev.setCancelled(true);
                    if (!pd.isCancelled()) gm.handleKill(p);
                } else {
                    gm.handleDamage(p);
                    if(ev.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK) p.setFireTicks(p.getFireTicks() > 40 ? 40 : p.getFireTicks());
                }
            } else ev.setCancelled(true);
        } else ev.setCancelled(true);
    }


}
