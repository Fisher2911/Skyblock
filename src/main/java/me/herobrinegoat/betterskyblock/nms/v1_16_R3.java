package me.herobrinegoat.betterskyblock.nms;

import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.PacketPlayOutMapChunk;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Container;
import org.bukkit.craftbukkit.v1_16_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
import org.bukkit.entity.Player;

public class v1_16_R3 implements NMS {

    @Override
    public void setBlockInNativeChunk(World world, int x, int y, int z, Material material, boolean applyPhysics) {
        net.minecraft.server.v1_16_R3.World nmsWorld = ((CraftWorld) world).getHandle();
        net.minecraft.server.v1_16_R3.Chunk nmsChunk = nmsWorld.getChunkAt(x >> 4, z >> 4);
        BlockPosition bp = new BlockPosition(x, y, z);
        Location location = new Location(world, x, y, z);
        if (location.getBlock().getState() instanceof Container) {
            Container container = (Container) location.getBlock().getState();
            container.getInventory().clear();
        }
        IBlockData ibd = CraftMagicNumbers.getBlock(material).getBlockData();
        nmsChunk.setType(bp, ibd, applyPhysics);
    }

    @Override
    public void sendChunk(Player player, Chunk chunk) {
        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(((CraftChunk) chunk).getHandle(), 65535);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packetPlayOutMapChunk);
    }
}
