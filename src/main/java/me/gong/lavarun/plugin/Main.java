package me.gong.lavarun.plugin;

import me.gong.lavarun.plugin.beam.BeamManager;
import me.gong.lavarun.plugin.command.CommandManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.region.RegionRenderer;
import me.gong.lavarun.plugin.scoreboard.ScoreboardManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timers;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        InManager.get().addInstance(this);
        InManager.get().addInstance(new Timers());
        InManager.get().addInstance(new RegionRenderer());
        InManager.get().addInstance(new CommandManager());
        InManager.get().addInstance(new GameManager());
        InManager.get().addInstance(new PowerupManager());
        InManager.get().addInstance(new Config());
        InManager.get().addInstance(new BeamManager());
        InManager.get().addInstance(new ShopManager());
        InManager.get().addInstance(new ScoreboardManager());
    }

    @Override
    public void onDisable() {
        InManager.get().clearInstances();
    }

    public class Config {
        private String username, password;
        private int channel;

        public Config() {
            saveDefaultConfig();
            loadConfig();
        }

        public Config(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public void loadConfig() {
            username = getConfig().getString("username");
            password = getConfig().getString("password");
            channel = getConfig().getInt("channel");
        }

        public void reloadConfig() {
            Main.this.reloadConfig();
            loadConfig();
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public int getChannel() {
            return channel;
        }

        //no saving
    }
    
    

}
