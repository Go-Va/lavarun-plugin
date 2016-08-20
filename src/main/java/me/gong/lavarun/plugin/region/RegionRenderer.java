package me.gong.lavarun.plugin.region;

import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.region.creation.RegionCreator;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class RegionRenderer implements Listener {

    private Set<RegionCreator> creators = new HashSet<>();

    public RegionRenderer() {
        Bukkit.getPluginManager().registerEvents(this, InManager.get().getInstance(Main.class));
        new BukkitRunnable() {
            @Override
            public void run() {
                creators.forEach(RegionCreator::render);
            }
        }.runTaskTimer(InManager.get().getInstance(Main.class), 0, 1);

    }

    public void addCreator(RegionCreator c) {
        creators.add(c);
    }

    public void removeCreator(RegionCreator c) {
        creators.remove(c);
    }

}
