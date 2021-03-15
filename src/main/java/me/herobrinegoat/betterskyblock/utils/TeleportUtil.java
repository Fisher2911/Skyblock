package me.herobrinegoat.betterskyblock.utils;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class TeleportUtil {

    public void teleportIfNotMoving(Player player, Location teleportLoc, int teleportTime, BetterSkyblock plugin) {
        User user = plugin.getUserManager().getUser(player);
        if (user.isTeleporting()) {
            player.sendMessage(RED + "You are already teleporting!");
            return;
        }
        Location playerLoc = player.getLocation();
        int x = playerLoc.getBlockX();
        int y = playerLoc.getBlockY();
        int z = playerLoc.getBlockZ();
        int[] time = {0};
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                user.setTeleporting(true);
                if (x == player.getLocation().getBlockX() && z == player.getLocation().getBlockZ() && y == player.getLocation().getBlockY()) {
                    if (teleportTime - time[0] != 0) player.sendMessage(ChatUtil.getTeleportMessage(teleportTime - time[0]));
                } else {
                    player.sendMessage(RED + "Don't move! Teleport canceled!");
                    user.setTeleporting(false);
                    cancel();
                    return;
                }

                time[0] += 1;
                if (time[0] >= teleportTime + 1) {
                    if (teleportLoc != null) {
                        player.teleport(teleportLoc);
                        player.sendMessage(GREEN + "Teleported!");
                    } else {
                        player.sendMessage(RED + "That location does not exist! Please contact a server staff for help!");
                    }
                    user.setTeleporting(false);
                    cancel();
                }
            }
        };
        r.runTaskTimer(plugin, 0, 20);
    }
}
