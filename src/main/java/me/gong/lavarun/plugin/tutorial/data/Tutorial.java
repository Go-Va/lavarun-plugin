package me.gong.lavarun.plugin.tutorial.data;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class Tutorial {
    private List<TutorialPoint> points;

    public Tutorial(List<TutorialPoint> points) {
        this.points = points;
    }

    public Tutorial copy() {
        return new Tutorial(points.stream().map(TutorialPoint::copy).collect(Collectors.toList()));
    }

    public List<TutorialPoint> getPoints() {
        return points;
    }

    public JSONArray toJSON() {
        JSONArray ret = new JSONArray();
        points.forEach(p -> ret.add(p.saveToJSON()));
        return ret;
    }

    public static Tutorial fromJSON(JSONArray object) {
        //noinspection unchecked
        return new Tutorial((List<TutorialPoint>) object.stream().map(l -> TutorialPoint.loadFromJSON((JSONObject)l)).collect(Collectors.toList()));
    }
}
