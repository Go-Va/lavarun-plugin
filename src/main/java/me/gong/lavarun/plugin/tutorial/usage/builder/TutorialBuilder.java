package me.gong.lavarun.plugin.tutorial.usage.builder;

import me.gong.lavarun.plugin.tutorial.data.Tutorial;
import me.gong.lavarun.plugin.tutorial.data.TutorialPoint;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TutorialBuilder {
    private List<TutorialElement> elements;
    private List<TutorialPoint> points;
    private int progress;

    public TutorialBuilder(List<TutorialElement> elements) {
        this.elements = elements;
        this.points = new ArrayList<>();
    }

    public void addElements(List<TutorialElement> element) {
        this.elements.addAll(element);
    }

    public void addElements(TutorialElement... elements) {
        this.elements.addAll(Arrays.stream(elements).collect(Collectors.toList()));
    }

    public boolean progress(Location location) {
        if(progress >= elements.size() - 1) return false;
        createPoint(location);
        progress++;
        boolean use = false;
        while (!elements.get(progress).useLocation()) {
            use = progress(null);
            if(!use) break;
        }
        return use;
    }

    public List<TutorialElement> getElements() {
        return elements;
    }

    public void createPoint(Location location) {
        points.add(elements.get(progress).createPoint(location));
    }

    public Tutorial build() {
        return new Tutorial(points);
    }
}
