package me.gong.lavarun.plugin.tutorial.usage.builder;

import me.gong.lavarun.plugin.tutorial.data.TutorialPoint;
import me.gong.lavarun.plugin.util.StringUtils;
import org.bukkit.Location;

public class TutorialElement {
    private String name, instruction;
    private String finishMessage;
    private int displayTime;
    private boolean useLocation;

    public TutorialElement(boolean useLocation, String name, String instruction, String finishMessage, int displayTime) {
        this.name = StringUtils.format(name);
        this.instruction = instruction;
        this.finishMessage = finishMessage;
        this.displayTime = displayTime;
        this.useLocation = useLocation;
    }

    public TutorialElement(String name, int displayTime) {
        this(false, name, null, null, displayTime);
    }

    public String getName() {
        return name;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getFinishMessage() {
        return finishMessage;
    }

    public boolean useLocation() {
        return useLocation;
    }

    public int getDisplayTime() {
        return displayTime;
    }

    public TutorialPoint createPoint(Location location) {
        return new TutorialPoint(location, displayTime, name);
    }
}
