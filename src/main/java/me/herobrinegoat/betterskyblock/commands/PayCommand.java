package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class PayCommand implements CommandExecutor, TabCompleter {

    BetterSkyblock plugin;

    public PayCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }


    CommandUtil commandUtil = new CommandUtil();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player);

        if (args.length == 0) {
            player.sendMessage(helpCommand());
            return true;
        }
        if (payMoney(user, args)) return true;
        if (sendHelpMessage(user, args)) return true;
        player.sendMessage(ChatUtil.getUnknownCommand("pay"));
        return true;
    }

    public boolean payMoney(User user, String... args) {
        Player player = user.getPlayer();

        if (args.length < 1) {
            return false;
        }

        Player payedPlayer = Bukkit.getPlayer(args[0]);
        if (payedPlayer == null) {
            player.sendMessage(ChatUtil.getPlayerNotOnlineMessage());
            return true;
        }

        User payedUser = plugin.getUserManager().getUser(payedPlayer);

        if (args.length == 1) {
            player.sendMessage(RED + "You must specify an amount!");
            return true;
        }

        if (!ChatUtil.isNum(args[1])) {
            player.sendMessage(RED + "That is not a valid number!");
            return true;
        }
        double amount = Double.parseDouble(args[1]);
        user.payPlayer(payedUser, amount);
        return true;
    }

    public boolean sendHelpMessage(User user, String... args) {
        String cmd = "help";
        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        user.getPlayer().sendMessage(helpCommand());
        return true;
    }

    public String helpCommand() {
        String dashColor = BLUE.toString();
        String dashes = ChatUtil.getDashSeparator(AQUA.toString(), 25);
        return dashes + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/pay help", dashColor, GREEN + "Sends this message") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/pay <player> <amount>", dashColor, GREEN + "Used to payer another player " +
                "the specified money amount") + "\n" +
                dashes;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> commands = new ArrayList<>();
        if (args.length == 1) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!player.getName().equals(sender.getName())) commands.add(player.getName());
            }
        }
        return commands;
    }
}
