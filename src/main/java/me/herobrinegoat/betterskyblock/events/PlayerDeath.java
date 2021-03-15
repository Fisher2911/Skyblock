package me.herobrinegoat.betterskyblock.events;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.PlaceableGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerDeath implements Listener {

    BetterSkyblock plugin;

    public PlayerDeath(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Inventory inventory = player.getInventory();
        User user = plugin.getUserManager().getUser(player);
        if (user == null || !user.hasIsland()) {
            return;
        }
        Island island = user.getIsland(plugin);
        if (island == null) {
            return;
        }
        Location home = island.getCenter();
        World world = home.getWorld();
        if (world == null) {
            return;
        }
        for (ItemStack item : event.getDrops()) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            if (PlaceableGenerator.isGeneratorItem(plugin, item)) {
                world.dropItem(home, item.clone());
                item.setAmount(0);
            }
        }
        player.sendMessage(ChatColor.YELLOW + "Your important items have been dropped at your island home because you died!");
    }

    @EventHandler
    public void playerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUserManager().getUser(player);
        if (user == null || !user.hasIsland()) {
            return;
        }
        Island island = user.getIsland(plugin);
        if (island == null) {
            return;
        }
        Location home = island.getCenter();
        World world = home.getWorld();
        if (world == null) {
            return;
        }
        BukkitRunnable delay = new BukkitRunnable() {
            @Override
            public void run() {
                player.teleport(home);
            }
        };
        delay.runTaskLater(plugin, 1);
    }

}
