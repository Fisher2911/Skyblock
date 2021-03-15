package me.herobrinegoat.betterskyblock.commands;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.YELLOW;

public class HelpCommand implements CommandExecutor, TabCompleter {

    BetterSkyblock plugin;

    public HelpCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        String dashes = ChatUtil.getDashSeparator(AQUA.toString(), 25);
        sender.sendMessage(dashes + "\n" + YELLOW +
                "/tutorial\n" +
                getHelpMessage("Island") + "\n" +
                getHelpMessage("Generators") + "\n" +
                getHelpMessage("Exchange") + "\n" +
                getHelpMessage("Pay") + "\n" +
                getHelpMessage("Balance") + "\n" +
                dashes);
        return true;
    }

    private String getHelpMessage(String cmd) {
        return YELLOW + cmd + " Command - /" + cmd.toLowerCase() + " help";
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabs = new ArrayList<>();
        if (args.length == 1) {
            if ("help".startsWith(args[0].toLowerCase())) {
                tabs.add("help");
            }
        }
        return tabs;
    }
}
