package me.gong.lavarun.plugin.options;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;

public abstract class Option implements Listener {

    private boolean isEnabled;

    public abstract String getHelp();
    public abstract String getName();

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    @Override
    public String toString() {
        return ChatColor.YELLOW+getName()+
                " ["+(isEnabled ? ChatColor.GREEN+"en" : ChatColor.RED+"dis")+"abled"+ChatColor.YELLOW+"]: "+
                ChatColor.GRAY+getHelp();
    }

    public GameManager getGame() {
        return InManager.get().getInstance(GameManager.class);
    }

    public ShopManager getShop() {
        return InManager.get().getInstance(ShopManager.class);
    }
}
