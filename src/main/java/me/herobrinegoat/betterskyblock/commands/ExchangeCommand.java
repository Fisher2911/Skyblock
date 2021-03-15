package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ExchangeCommand implements CommandExecutor/*, TabCompleter*/ {

    BetterSkyblock plugin;

    public ExchangeCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                plugin.getExchangeMenu().openPage(player);
                return true;
            }
        }
        return true;
    }
}
