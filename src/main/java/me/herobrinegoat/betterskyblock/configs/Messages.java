package me.herobrinegoat.betterskyblock.configs;

import me.herobrinegoat.betterskyblock.utils.ChatUtil;

import static org.bukkit.ChatColor.*;

public class Messages {

    public static String NEW_JOIN = YELLOW + "Welcome " + AQUA + "%player% " + YELLOW + " to " + ChatUtil.getFancyPluginName() + "!";
    public static String PLAYER_JOIN = YELLOW + "Welcome back, " + AQUA + "%player%.";
    public static String PLAYER_LEAVE = AQUA + "%player% " + AQUA + "has left the server!";
    public static String NOT_ISLAND_OWNER = RED + "You are not the island owner!";
    public static String NO_ISLAND = RED + "You do not have a island!";
    public static String ALREADY_HAS_ISLAND = RED + "You already have a island!";
    public static String NO_PERMISSION = RED + "You do not have permission to do this!";
    public static String PLAYER_NOT_ONLINE = RED + "That player is not online or does not exist!";
    public static String OTHER_PLAYER_IN_ISLAND = RED + "That player is already in a island!";
    public static String OTHER_PLAYER_NOT_IN_ISLAND = RED + "That player is not in an island!";
    public static String PLAYER_IN_YOUR_ISLAND = RED + "That player is already in your island!";
    public static String NOT_IN_YOUR_ISLAND = RED + "That player is not in your island!";
    public static String ISLAND_DELETED = YELLOW + "Your island has been deleted.";
    public static String UNSAFE_AREA = RED + "That location is not safe!";
    public static String SPECIFY_PLAYER = RED + "You must specify a player!";
    public static String ISLAND_LOCKED = RED + "That island is locked!";
    public static String NO_PERMISSION_TO_COLLECT = RED + "You do not have permission to collect from generators.";
    public static String NOT_UNLOCKED_YET = RED + "You have not unlocked this generator yet.";
    public static String OUT_OF_ISLAND_RANGE = RED + "You are outside of your island range!";

}
