package net.wesjd.lavarun.plugin.im;

import java.util.ArrayList;
import java.util.List;

public class InManager {
    private static InManager instance = new InManager();

    public static InManager get() {
        return instance;
    }

    private InManager() {

    }

    private List<Object> instances;

    public void clearInstances() {
        getInstances().clear();
    }

    public void addInstance(Object instance) {
        getInstances().add(instance);
    }

    public <T> T getInstance(Class<T> type) {
        //noinspection unchecked
        return (T) getInstances().stream().filter(type::isInstance).findFirst().orElse(null);
    }

    private List<Object> getInstances() {
        //lazy initialization
        if(instances == null) instances = new ArrayList<>();
        return instances;
    }
}
