package me.herobrinegoat.betterskyblock.utils;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

import static org.bukkit.ChatColor.*;

public class ChatUtil {

    public static String getFancyPluginName() {
        return (GOLD + "Fishy Skyblock" + RESET);
    }

    public static String getFancyStringFromList(List<String> list, String listName, String startColor, String itemsColor) {
        StringBuilder sorted = new StringBuilder(startColor + listName + itemsColor + " ");
        for (String string : list) {
            sorted.append(string);
            if (list.indexOf(string) < list.size() - 1) {
                sorted.append(", ");
            }
        }
        return sorted.toString();
    }

    public static String capitalize(String string) {
        if (!string.contains("_") && (!string.equals(string.toUpperCase())))
            return string;
        StringBuilder finalString = new StringBuilder();
        string = string.toLowerCase();
        if (string.contains("_")) {
            String[] splt = string.split("_");
            int i = 0;
            for (String s : splt) {
                i += 1;
                finalString.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
                if (i < splt.length)
                    finalString.append(" ");
            }
        } else {
            finalString.append(Character.toUpperCase(string.charAt(0))).append(string.substring(1));
        }
        return finalString.toString();
    }

    public static String unCapitalize(String string) {
        string = string.toUpperCase();
        return string.replace(" ", "_");
    }

    public static String getTeleportMessage(int timeLeft) {
        return GREEN + "Teleporting in " + (timeLeft) + " seconds";
    }

    public static String getPlayerNotOnlineMessage() {
        return RED + "That player is not online or does not exist!";
    }

    public static void sendNoIsland(Player player) {
        player.sendMessage(RED + "You do not have a island!");
    }

    public static boolean isNum(String num) {
        return NumberUtils.isCreatable(num);
    }

    public static void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static int getRandomNumRange(int min, int max) {
        if (min >= max) {
            return min;
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static String getDashSeparator(String color, int dashAmount) {
        StringBuilder dashes = new StringBuilder(color);
        for (int i = 0; i < dashAmount; i++) {
            dashes.append("-");
        }
        return dashes.toString();
    }

    public static String getHelpMessage(String first, String color, String second) {
        return first + color + " - " + second;
    }

    public static String getUnknownCommand(String cmd) {
        return RED + "Unknown command, please use " + YELLOW + "/help " + cmd;
    }
}
