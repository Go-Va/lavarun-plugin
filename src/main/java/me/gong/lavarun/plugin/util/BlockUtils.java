package me.gong.lavarun.plugin.util;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;

public class BlockUtils {

    public static List<Location> outlineBox(AxisAlignedBB bb, World world) {
        return outlineBox(bb, world, 0.1);
    }

    public static List<Location> outlineBox(AxisAlignedBB bb, World world, double inc) {
        return outlineBox(new Location(world, bb.minX, bb.minY, bb.minZ), new Location(world, bb.maxX, bb.maxY, bb.maxZ), inc);
    }

    public static List<Location> outlineBox(Location start, Location end, double inc) {
        AxisAlignedBB bb = new AxisAlignedBB(start.getX(), start.getY(), start.getZ(), end.getX(), end.getY(), end.getZ());
        World w = start.getWorld();
        List<Location> ret = new ArrayList<>();
        //total: 12
        /*
        tbl -> tfl [z] 1 top back left -> top forward left
        
        tbr -> tfr [z] 2 top back right -> top forward right
        
        bbl -> bfl [z] 3 bottom back left -> bottom forward left
        
        bbr -> bfr [z] 4 bottom back right -> bottom forward right
        
        
        tfl -> tfr [x] 1 maxY maxZ minX -> maxY maxZ maxX
        
        bfl -> bfr [x] 2 minY maxZ minX -> minY maxZ maxX
        
        tbl -> tbr [x] 3 maxY minZ minX -> maxY minZ maxX
        
        bbl -> bbr [x] 4 minY minZ minX -> minY minZ maxX

        
        bbl -> tbl [y] 1 minY minZ minX -> maxY minZ minX

        bbr -> tbr [y] 2 minY minZ maxX -> maxY minZ maxX

        bfl -> tfl [y] 3 minY maxZ minX -> maxY maxZ minX

        bfr -> tfr [y] 4 minY maxZ maxX -> maxY maxZ maxX

         */
        
        {//z
            {//1             left          top
                double x = bb.minX, y = bb.maxY;
                for(double z : getBetween(bb.minZ, bb.maxZ, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//2             right          top
                double x = bb.maxX, y = bb.maxY;
                for(double z : getBetween(bb.minZ, bb.maxZ, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//3             left          bottom
                double x = bb.minX, y = bb.minY;
                for(double z : getBetween(bb.minZ, bb.maxZ, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//4             right         bottom
                double x = bb.maxX, y = bb.minY;
                for(double z : getBetween(bb.minZ, bb.maxZ, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }
        }

        {//x
            {//1
                double y = bb.maxY, z = bb.maxZ;
                for(double x : getBetween(bb.minX, bb.maxX, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//2
                double y = bb.minY, z = bb.maxZ;
                for(double x : getBetween(bb.minX, bb.maxX, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//3
                double y = bb.maxY, z = bb.minZ;
                for(double x : getBetween(bb.minX, bb.maxX, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//4
                double y = bb.minY, z = bb.minZ;
                for(double x : getBetween(bb.minX, bb.maxX, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }
        }

        {//y
            {//1
                double x = bb.minX, z = bb.minZ;
                for(double y : getBetween(bb.minY, bb.maxY, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//2
                double x = bb.maxX, z = bb.minZ;
                for(double y : getBetween(bb.minY, bb.maxY, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//3
                double x = bb.minX, z = bb.maxZ;
                for(double y : getBetween(bb.minY, bb.maxY, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }

            {//4
                double x = bb.maxX, z = bb.maxZ;
                for(double y : getBetween(bb.minY, bb.maxY, inc)) {
                    Location l = new Location(w, x, y, z);
                    if(!ret.contains(l)) ret.add(l);
                }
            }
        }
        return ret;
    }
    
    public static List<Double> getBetween(double begin, double end, double inc) {
        List<Double> ret = new ArrayList<>();
        for(double b = begin; b <= end; b+= inc) ret.add(b);
        return ret;
    }
}
