package me.herobrinegoat.betterskyblock.nms;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public interface NMS {
    void setBlockInNativeChunk(World world, int x, int y, int z, Material material, boolean applyPhysics);
    //void setBlockInNativeChunk(World world, int x, int y, int z, int blockId, byte data, boolean applyPhysics);
    void sendChunk(Player player, Chunk chunk);
}
