package net.wesjd.lavarun.plugin.region;

import net.wesjd.lavarun.plugin.Main;
import net.wesjd.lavarun.plugin.im.InManager;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class RegionManager implements Listener {

    public RegionManager() {
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
    }


}
