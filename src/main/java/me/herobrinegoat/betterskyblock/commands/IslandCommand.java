package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.inworld.Region;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.CommandUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class IslandCommand implements CommandExecutor, TabCompleter {

    BetterSkyblock plugin;

    public IslandCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    CommandUtil commandUtil = new CommandUtil();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            User user = plugin.getUserManager().getUser(player);

            if (user == null) {
                player.sendMessage(RED + "Please wait a couple seconds, your data is loading.");
                return true;
            }

            if (!user.isOnline()) {
                return true;
            }

            if (args.length == 0) {
                sendDefaultCommand(user);
                return true;
            }

            if (deleteIsland(user, args)) return true;
            if (createIsland(user, args)) return true;
            if (openIslandMenu(user, false, args)) return true;
            if (invitePlayer(user, args)) return true;
            if (joinIsland(user, args)) return true;
            if (kickPlayer(user, args)) return true;
            if (leaveIsland(user, args)) return true;
            if (teleportHome(user, args)) return true;
            if (sendIslandMembers(user, args)) return true;
            if (setIslandHome(user, args)) return true;
            if (sendHelpMessage(user, args)) return true;
            if (setIslandWarp(user, args)) return true;
            if (visitIsland(user, args)) return true;
            if (sendTopIslands(user, args)) return true;
            player.sendMessage(ChatUtil.getUnknownCommand("island"));
        }
        return true;
    }

    private void sendDefaultCommand(User user) {
        if (user.hasIsland()) {
            openIslandMenu(user, true);
            return;
        }
        sendCreateIslandCommandHelp(user);
    }

    private void sendCreateIslandCommandHelp(User user) {
        user.getPlayer().sendMessage(YELLOW + "Use /island create to join a island or /island join <player> to join another player's island, if you are invited.");
    }

    //isDefault = true if opening from sendDefaultCommand();
    private boolean openIslandMenu(User user, boolean isDefault, String... args) {
        if (!isDefault) {
            if (!commandUtil.isLengthAndArg(1, "menu", args)) {
                return false;
            }
        }
        if (user.hasIsland()) {
            Island island = user.getIsland(plugin);
            island.getIslandPage(plugin).openPage(user.getPlayer());
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    island.getIslandPage(plugin).updateInventory(user.getPlayer());
                }
            };
            runnable.runTaskLater(plugin, 10);
            return true;
        }
        sendDefaultCommand(user);
        return true;
    }

    private boolean createIsland(User user, String... args) {
        if (!commandUtil.isLengthAndArg(1, "create", args)) {
            return false;
        }
        Player player = user.getPlayer();
        if (!user.hasIsland()) {
            Region region = plugin.getNextIslandLocation();
            int id = plugin.getIslandManager().getId();
            plugin.getIslandManager().setNextId();
            Island island = new Island(id, new HashSet<>(), region, new HashSet<>(), 0, user.getPlayerUUIDAsString(), user.getPlayerName(),
                    null, null,
                    new ArrayList<>());
            island.create(plugin, user);
            return true;
        }
        player.sendMessage(RED + "You already are in a island!");
        return true;
    }

    private boolean deleteIsland(User user, String... args) {
        if (!commandUtil.isLengthAndArg(1, "delete", args)) {
            return false;
        }
        Player player = user.getPlayer();
        if (user.hasIsland()) {
            Island island = user.getIsland(plugin);
            island.deleteIsland(user, plugin);
            return true;
        }
        player.sendMessage(RED + "You are not in a island!");
        return true;
    }

    private boolean invitePlayer(User user, String... args) {
        String cmd = "invite";
        Player player = user.getPlayer();

      if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) {
          return false;
      }

        if (!user.hasIsland()) {
            player.sendMessage(Messages.NO_ISLAND);
            return true;
        }

        Island island = user.getIsland(plugin);

      if (commandUtil.isLengthAndArg(1, cmd, args)) {
          player.sendMessage(RED + "Please specify a player to invite!");
          return true;
      }

        Player invitePlayer = Bukkit.getPlayer(args[1]);
        if (!isPlayerOnline(player, invitePlayer)) {
            return true;
        }

        User inviteUser = plugin.getUserManager().getUser(invitePlayer);
        island.inviteMember(user, inviteUser, plugin);
        return true;
    }

    private boolean joinIsland(User user, String... args) {
        String cmd = "join";
        Player player = user.getPlayer();

        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) {
            return false;
        }

        if (user.hasIsland()) {
            player.sendMessage(Messages.ALREADY_HAS_ISLAND);
            return true;
        }

        if (!commandUtil.isLengthAndArg(2, cmd, args)) {
            player.sendMessage(RED + "Incorrect command usage, use /island join <player>");
            return true;
        }
        user.joinIsland(args[1], plugin);
        return true;
    }

    private boolean kickPlayer(User user, String... args) {
        String cmd = "kick";
        Player player = user.getPlayer();

        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) {
            return false;
        }

        if (!user.hasIsland()) {
            ChatUtil.sendNoIsland(player);
        }

        if (!commandUtil.isLengthAndArg(2, cmd, args)) {
            player.sendMessage(RED + "Incorrect command usage, use /island kick <player>");
            return true;
        }

        Island island = user.getIsland(plugin);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> island.kickMember(user, plugin.getUserManager().getUUIDFromName(args[1]), plugin));
        return true;
    }

    private boolean leaveIsland(User user, String... args) {
        String cmd = "leave";
        Player player = user.getPlayer();

        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }

        if (!user.hasIsland()) {
            ChatUtil.sendNoIsland(player);
            return true;
        }
        user.leaveIsland(plugin);
        return true;
    }

    public boolean teleportHome(User user, String... args) {
        Player player = user.getPlayer();
        String cmd = "home";
        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        if (!user.hasIsland()) {
            player.sendMessage(Messages.NO_ISLAND);
            return true;
        }

        Island island = user.getIsland(plugin);
        island.teleportToArea(user, plugin, island.getHome());
        return true;
    }

    public boolean sendIslandMembers(User user, String... args) {
        String cmd = "members";
        Player player = user.getPlayer();
        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        if (!user.hasIsland()) {
            ChatUtil.sendNoIsland(player);
            return true;
        }
        user.listIslandMembers(plugin);
        return true;
    }

    public boolean setIslandHome(User user, String... args) {
        String cmd = "sethome";
        Player player = user.getPlayer();
        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        if (!user.hasIsland()) {
            ChatUtil.sendNoIsland(player);
            return true;
        }
        if (!user.isOwner()) {
            player.sendMessage(Messages.NO_PERMISSION);
            return true;
        }
        Location location = player.getLocation();
        if (location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            player.sendMessage(Messages.UNSAFE_AREA);
            return true;
        }
        user.getIsland(plugin).setHome(location);
        player.sendMessage(YELLOW + "Your new island home has been set.");
        return true;
    }

    public boolean setIslandWarp(User user, String... args) {
        String cmd = "setwarp";
        Player player = user.getPlayer();
        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        if (!user.hasIsland()) {
            ChatUtil.sendNoIsland(player);
            return true;
        }
        if (!user.isOwner()) {
            player.sendMessage(Messages.NO_PERMISSION);
            return true;
        }
        Location location = player.getLocation();
        if (location.getBlock().getRelative(BlockFace.DOWN).getType() == Material.AIR) {
            player.sendMessage(Messages.UNSAFE_AREA);
            return true;
        }
        user.getIsland(plugin).setWarp(location);
        player.sendMessage(YELLOW + "Your new island warp has been set.");
        return true;
    }

    public boolean visitIsland(User user, String... args) {
        String cmd = "visit";
        Player player = user.getPlayer();

        if (!commandUtil.isArgAndGreaterThan(0, cmd, args)) {
            return false;
        }
        if (args.length == 1) {
            player.sendMessage(Messages.SPECIFY_PLAYER);
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Messages.SPECIFY_PLAYER);
            return true;
        }

        OfflinePlayer offlinePlayer = Bukkit.getPlayer(args[1]);

        if (offlinePlayer == null) {
            offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
        }

        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            player.sendMessage(Messages.PLAYER_NOT_ONLINE);
            return true;
        }

        User visitUser = plugin.getUserManager().getUser(offlinePlayer);
        if (visitUser == null) {
            visitUser = plugin.getUserManager().getUserFromDatabase(offlinePlayer);
        }

        if (visitUser == null) {
            player.sendMessage(Messages.PLAYER_NOT_ONLINE);
            return true;
        }

        if (!visitUser.hasIsland()) {
            player.sendMessage(Messages.NO_ISLAND);
            return true;
        }

        Island island = visitUser.getIsland(plugin);
        if (island == null) {
            island = plugin.getIslandManager().getIslandFromDB(visitUser.getIslandId());
            plugin.getIslandManager().getIslands().put(visitUser.getIslandId(), island);
        }

        if (!user.hasPermission(island, "TELEPORT", plugin)) {
            player.sendMessage(Messages.ISLAND_LOCKED);
            return true;
        }
        island.teleportToArea(user, plugin, island.getWarp());
        return true;
    }

    public boolean sendTopIslands(User user, String... args) {
        String cmd = "top";
        Player player = user.getPlayer();

        if (!commandUtil.isLengthAndArg(1, cmd, args)) {
            return false;
        }
        if (plugin.getIslandManager().getTopTenPage() == null) {
            return true;
        }
        plugin.getIslandManager().getTopTenPage().openPage(player);
        return true;
    }

    private boolean isPlayerOnline(Player player, Player checkPlayer) {
        if (checkPlayer == null) {
            player.sendMessage(ChatUtil.getPlayerNotOnlineMessage());
            return false;
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
                YELLOW + ChatUtil.getHelpMessage("/island help", dashColor, GREEN + "Sends this message") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island", dashColor, GREEN + "Opens your island menu if you have one") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island menu", dashColor, GREEN + "Opens your island menu") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island create", dashColor, GREEN + "Creates an island for you if you do " +
                "not have one") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island home", dashColor, GREEN + "Teleports you to your island home") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island invite <player>", dashColor, GREEN + "Invites a player to your island") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island join <player>", dashColor, GREEN + "Joins an island if you have been invited") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island visit <player>", dashColor, GREEN + "Teleports you to an island") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island kick <player>", dashColor, GREEN + "Kicks a player from your island") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island delete", dashColor, GREEN + "Deletes your island") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island members", dashColor, GREEN + "Lists your island members") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island sethome", dashColor, GREEN + "Sets your island teleport point") + "\n" +
                YELLOW + ChatUtil.getHelpMessage("/island setwarp", dashColor, GREEN + "Sets your island visitor teleport point") + "\n" +
                dashes;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return null;
        }
        Player player = (Player) sender;
        User user = plugin.getUserManager().getUser(player);
        List<String> tabs = new ArrayList<>();
        if (args.length == 1) {
            tabs.add("help");
            tabs.add("join");
            tabs.add("visit");
            if (!user.hasIsland()) {
                tabs.add("create");
            } else {
                tabs.add("menu");
                tabs.add("members");
                tabs.add("home");

                if (user.isOwner()) {
                    tabs.add("invite");
                    tabs.add("kick");
                    tabs.add("delete");
                    tabs.add("sethome");
                    tabs.add("setwarp");
                }
            }
            tabs.removeIf(tab -> !tab.toLowerCase().startsWith(args[0].toLowerCase()));
        } else if (args.length == 2) {
            if (args[0].equals("invite") || args[0].equals("kick") || args[0].equals("join") || args[0].equals("visit")) {
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
