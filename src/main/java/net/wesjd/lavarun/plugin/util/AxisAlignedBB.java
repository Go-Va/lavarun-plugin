package net.wesjd.lavarun.plugin.util;

//took from mcp v1.9 [so no nms needed]
public class AxisAlignedBB {
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AxisAlignedBB(double x1, double y1, double z1, double x2, double y2, double z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);
    }


    public AxisAlignedBB centerOnBlock() {
        return new AxisAlignedBB(((int) minX), ((int) minY), ((int) minZ), ((int) maxX), ((int) maxY), ((int) maxZ));
    }

    public AxisAlignedBB func_186666_e(double y2) {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, y2, this.maxZ);
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof AxisAlignedBB)) {
            return false;
        } else {
            AxisAlignedBB axisalignedbb = (AxisAlignedBB) p_equals_1_;
            return Double.compare(axisalignedbb.minX, this.minX) != 0 ? false : (Double.compare(axisalignedbb.minY, this.minY) != 0 ? false : (Double.compare(axisalignedbb.minZ, this.minZ) != 0 ? false : (Double.compare(axisalignedbb.maxX, this.maxX) != 0 ? false : (Double.compare(axisalignedbb.maxY, this.maxY) != 0 ? false : Double.compare(axisalignedbb.maxZ, this.maxZ) == 0))));
        }
    }

    public int hashCode() {
        long i = Double.doubleToLongBits(this.minX);
        int j = (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minY);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.minZ);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxX);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxY);
        j = 31 * j + (int) (i ^ i >>> 32);
        i = Double.doubleToLongBits(this.maxZ);
        j = 31 * j + (int) (i ^ i >>> 32);
        return j;
    }

    /**
     * Adds the coordinates to the bounding box extending it if the point lies outside the current ranges. Args: x, y, z
     */
    public AxisAlignedBB addCoord(double x, double y, double z) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D) {
            d0 += x;
        } else if (x > 0.0D) {
            d3 += x;
        }

        if (y < 0.0D) {
            d1 += y;
        } else if (y > 0.0D) {
            d4 += y;
        }

        if (z < 0.0D) {
            d2 += z;
        } else if (z > 0.0D) {
            d5 += z;
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Returns a bounding box expanded by the specified vector (if negative numbers are given it will shrink). Args: x,
     * y, z
     */
    public AxisAlignedBB expand(double x, double y, double z) {
        double d0 = this.minX - x;
        double d1 = this.minY - y;
        double d2 = this.minZ - z;
        double d3 = this.maxX + x;
        double d4 = this.maxY + y;
        double d5 = this.maxZ + z;
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    public AxisAlignedBB expandAllAxisesBy(double value) {
        return this.expand(value, value, value);
    }

    public AxisAlignedBB union(AxisAlignedBB other) {
        double d0 = Math.min(this.minX, other.minX);
        double d1 = Math.min(this.minY, other.minY);
        double d2 = Math.min(this.minZ, other.minZ);
        double d3 = Math.max(this.maxX, other.maxX);
        double d4 = Math.max(this.maxY, other.maxY);
        double d5 = Math.max(this.maxZ, other.maxZ);
        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * Offsets the current bounding box by the specified coordinates. Args: x, y, z
     */
    public AxisAlignedBB offset(double x, double y, double z) {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateXOffset(AxisAlignedBB other, double offsetX) {
        if (other.maxY > this.minY && other.minY < this.maxY && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetX > 0.0D && other.maxX <= this.minX) {
                double d1 = this.minX - other.maxX;

                if (d1 < offsetX) {
                    offsetX = d1;
                }
            } else if (offsetX < 0.0D && other.minX >= this.maxX) {
                double d0 = this.maxX - other.minX;

                if (d0 > offsetX) {
                    offsetX = d0;
                }
            }

            return offsetX;
        } else {
            return offsetX;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateYOffset(AxisAlignedBB other, double offsetY) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxZ > this.minZ && other.minZ < this.maxZ) {
            if (offsetY > 0.0D && other.maxY <= this.minY) {
                double d1 = this.minY - other.maxY;

                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.minY >= this.maxY) {
                double d0 = this.maxY - other.minY;

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }

            return offsetY;
        } else {
            return offsetY;
        }
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateZOffset(AxisAlignedBB other, double offsetZ) {
        if (other.maxX > this.minX && other.minX < this.maxX && other.maxY > this.minY && other.minY < this.maxY) {
            if (offsetZ > 0.0D && other.maxZ <= this.minZ) {
                double d1 = this.minZ - other.maxZ;

                if (d1 < offsetZ) {
                    offsetZ = d1;
                }
            } else if (offsetZ < 0.0D && other.minZ >= this.maxZ) {
                double d0 = this.maxZ - other.minZ;

                if (d0 > offsetZ) {
                    offsetZ = d0;
                }
            }

            return offsetZ;
        } else {
            return offsetZ;
        }
    }

    /**
     * Returns whether the given bounding box intersects with this one. Args: axisAlignedBB
     */
    public boolean intersectsWith(AxisAlignedBB other) {
        return this.intersectsWith(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersectsWith(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX && this.minY < maxY && this.maxY > minY && this.minZ < maxZ && this.maxZ > minZ;
    }

    public double getIntersectionArea(AxisAlignedBB other) {
        return this.getIntersectionArea(other.minX, other.minY, other.minZ, other.maxX, other.maxY, other.maxZ);
    }
    
    public double getIntersectionArea(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if(!intersectsWith(minX, minY, minZ, maxX, maxY, maxZ)) return 0;
        double mX = minX > this.minX ? minX - minX : minX - this.minX, 
                mY = minY > this.minY ? minY - minY : minY - this.minY, 
                mZ = minZ > this.minZ ? minZ - minZ : minZ - this.minZ;
        double maX = this.minX > minX ? this.minX - this.minX : this.minX - minX,
                maY = this.minY > minY ? this.minY - this.minY : this.minY - minY,
                maZ = this.minZ > minZ ? this.minZ - this.minZ : this.minZ - minZ;
        Vec3d d1 = new Vec3d(mX, mY, mZ);
        Vec3d d2 = new Vec3d(maX, maY, maZ);
        return d1.distanceTo(d2);
    }

    /**
     * Returns if the supplied Vec3D is completely inside the bounding box
     */
    public boolean isVecInside(Vec3d vec) {
        return (vec.xCoord > this.minX && vec.xCoord < this.maxX) && ((vec.yCoord > this.minY && vec.yCoord < this.maxY) && (vec.zCoord > this.minZ && vec.zCoord < this.maxZ));
    }

    public boolean isVecAlmostInside(Vec3d vec) {
        return (vec.xCoord >= this.minX && vec.xCoord <= this.maxX) && ((vec.yCoord >= this.minY && vec.yCoord <= this.maxY) && (vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ));
    }

    /**
     * Returns the average length of the edges of the bounding box.
     */
    public double getAverageEdgeLength() {
        double d0 = this.maxX - this.minX;
        double d1 = this.maxY - this.minY;
        double d2 = this.maxZ - this.minZ;
        return (d0 + d1 + d2) / 3.0D;
    }

    public AxisAlignedBB expandAlAxisesNegatively(double p_186664_1_) {
        return this.expandAllAxisesBy(-p_186664_1_);
    }

    boolean func_186661_a(Vec3d p_186661_1_, Vec3d p_186661_2_, Vec3d p_186661_3_) {
        return p_186661_2_ == null || p_186661_1_.squareDistanceTo(p_186661_3_) < p_186661_1_.squareDistanceTo(p_186661_2_);
    }

    Vec3d func_186671_a(double p_186671_1_, Vec3d p_186671_3_, Vec3d p_186671_4_) {
        Vec3d vec3d = p_186671_3_.getIntermediateWithXValue(p_186671_4_, p_186671_1_);
        return vec3d != null && this.func_186660_b(vec3d) ? vec3d : null;
    }

    Vec3d func_186663_b(double p_186663_1_, Vec3d p_186663_3_, Vec3d p_186663_4_) {
        Vec3d vec3d = p_186663_3_.getIntermediateWithYValue(p_186663_4_, p_186663_1_);
        return vec3d != null && this.func_186667_c(vec3d) ? vec3d : null;
    }

    Vec3d func_186665_c(double p_186665_1_, Vec3d p_186665_3_, Vec3d p_186665_4_) {
        Vec3d vec3d = p_186665_3_.getIntermediateWithZValue(p_186665_4_, p_186665_1_);
        return vec3d != null && this.func_186669_d(vec3d) ? vec3d : null;
    }

    public boolean func_186660_b(Vec3d p_186660_1_) {
        return p_186660_1_.yCoord >= this.minY && p_186660_1_.yCoord <= this.maxY && p_186660_1_.zCoord >= this.minZ && p_186660_1_.zCoord <= this.maxZ;
    }

    public boolean func_186667_c(Vec3d vec) {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.zCoord >= this.minZ && vec.zCoord <= this.maxZ;
    }

    public boolean func_186669_d(Vec3d vec) {
        return vec.xCoord >= this.minX && vec.xCoord <= this.maxX && vec.yCoord >= this.minY && vec.yCoord <= this.maxY;
    }

    public String toString() {
        return "box[" + this.minX + ", " + this.minY + ", " + this.minZ + " -> " + this.maxX + ", " + this.maxY + ", " + this.maxZ + "]";
    }

    public boolean hasNaN() {
        return Double.isNaN(this.minX) || Double.isNaN(this.minY) || Double.isNaN(this.minZ) || Double.isNaN(this.maxX) || Double.isNaN(this.maxY) || Double.isNaN(this.maxZ);
    }
}
