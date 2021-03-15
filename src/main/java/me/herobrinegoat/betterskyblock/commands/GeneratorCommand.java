package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.RED;

public class GeneratorCommand implements CommandExecutor {

    BetterSkyblock plugin;

    public GeneratorCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }



    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player);

        if (user == null) {
            return true;
        }

        if (!user.hasIsland()) {
            player.sendMessage(RED + "You do not have a island!");
            return true;
        }

        Island island = user.getIsland(plugin);
        if (island == null) {
            return true;
        }
        island.getGeneratorPage(plugin).openPage(island, player);
        return true;
    }


}
