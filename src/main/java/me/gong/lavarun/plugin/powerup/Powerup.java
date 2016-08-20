package me.gong.lavarun.plugin.powerup;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.arena.team.Team;
import me.gong.lavarun.plugin.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class Powerup implements Listener {

    private Map<UUID, Integer> usesLeft;

    public Powerup() {
        usesLeft = new HashMap<>();
    }

    public abstract ItemStack getItem(Team team);

    public abstract int getMaxUses();

    public abstract String getName();

    public abstract int getCost();

    public void unload() {

    }

    protected void onUse(Player player) {
        if(getMaxUses() == -1) return; //disabled


        if(!usesLeft.containsKey(player.getUniqueId()) && getMaxUses() > 1) {
            usesLeft.put(player.getUniqueId(), getMaxUses() - 1);
            ItemStack s = getItem(InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player));

            s.setItemMeta(updateName(s.getItemMeta(), getMaxUses(), getMaxUses() - 1));
            player.getInventory().setItem(player.getInventory().getHeldItemSlot(), s);
        }
        else {
            int num = usesLeft.containsKey(player.getUniqueId()) ? usesLeft.get(player.getUniqueId()) : -1;
            if(num - 1 <= 0) {
                usesLeft.remove(player.getUniqueId());
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), Team.getEmptySlot());
                player.updateInventory();
            } else {
                usesLeft.put(player.getUniqueId(), num - 1);
                ItemStack s = getItem(InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player));
                s.setItemMeta(updateName(s.getItemMeta(), num, num - 1));
                player.getInventory().setItem(player.getInventory().getHeldItemSlot(), s);
            }
        }
    }

    public ItemMeta updateName(ItemMeta m, int oldNum, int newNum) {
        String oldString = " "+ChatColor.YELLOW+"["+(oldNum)+"]", newString = " "+ChatColor.YELLOW+"["+newNum+"]";
        if(m.getDisplayName().endsWith(oldString)) m.setDisplayName(m.getDisplayName().substring(0, m.getDisplayName().indexOf(oldString))+newString);
        else m.setDisplayName(m.getDisplayName()+newString);
        return m;
    }

    public final boolean isSelected(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        return held.hasItemMeta() && held.getType() == getItem(InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player)).getType() &&
                held.getItemMeta().getDisplayName().contains(getItem(InManager.get().getInstance(GameManager.class).getCurrentArena().getTeam(player)).getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent ev) {
        usesLeft.remove(ev.getPlayer().getUniqueId());
    }
}
