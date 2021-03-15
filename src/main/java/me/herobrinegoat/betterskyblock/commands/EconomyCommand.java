package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.bukkit.ChatColor.*;

public class EconomyCommand implements CommandExecutor, TabCompleter {

    BetterSkyblock plugin;

    public EconomyCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    CommandUtil commandUtil = new CommandUtil();


    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (addBalance(sender, args)) return true;
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player);

        if (args.length == 0) {
            player.sendMessage(AQUA + "Balance - " + GREEN + "$" + plugin.getEconomySetup().getBalance(player.getName()));
            return true;
        }

        if (sendHelpMessage(user, args)) return true;
        if (setBalance(user, args)) return true;
        if (sendTopTenBalances(player, args)) return true;
        if (getOtherPlayerBalance(user, args)) return true;
        player.sendMessage(ChatUtil.getUnknownCommand("balance"));
        return true;
    }

    public boolean getOtherPlayerBalance(User user, String... args) {
        Player player = user.getPlayer();

        if (args.length != 1) {
            return false;
        }

        Player checkedPlayer = Bukkit.getPlayer(args[0]);
        if (checkedPlayer == null) {
            player.sendMessage(ChatUtil.getPlayerNotOnlineMessage());
            return true;
        }

        User checkBalance = plugin.getUserManager().getUser(checkedPlayer);
        player.sendMessage(AQUA + checkBalance.getPlayerName() + "'s balance - " + GREEN + "$" + checkBalance.getBalance());
        return true;
    }

    public boolean sendTopTenBalances(Player player, String... args) {
        if (args.length != 1) {
            return false;
        }

        if (!args[0].equalsIgnoreCase("top")) {
            return false;
        }

        Map<String, Integer> topBalances = plugin.getUserManager().getTopTenBalances();

        if (topBalances == null || topBalances.isEmpty()) {
            player.sendMessage(RED + "There are no players!");
            return true;
        }
        int place = 1;
        player.sendMessage(ChatUtil.getDashSeparator(AQUA.toString(), 30));
        for (Map.Entry<String, Integer> entry : topBalances.entrySet()) {
            String name = entry.getKey();
            double balance = entry.getValue();
            player.sendMessage(GREEN.toString() + place + ": " + AQUA + name + GREEN + " - $" + balance);
            place++;
        }
        player.sendMessage(ChatUtil.getDashSeparator(AQUA.toString(), 30));
        return true;
    }

    public boolean addBalance(CommandSender sender, String... args) {
        String cmd = "add";

        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) return false;
        if (!sender.hasPermission("skyblock.setmoney")) {
            sender.sendMessage(Messages.NO_PERMISSION);
            return true;
        }
        if (commandUtil.isLengthAndArg(1, cmd, args)) {
            sender.sendMessage(RED + "You must specify a player and a balance");
            return true;
        }

        if (commandUtil.isArgAndGreaterThan(1, cmd, args)) {
            Player setPlayer = Bukkit.getPlayer(args[1]);
            if (setPlayer == null) {
                sender.sendMessage(ChatUtil.getPlayerNotOnlineMessage());
                return true;
            }
            if (commandUtil.isLengthAndArg(3, cmd, args)) {

                if (!ChatUtil.isNum(args[2])) {
                    sender.sendMessage(RED + "That is not a valid number!");
                    return true;
                }

                User setUser = plugin.getUserManager().getUser(setPlayer);
                double setMoney = Double.parseDouble(args[2]);
                double money = setMoney + setUser.getBalance();
                setUser.setBalance(money);
                setUser.getPlayer().sendMessage(GREEN + String.valueOf(setMoney) + "$ has been added to your balance.");
                sender.sendMessage(GREEN + String.valueOf(setMoney) + "$ has been added to " + setUser.getPlayerName() + "'s balance.");
                return true;
            }
        }
        return true;
    }

    public boolean setBalance(User user, String... args) {
        String cmd = "set";

        Player player = user.getPlayer();

        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) return false;
        if (!player.hasPermission("skyblock.setmoney")) {
            player.sendMessage(Messages.NO_PERMISSION);
            return true;
        }
        if (commandUtil.isLengthAndArg(1, cmd, args)) {
            player.sendMessage(RED + "You must specify a player and a balance");
            return true;
        }

        if (commandUtil.isArgAndGreaterThan(1, cmd, args)) {
            Player setPlayer = Bukkit.getPlayer(args[1]);
            if (setPlayer == null) {
                player.sendMessage(ChatUtil.getPlayerNotOnlineMessage());
                return true;
            }
            if (commandUtil.isLengthAndArg(3, cmd, args)) {

                if (!ChatUtil.isNum(args[2])) {
                    player.sendMessage(RED + "That is not a valid number!");
                    return true;
                }

                double money = Double.parseDouble(args[2]);

                User setUser = plugin.getUserManager().getUser(setPlayer);
                setUser.setBalance(money);
                setUser.getPlayer().sendMessage(GREEN + "Your balance has been set to $" + money);
                player.sendMessage(GREEN + "You have set " + setUser.getPlayerName() + "'s balance to $" + money);
                return true;
            }
        }
        return true;
    }

    public boolean sendHelpMessage(User user, String... args) {
        String cmd = "help";

        Player player = user.getPlayer();
        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) return false;
        player.sendMessage(helpCommand());
        return true;
    }

    public String helpCommand() {
        String dashColor = BLUE.toString();
        String dashes = ChatUtil.getDashSeparator(AQUA.toString(), 25);
        return dashes + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/balance help", dashColor, GREEN + "Sends this message") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/balance", dashColor, GREEN + "Sends your balance") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/balance <player>", dashColor, GREEN + "Sends the specified " +
                "player's balance") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/balance top", dashColor, GREEN + "Sends the top ten balances" +
                        " of players on the server") + "\n" +
                dashes;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> tabs = new ArrayList<>();
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;
        if (args.length == 1) {
            tabs.add("top");
            tabs.add("help");
            for (Player p : Bukkit.getOnlinePlayers()) {
                tabs.add(p.getName());
            }
            if (player.hasPermission("skyblock.setmoney")) {
                tabs.add("set");
            }
            tabs.removeIf(tab -> !tab.toLowerCase().startsWith(args[0].toLowerCase()));
        } else if (args.length == 2) {
            if (!player.hasPermission("skyblock.setmoney")) {
                return null;
            }
            if (args[0].equals("set")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    tabs.add(p.getName());
                }
            }
            tabs.removeIf(tab -> !tab.toLowerCase().startsWith(args[1].toLowerCase()));
        }
        Collections.sort(tabs);
        return tabs;
    }
}
