package me.herobrinegoat.betterskyblock.events;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.managers.IslandManager;
import me.herobrinegoat.betterskyblock.managers.UserManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class PlayerLeave implements Listener {

    private final BetterSkyblock plugin;

    private final UserManager userManager;

    private final IslandManager islandManager;

    public PlayerLeave(BetterSkyblock plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.islandManager = plugin.getIslandManager();
    }

    @EventHandler
    public void playerLeaveEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = userManager.getUser(player);
        event.setQuitMessage(Messages.PLAYER_LEAVE.replace("%player%", player.getName()));
        if (user == null) {
            return;
        }
        userManager.removeUser(user);
        user.setLastOnline(LocalDateTime.now());

        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (user.hasIsland()) {
                    Island island = user.getIsland(plugin);
                    if (!island.hasOnlinePlayersExcluding(player)) {
                        if (island.getPlaceableGeneratorUpdater() != null) {
                            island.getPlaceableGeneratorUpdater().cancel();
                            island.setPlaceableGeneratorUpdater(null);
                        }
                    }
                    island.save(plugin.getPool());
                }
                user.save(plugin.getPool());
            }
        };
        r.runTaskAsynchronously(plugin);

    }


}
