package me.gong.lavarun.plugin.options;

import me.gong.lavarun.plugin.InManager;
import me.gong.lavarun.plugin.Main;
import me.gong.lavarun.plugin.command.CommandManager;
import me.gong.lavarun.plugin.options.impl.NightTimeOption;
import me.gong.lavarun.plugin.options.impl.NoHungerOption;
import me.gong.lavarun.plugin.options.impl.SpeedOption;
import me.gong.lavarun.plugin.options.impl.UnlimitedPointsOption;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class OptionsManager {

    private List<Option> options;

    public void onEnable() {
        options = new ArrayList<>();
        options.add(new NoHungerOption());
        options.add(new UnlimitedPointsOption());
        options.add(new SpeedOption());
        options.add(new NightTimeOption());

        JavaPlugin p = InManager.get().getInstance(Main.class);
        options.forEach(o -> Bukkit.getPluginManager().registerEvents(o, p));
        InManager.get().getInstance(CommandManager.class).addAllCommands(new OptionsCmd());
    }

    public void onDisable() {
        options = null;
    }

    public Option getOptionFor(String name) {
        return options.stream().filter(o -> o.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public List<Option> getOptions() {
        return options;
    }
}
