package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class TutorialCommand implements CommandExecutor, TabCompleter {

    BetterSkyblock plugin;

    public TutorialCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    private final String dashes = ChatUtil.getDashSeparator(GOLD.toString(), 20);

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }
        Player player = (Player) sender;
        if (args.length == 0) {
            sendFirstMessage(player);
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("generators")) {
                sendGeneratorHelp(player);
            } else if (args[0].equalsIgnoreCase("blockgenerators")) {
                sendBlockGeneratorHelp(player);
            } else if (args[0].equalsIgnoreCase("inventorygenerators")) {
                sendInventoryGeneratorHelp(player);
            } else if (args[0].equalsIgnoreCase("exchange")) {
                sendExchangeHelp(player);
            }
        }

        return true;
    }

    public void sendFirstMessage(Player player) {
        player.sendMessage(dashes);
        player.sendMessage(YELLOW + "To start off, do " + GREEN + " /is create " + YELLOW + "to create your own island, or " + GREEN + "/is join " + AQUA + "<player>" + YELLOW +
                " to join another player's island if they invite you. If you decide to create your own island, in your island chest you will find two " + AQUA + "generators" + YELLOW + "." +
                " Generators are a major part of this game, and you can use them to get money, make tools, weapons, armor, and more. To see recipes you can craft, use the command /recipes." +
                " Use /tutorial generators to learn more about them, or click the next section message in the chat.");
        TextComponent message = new TextComponent("\n" + GREEN + ">Next Section<");
        message.setColor(ChatColor.GREEN);
        message.setBold(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tutorial generators"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to learn more about generators").color(ChatColor.GRAY).italic(true).create()));
        player.spigot().sendMessage(message);
        player.sendMessage(dashes);
    }

    public void sendGeneratorHelp(Player player) {
        player.sendMessage(dashes);
        player.sendMessage(YELLOW + "There are three types of generators, " + AQUA + "inventory generators, block generators, and mob generators " + YELLOW + "(a.k.a spawners). " +
                "Block generators and mob spawners will " + BOLD + "not " + RESET + YELLOW + " work while you are offline, but inventory generators " + BOLD + "will" + RESET + YELLOW + ". " +
                "You can learn more about block generators with the command /tutorial blockgenerators, or by clicking the next section message.!");
        TextComponent message = new TextComponent("\n" + GREEN + ">Next Section<");
        message.setColor(ChatColor.GREEN);
        message.setBold(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tutorial blockgenerators"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to learn more about block generators").color(ChatColor.GRAY).italic(true).create()));
        player.spigot().sendMessage(message);
        player.sendMessage(dashes);
    }

    public void sendBlockGeneratorHelp(Player player) {
        player.sendMessage(dashes);
        player.sendMessage(YELLOW + "Generators can be upgraded by right clicking them, and clicking the anvil. It requires a certain amount of items and a certain amount of money, and will increase speed, amount of" +
                "generators that can be placed in the block, and amount of blocks/mobs that can be spawned/generated. Generators are unlocked by killing a certain amount of mobs, or breaking a certain " +
                "amount of blocks. You can see how many you have left on the scoreboard on the right side of your screen. The number will " + BOLD + "only" + RESET + YELLOW + " go up when the item you get " +
                "is from a generator. Once you get the required amount, you receive a generator of the next level. You can can learn about inventory generators by using the command " + AQUA + "/tutorial inventorygenerators " +
                YELLOW + "or by clicking the next section message.");
        TextComponent message = new TextComponent("\n" + GREEN + ">Next Section<");
        message.setColor(ChatColor.GREEN);
        message.setBold(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tutorial inventorygenerators"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to learn more about inventory generators").color(ChatColor.GRAY).italic(true).create()));
        player.spigot().sendMessage(message);
        player.sendMessage(dashes);
    }

    public void sendInventoryGeneratorHelp(Player player) {
        player.sendMessage(dashes);
        player.sendMessage( YELLOW + "You can unlock inventory generators by using " + AQUA + " /generators" + YELLOW + ", and upgrading them with the required money and items. " +
                "When you upgrade them, the speed and amount of items they can hold will increase. To get generators, you can trade in items to get them. Use " + AQUA + "/tutorial exchange" + YELLOW + " " +
                "or click the next section button in chat.");
        TextComponent message = new TextComponent("\n" + GREEN + ">Next Section<");
        message.setColor(ChatColor.GREEN);
        message.setBold(true);
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tutorial exchange"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Click here to learn more about trading items for generators").color(ChatColor.GRAY).italic(true).create()));
        player.spigot().sendMessage(message);
        player.sendMessage(dashes);
    }

    public void sendExchangeHelp(Player player) {
        player.sendMessage(dashes);
        player.sendMessage(YELLOW + "You can trade in items for generators with the command " + AQUA + "/exchange" + YELLOW + ". You will only" +
                " be able to place them if you unlocked them, however, so you cannot get them from other people giving you items. " + GREEN +
                "Congratulations! You have finished the tutorial! If you have any questions, " +
                "feel free to ask in chat, or join the discord " +
                "with /discord for help.");
        player.sendMessage(dashes);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) {
            return null;
        }
        List<String> tabs = new ArrayList<>();
        tabs.add("generators");
        tabs.add("blockgenerators");
        tabs.add("inventorygenerators");
        tabs.add("exchange");
        tabs.removeIf(tab -> !tab.toLowerCase().startsWith(args[0].toLowerCase()));
        return tabs;
    }
}
