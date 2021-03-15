package me.herobrinegoat.betterskyblock.inworld;

import org.bukkit.Location;
import org.bukkit.World;

import java.util.Objects;

public class Region implements Cloneable {

    private World world;

    private int lowerX;

    private int lowerZ;

    private int lowerY;

    private int upperX;

    private int upperY;

    private int upperZ;

    public Region(World world, int lowerX, int upperX, int lowerY, int upperY, int lowerZ, int upperZ) {
        this.world = world;
        this.lowerX = lowerX;
        this.lowerZ = lowerZ;
        this.lowerY = lowerY;
        this.upperX = upperX;
        this.upperY = upperY;
        this.upperZ = upperZ;
    }

    /**
     *
     * @param x - x coord to be checked
     * @param y - y coord to be checked
     * @param z - z coord to be checked
     * @param includeBounds - whether to include corners when checking for location
     * @return true if in bounds, false if not
     */

    public boolean inRegion(double x, double y, double z, boolean includeBounds) {
        if (includeBounds) {
            return (x >= lowerX && x <= upperX && y >= lowerY && y <= upperY && z >= lowerZ && z <= upperZ);
        } else {
            return (x > lowerX && x < upperX && y > lowerY && y < upperY && z > lowerZ && z < upperZ);
        }
    }

    public boolean inRegion(double x, double z, boolean includeBounds, boolean includeY) {
        if (includeBounds) {
            return (x >= lowerX && x <= upperX && z >= lowerZ && z <= upperZ);
        } else {
            return (x > lowerX && x < upperX && z > lowerZ && z < upperZ);
        }
    }

    /**
     *
     * @param location - coordinates to be checked
     * @param includeBounds - whether to include corners when checking for location
     *  @return true if in bounds, false if not
     */

    public boolean inRegion(Location location, boolean includeBounds) {
        return inRegion(location.getX(), location.getY(), location.getZ(), includeBounds);
    }

    public boolean inRegion(Location location, boolean includeBounds, boolean includeY) {
        return inRegion(location.getX(), location.getZ(), includeBounds, includeY);
    }

    public Location getCenter() {
        int centerX = (lowerX + upperX) / 2;
        int centerY = (lowerY + upperY) / 2 + 1;
        int centerZ = (lowerZ + upperZ) / 2;
        return new Location(world, centerX, centerY, centerZ);
    }

    public World getWorld() {
        return world;
    }

    public int getLowerX() {
        return lowerX;
    }

    public int getLowerZ() {
        return lowerZ;
    }

    public int getLowerY() {
        return lowerY;
    }

    public int getUpperX() {
        return upperX;
    }

    public int getUpperY() {
        return upperY;
    }

    public int getUpperZ() {
        return upperZ;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setLowerX(int lowerX) {
        this.lowerX = lowerX;
    }

    public void setLowerZ(int lowerZ) {
        this.lowerZ = lowerZ;
    }

    public void setLowerY(int lowerY) {
        this.lowerY = lowerY;
    }

    public void setUpperX(int upperX) {
        this.upperX = upperX;
    }

    public void setUpperY(int upperY) {
        this.upperY = upperY;
    }

    public void setUpperZ(int upperZ) {
        this.upperZ = upperZ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Region)) return false;
        Region region = (Region) o;
        return lowerX == region.lowerX &&
                lowerZ == region.lowerZ &&
                lowerY == region.lowerY &&
                upperX == region.upperX &&
                upperY == region.upperY &&
                upperZ == region.upperZ &&
                Objects.equals(world, region.world);
    }

    @Override
    public int hashCode() {
        return Objects.hash(world, lowerX, lowerZ, lowerY, upperX, upperY, upperZ);
    }

    public Region clone(){
        try {
            return (Region)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
