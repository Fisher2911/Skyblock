package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.utils.TeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {

    BetterSkyblock plugin;

    public SpawnCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        if (args.length > 0) {
            return true;
        }
        Player player = (Player) sender;
        World world = Bukkit.getWorld("SpawnWorld");
        if (world == null) {
            player.sendMessage(ChatColor.RED + "That world does not exist.");
            return true;
        }
        TeleportUtil teleportUtil = new TeleportUtil();
        teleportUtil.teleportIfNotMoving(player, world.getSpawnLocation(), 3, plugin);
        return true;
    }
}
