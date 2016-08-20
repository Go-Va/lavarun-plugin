package me.gong.lavarun.plugin.region.creation.box;

import me.gong.lavarun.plugin.util.AxisAlignedBB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class Box {
    private AxisAlignedBB axis;
    private List<Location> renderPoint;
    private int renderIndex1, renderIndex2;
    private final Object renderPointLock = new Object();

    public Box(AxisAlignedBB axis) {
        this.axis = axis;
    }

    public AxisAlignedBB getAxis() {
        return axis;
    }

    public void setAxis(AxisAlignedBB axis) {
        this.axis = axis;
    }

    public List<Location> getRenderPoint() {
        synchronized (renderPointLock) {
            return renderPoint;
        }
    }

    public Location getMinimum(World world) {
        return new Location(world, axis.minX, axis.minY, axis.minZ);
    }

    public Location getMaximum(World world) {
        return new Location(world, axis.maxX, axis.maxY, axis.maxZ);
    }

    public void setRenderPoint(List<Location> renderPoint) {
        synchronized (renderPointLock) {
            this.renderPoint = renderPoint;
            this.renderIndex2 = renderPoint.size() - 1;
        }
    }

    public void scroll() {
        if(renderIndex1 >= renderPoint.size() - 1) {
            renderIndex1 = 0;
        } else renderIndex1++;
        if(renderIndex2 <= 1) {
            renderIndex2 = renderPoint.size() - 1;
        } else renderIndex2--;
    }

    public Location getToRender1() {
        return renderPoint.get(renderIndex1);
    }

    public Location getToRender2() {
        return renderPoint.get(renderIndex2);
    }
}
