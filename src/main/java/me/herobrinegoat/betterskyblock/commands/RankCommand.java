package me.herobrinegoat.betterskyblock.commands;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Rank;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Player;

import static org.bukkit.ChatColor.GREEN;
import static org.bukkit.ChatColor.RED;

public class RankCommand implements CommandExecutor {

    BetterSkyblock plugin;

    public RankCommand(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (!sender.hasPermission("betterskyblock.setrank")) {
            sender.sendMessage(Messages.NO_PERMISSION);
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(RED +"You must specify a player!");
            return true;
        }
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            sender.sendMessage(Messages.PLAYER_NOT_ONLINE);
            return true;
        }
        User user = plugin.getUserManager().getUser(player);

        if (user == null) {
            sender.sendMessage(RED + "That player's data is not loaded yet!");
            return true;
        }

        if (args.length == 1) {
            sender.sendMessage(RED + "You must specify a rank!");
            return true;
        }

        String type;

        if (args.length == 2) {
            type = args[1];
            if (!type.equalsIgnoreCase("Block") && !type.equalsIgnoreCase("Mob")) {
                sender.sendMessage(RED + "Invalid type, choose block or mob");
                return true;
            }
            sender.sendMessage(RED + "You must specify a rank number!");
            return true;
        }
        type = args[1].toUpperCase();

        if (args.length == 3) {
            if (!NumberUtils.isCreatable(args[2])) {
                sender.sendMessage(RED + "You must specify a valid number!");
                return true;
            }

            int rankNum = Integer.parseInt(args[2]);
            RankManager rankManager = plugin.getRankManager();
            int currentRank = 1;
            Type type1 = Type.valueOf(type);
            while (currentRank <= rankNum) {
                Rank rank = rankManager.getRank(currentRank, Type.valueOf(type.toUpperCase()));
                if (rank == null) {
                    continue;
                }
                Rank previousRank;
                if (type1 == Type.BLOCK) {
                    BlockGenerator blockGenerator = new BlockGenerator(-1, rank.getItemStack(), 0, null, null, currentRank, 1,
                            plugin.getExchangeItemsConfig().getMaterialFromItem(rank.getItemStack()), null, 1);
                    InventoryUtil.addItemsToInventory(player, blockGenerator.getItemForInventory(plugin), 1);
                    previousRank = rankManager.getRank(currentRank -1, type1);
                    user.getBlockRequirements().put(previousRank.getItemStack(), rank.getRequirement() +1);
                    user.getBlockRequirements().put(rank.getItemStack(), 0);
                } else if (type1 == Type.MOB) {
                    MobGenerator mobGenerator = new MobGenerator(-1, rank.getItemStack(), 0, null, null, currentRank, 1,
                            plugin.getExchangeItemsConfig().getEntityTypeFromItem(rank.getItemStack()), null, 1);
                    InventoryUtil.addItemsToInventory(player, mobGenerator.getItemForInventory(plugin), 1);
                    previousRank = rankManager.getRank(currentRank -1, type1);
                    user.getMobRequirements().put(previousRank.getItemStack(), rank.getRequirement() +1);
                    user.getMobRequirements().put(rank.getItemStack(), 0);
                }
                currentRank++;
            }
            if (type1 == Type.BLOCK) {
                user.setBlockRank(rankNum);
                player.sendMessage(GREEN + "Your block rank has been set to " + rankNum);
            } else if (type1 == Type.MOB) {
                player.sendMessage(GREEN + "Your mob rank has been set to " + rankNum);
                user.setMobRank(rankNum);
            }
            return true;
        }
        return true;
    }
}
