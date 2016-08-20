package me.gong.lavarun.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InManager {
    private static InManager instance = new InManager();

    public static InManager get() {
        return instance;
    }

    private InManager() {

    }

    private List<Object> instances;

    public void clearInstances() {
        getInstances().forEach(s -> {
            try {
                s.getClass().getMethod("onDisable").invoke(s);
            } catch (Exception ex) {
                //no method
            }
        });
        getInstances().clear();
    }

    public <T> T addInstance(T instance) {
        if(!(instance instanceof JavaPlugin)) //boi
            try {
                instance.getClass().getMethod("onEnable").invoke(instance);
            } catch (Exception ex) {
                //no method
            }
        getInstances().add(instance);
        return instance;
    }

    public <T> T getInstance(Class<T> type) {
        //noinspection unchecked
        return (T) getInstances().stream().filter(type::isInstance).findFirst().orElse(null);
    }

    private List<Object> getInstances() {
        //lazy initialization
        if(instances == null) instances = new CopyOnWriteArrayList<>();
        return instances;
    }
}
