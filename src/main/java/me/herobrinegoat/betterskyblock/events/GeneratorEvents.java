package me.herobrinegoat.betterskyblock.events;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.api.BlockGeneratorCollectEvent;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.generators.PlaceableGenerator;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.managers.UserManager;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;

import static org.bukkit.ChatColor.*;

public class GeneratorEvents implements Listener {

    BetterSkyblock plugin;
    UserManager userManager;

    public GeneratorEvents(BetterSkyblock plugin) {
        this.plugin = plugin;
        userManager = plugin.getUserManager();
    }

    @EventHandler(ignoreCancelled = true)
    public void itemClick(PlayerInteractEvent event) {

        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            return;
        }

        Player player = event.getPlayer();
        User user = userManager.getUser(player);

        if (event.getClickedBlock() == null) {
            return;
        }

        if (user == null) {
            return;
        }

        if (!user.hasIsland()) {
            return;
        }

        Island island = user.getIsland(plugin);

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();

        Block block = event.getClickedBlock();
        Location location = block.getLocation();

        if (island == null) {
            return;
        }

        PlaceableGenerator generator;

        generator = BlockGenerator.getGeneratorFromItem(item, plugin);

        if (generator == null) {
            generator = MobGenerator.getGeneratorFromItem(item, plugin);
        }

        PlaceableGenerator islandGenerator = island.getGeneratorFromLocation(location);
        if (islandGenerator != null) {
            if (player.isSneaking() && !PlaceableGenerator.isGeneratorItem(plugin, item)) {
                event.setCancelled(true);
                return;
            }
            if (generator != null && PlaceableGenerator.isGeneratorItem(plugin, item) && !islandGenerator.getGeneratedItemStack().equals(generator.getGeneratedItemStack())) {
                event.setCancelled(true);
                return;
            }
            if (!PlaceableGenerator.isGeneratorItem(plugin, item)) {
                openGeneratorPage(player, islandGenerator);
                event.setCancelled(true);
                return;
            }
        }

        if (!event.hasItem() || item == null) {
            return;
        }

        if (!PlaceableGenerator.isGeneratorItem(plugin, item)) {
            return;
        }
        BlockFace face = event.getBlockFace();
        Location relative = location.getBlock().getRelative(face, 1).getLocation();
        if (relative.getBlock().getType() != Material.AIR || location.getBlock().getType().isInteractable() && !player.isSneaking()) {
            if (event.getItem() != null && PlaceableGenerator.isGeneratorItem(plugin, event.getItem())) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);

        if (generator == null) {
            return;
        }

        generator.setLocation(location);

        if (!user.hasPermission(island, "GENERATOR_PLACE", plugin)) {
            return;
        }

        String error = RED + "Error placing generator, please contact a server staff for help!";

        RankManager costLoading = plugin.getRankManager();

        boolean hasCompletedRequirements;

        World world = location.getWorld();
        if (world == null) {
            return;
        }
        if (!world.getNearbyEntities(relative, .75, .75, .75).isEmpty()) {
            return;
        }
            if (generator instanceof MobGenerator) {
                hasCompletedRequirements = costLoading.hasMobRequirements(generator.getRank(), user, true);
                if (!hasCompletedRequirements) {
                    player.sendMessage(Messages.NOT_UNLOCKED_YET);
                    return;
                }
                if (!generator.placeInWorld(face, block.getLocation(), Material.SPAWNER, user, plugin)) {
                    return;
                }
                ((MobGenerator) generator).updateSpawner();
            } else {
                hasCompletedRequirements = costLoading.hasBlockRequirements(generator.getRank(), user, true);
                if (!hasCompletedRequirements) {
                    player.sendMessage(Messages.NOT_UNLOCKED_YET);
                    return;
                }
                if (!generator.placeInWorld(face, block.getLocation(), ((BlockGenerator) generator).getBlock(), user, plugin)) {
                 return;
                }
            }
            generator.setLastGeneratedDate(LocalDateTime.now());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void blockBreakEvent(BlockBreakEvent event) {
        if (event instanceof BlockGeneratorCollectEvent) return;
        Player player = event.getPlayer();
        User user = userManager.getUser(player);

        if (user == null) {
            event.setCancelled(true);
            return;
        }

        if (!user.hasIsland()) {
            return;
        }

        Block block = event.getBlock();
        Location location = block.getLocation();

        if (location.getWorld() == null) {
            return;
        }

        Island island = user.getIsland(plugin);

        PlaceableGenerator generator = island.getGeneratorFromLocation(location);
        if (generator == null) {
            return;
        }

        if (!(generator instanceof BlockGenerator)) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        if (!user.hasPermission(island, "GENERATOR_COLLECT", plugin)) {
            player.sendMessage(Messages.NO_PERMISSION_TO_COLLECT);
            return;
        }
        RankManager rankManager = plugin.getRankManager();
        if (!rankManager.hasBlockRequirements(generator.getRank(), user, true)) {
            player.sendMessage(Messages.NOT_UNLOCKED_YET);
            return;
        }
        BlockGenerator blockGenerator = (BlockGenerator) generator;
        blockGenerator.generate();
        String message = WHITE.toString() + BOLD + "[" + AQUA + BOLD + "Resources Generated" + WHITE + BOLD + "]" +
                DARK_GREEN + " - " + WHITE.toString() + BOLD + "[" + "REPLACE" + WHITE + BOLD +
                "]";
        int resourcesGenerated = blockGenerator.getTotalResources();
        if (resourcesGenerated -1 < 1) {
            message = message.replace("REPLACE", RED.toString() + BOLD + 0);
            location.getBlock().setType(Material.BEDROCK);
        } else {
            message = message.replace("REPLACE", GREEN.toString() + BOLD + (resourcesGenerated - 1));
        }
        if (resourcesGenerated == 0) {
            ChatUtil.sendActionBar(player, message);
            return;
        }
        blockGenerator.setTotalResources(resourcesGenerated - 1, false);
        World world = location.getWorld();
        location.setY(location.getBlockY() + 1);
        BlockGeneratorCollectEvent blockGeneratorCollectEvent = new BlockGeneratorCollectEvent(block, player, blockGenerator, user, 1, player.getInventory().getItemInMainHand());
        Bukkit.getPluginManager().callEvent(blockGeneratorCollectEvent);
        ItemStack collectedItem = blockGenerator.getGeneratedItemStack().clone();
        if (blockGeneratorCollectEvent.getResourcesCollected() > 0) {
            collectedItem.setAmount(blockGeneratorCollectEvent.getResourcesCollected());
            world.dropItem(location, collectedItem);
        }
        ChatUtil.sendActionBar(player, message);
        user.addBlockItemToRequirements(blockGenerator.getGeneratedItemStack());
    }

    void openGeneratorPage(Player player, PlaceableGenerator generator) {
        generator.openPage(player, plugin);
    }
}
