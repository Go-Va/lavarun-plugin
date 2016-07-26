package net.wesjd.lavarun.plugin;

import net.wesjd.lavarun.plugin.command.CommandManager;
import net.wesjd.lavarun.plugin.command.annotation.Command;
import net.wesjd.lavarun.plugin.im.InManager;
import net.wesjd.lavarun.plugin.region.Region;
import net.wesjd.lavarun.plugin.region.RegionManager;
import net.wesjd.lavarun.plugin.region.creation.RegionCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {


    @Override
    public void onEnable() {
        InManager.get().addInstance(this);
        InManager.get().addInstance(new RegionManager());
        InManager.get().addInstance(new CommandManager());
        InManager.get().getInstance(CommandManager.class).addAllCommands(this);
    }

    @Override
    public void onDisable() {
        InManager.get().clearInstances();
    }

    @Command(name = "test", help = "Lel")
    public boolean onC(Player p, String[] args) {
        RegionCreator.beginPlayer(p, new RegionCreator.CreationListener() {
            @Override
            public void onCreation(Region region) {
                p.sendMessage("Ayyy, you did a thing");
            }

            @Override
            public void onFail() {
                p.sendMessage("Booo!");
            }
        });
        return true;
    }

}
