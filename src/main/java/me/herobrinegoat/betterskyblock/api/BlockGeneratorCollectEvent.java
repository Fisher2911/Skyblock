package me.herobrinegoat.betterskyblock.api;

import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

public class BlockGeneratorCollectEvent extends BlockBreakEvent {

    private final BlockGenerator blockGenerator;
    private final User user;
    private int resourcesCollected;
    private final ItemStack tool;

    public BlockGeneratorCollectEvent(Block theBlock, Player player, BlockGenerator blockGenerator, User user, int resourcesCollected, ItemStack tool) {
        super(theBlock, player);
        this.blockGenerator = blockGenerator;
        this.user = user;
        this.resourcesCollected = resourcesCollected;
        this.tool = tool;
    }

    public BlockGenerator getGenerator() {
        return blockGenerator;
    }

    public User getUser() {
        return user;
    }

    public int getResourcesCollected() {
        return resourcesCollected;
    }

    public void setResourcesCollected(int resourcesCollected) {
        this.resourcesCollected = resourcesCollected;
    }

    public ItemStack getTool() {
        return tool;
    }
}
