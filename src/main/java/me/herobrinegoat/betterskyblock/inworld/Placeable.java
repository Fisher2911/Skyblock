package me.herobrinegoat.betterskyblock.inworld;

import org.bukkit.Location;

public interface Placeable {

    Location getLocation();
    void setLocation(Location location);
    boolean place();
    boolean remove();


}
