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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class PlayerJoin implements Listener {

    private final BetterSkyblock plugin;

    private final UserManager userManager;

    private final IslandManager islandManager;

    public PlayerJoin(BetterSkyblock plugin) {
        this.plugin = plugin;
        this.userManager = plugin.getUserManager();
        this.islandManager = plugin.getIslandManager();
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String joinMessage;
        if (!player.hasPlayedBefore()) {
            joinMessage = Messages.NEW_JOIN.replace("%player%", player.getName());
        } else {
            joinMessage = Messages.PLAYER_JOIN.replace("%player%", player.getName());
        }
        event.setJoinMessage(joinMessage);
        
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    User user = userManager.getUserFromDatabase(player);
                    userManager.addUser(user);
                
                    if (user.hasIsland()) {
                        Island island = islandManager.getIsland(user.getIslandId());
                        if (island == null) {
                            island = islandManager.getIslandFromDB(user.getIslandId());
                            islandManager.getIslands().put(user.getIslandId(), island);
                        }
                        if (!island.hasOnlinePlayersExcluding(player)) {
                            island.setGeneratorDates(LocalDateTime.now());
                        }
                        island.startPlaceableGeneratorUpdater(plugin);
                    }
                    BukkitRunnable sync = new BukkitRunnable() {
                        @Override
                        public void run() {
                            user.setScoreBoard(plugin);
                        }
                    };
                    sync.runTaskLater(plugin, 20);

                }
            };
            r.runTaskAsynchronously(plugin);
            
    }


}
