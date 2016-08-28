package me.gong.lavarun.plugin;

import me.gong.lavarun.plugin.beam.BeamManager;
import me.gong.lavarun.plugin.command.CommandManager;
import me.gong.lavarun.plugin.game.GameManager;
import me.gong.lavarun.plugin.options.OptionsManager;
import me.gong.lavarun.plugin.powerup.PowerupManager;
import me.gong.lavarun.plugin.region.RegionRenderer;
import me.gong.lavarun.plugin.scoreboard.ScoreboardManager;
import me.gong.lavarun.plugin.shop.ShopManager;
import me.gong.lavarun.plugin.timer.Timers;
import me.gong.lavarun.plugin.tutorial.TutorialManager;
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
        InManager.get().addInstance(new OptionsManager());
        InManager.get().addInstance(new TutorialManager());
    }

    @Override
    public void onDisable() {
        InManager.get().clearInstances();
    }

    public class Config {
        private String serverIP;
        private int port;

        public Config() {
            saveDefaultConfig();
            loadConfig();
        }

        public Config(String serverIP, int port) {
            this.serverIP = serverIP;
            this.port = port;
        }

        public void loadConfig() {
            serverIP = getConfig().getString("serverIP");
            port = getConfig().getInt("port");
        }

        public void reloadConfig() {
            Main.this.reloadConfig();
            loadConfig();
        }

        public String getServerIP() {
            return serverIP;
        }

        public int getPort() {
            return port;
        }
    }
    
    

}
