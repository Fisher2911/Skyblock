package me.herobrinegoat.betterskyblock.managers;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Rank;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;
import me.herobrinegoat.betterskyblock.generators.InventoryGenerator;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.FileUtil;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.GREEN;

public class RankManager {

    private BetterSkyblock plugin;

    public RankManager(BetterSkyblock plugin) {
        this.plugin = plugin;
        this.blockRanks = new LinkedList<>();
        this.mobRanks = new LinkedList<>();
        this.inventoryRanks = new LinkedList<>();
        this.defaultGenerators = new HashSet<>();
    }

    private final LinkedList<Rank> blockRanks;
    private final LinkedList<Rank> mobRanks;
    private final LinkedList<Rank> inventoryRanks;
    private final Set<InventoryGenerator> defaultGenerators;

    private int getItemRequirement(ItemStack item, LinkedList<Rank> ranks) {
        if (ranks == null || ranks.isEmpty()) {
            return -1;
        }
        Collections.sort(ranks);
        int rankNum = ranks.indexOf(new Rank(item, -1, -1));
        if (rankNum == -1) {
            return -1;
        }
        Rank rank = ranks.get(rankNum);
        return rank.getRequirement();
    }

    public Rank getRank(int rank, Type type) {
        if (type == Type.BLOCK) {
            return blockRanks.get(rank);
        } else if (type == Type.MOB) {
            return mobRanks.get(rank);
        }
        return null;
    }

    private Rank getRank(int rank, LinkedList<Rank> ranks) {
        if (rank > ranks.size() -1 || ranks.isEmpty() || rank == -1) {
            return null;
        }
        return ranks.get(rank);
    }

    public int getBlockRequirement(ItemStack itemStack) {
        return getItemRequirement(itemStack, blockRanks);
    }

    public int getMobItemRequirement(ItemStack itemStack) {
        return getItemRequirement(itemStack, mobRanks);
    }

    public boolean hasBlockRequirements(int rankNum, User user, boolean upgrade) {
        return hasRequirements(rankNum, user.getBlockRequirements(), blockRanks, upgrade, user, Type.BLOCK);
    }

    public boolean hasMobRequirements(int rankNum, User user, boolean upgrade) {
        return hasRequirements(rankNum, user.getMobRequirements(), mobRanks, upgrade, user, Type.MOB);
    }

    private boolean hasRequirements(int generatorRankNum, Map<ItemStack, Integer> playersCollectedItems, LinkedList<Rank> ranks, boolean upgrade, User user, Type rankType) {
        Rank nextRank = getRank(generatorRankNum +1, ranks);
        Rank currentRank = getRank(generatorRankNum, ranks);
        if (nextRank == null || currentRank == null) {
            return true;
        }
        if (rankType == Type.BLOCK && user.getBlockRank() > generatorRankNum) {
            return true;
        }
        if (rankType == Type.MOB && user.getMobRank() > generatorRankNum) {
            return true;
        }
        ItemStack itemStack = currentRank.getItemStack();
        if (playersCollectedItems == null || !playersCollectedItems.containsKey(itemStack)) {
            return false;
        }
        int collectedAmount = playersCollectedItems.get(itemStack);
        if (collectedAmount >= nextRank.getRequirement() -1) {
            if (upgrade && user != null) {
                if (isMaxLevel(generatorRankNum, ranks)) {
                    return true;
                }
                if (rankType == Type.BLOCK) {
                    user.setBlockRank(generatorRankNum + 1);
                    BlockGenerator blockGenerator = new BlockGenerator(-1, nextRank.getItemStack(), 0, null, null, generatorRankNum +1, 1,
                            plugin.getExchangeItemsConfig().getMaterialFromItem(nextRank.getItemStack()), null, 1);
                    InventoryUtil.addItemsToInventory(user.getPlayer(), blockGenerator.getItemForInventory(plugin), 1);
                    user.getBlockRequirements().put(nextRank.getItemStack(), 0);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(AQUA + user.getPlayerName() + GREEN + " has collected " + (collectedAmount + 1) + " " + InventoryUtil.getItemName(currentRank.getItemStack()));
                    }
                } else if (rankType == Type.MOB) {
                    user.setMobRank(generatorRankNum + 1);
                    user.getMobRequirements().put(nextRank.getItemStack(), 0);
                    MobGenerator mobGenerator = new MobGenerator(-1, nextRank.getItemStack(), 0, null, null, generatorRankNum +1, 1,
                            plugin.getExchangeItemsConfig().getEntityTypeFromItem(nextRank.getItemStack()), null, 1);
                    InventoryUtil.addItemsToInventory(user.getPlayer(), mobGenerator.getItemForInventory(plugin), 1);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(AQUA + user.getPlayerName() + GREEN + " has collected " + (collectedAmount + 1) + " " + InventoryUtil.getItemName(currentRank.getItemStack()));
                    }
                }
            }
            return true;
        } else {
            int rank;
            if (rankType == Type.BLOCK) {
                rank = user.getBlockRank();
            } else if (rankType == Type.MOB) {
                rank = user.getMobRank();
            } else {
                return true;
            }
            if (currentRank.getRequirement() <= 0 || rank == generatorRankNum) {
                return true;
            }
            return collectedAmount >= currentRank.getRequirement();
            }
    }

    public int getItemRank(ItemStack itemStack, Type type) {
        List<Rank> ranks;
        if (type == Type.BLOCK) {
            ranks = blockRanks;
        } else if (type == Type.MOB) {
            ranks = mobRanks;
        } else {
            return -1;
        }
        for (Rank rank : ranks) {
            if (rank.getItemStack().equals(itemStack)) {
                return ranks.indexOf(rank);
            }
        }
        return -1;
    }

    boolean isMaxLevel(int rank, LinkedList<Rank> ranks) {
        return rank >= ranks.size() - 1;
    }

    public void loadRanks() {
        loadRanks("BlockRanks");
        loadRanks("MobRanks");
        loadRanks("InventoryRanks");
        loadDefaultInventoryGenerators();
    }

    private void loadRanks(String fileName) {
        File file = FileUtil.setupFile(plugin, "GeneratorRanks", fileName);
        FileConfiguration fileData = FileUtil.getFileData(file);

        for (String key : fileData.getKeys(false)) {
            int rankNum = -1;
            if (fileData.getKeys(true).contains(key + ".rank")) rankNum = fileData.getInt(key + ".rank");
            int itemRequirement = fileData.getInt(key + ".requirement");
            ItemStack itemStack = FileUtil.getItemFromFile(key + ".Item", fileData, file, plugin);
            if (itemStack == null) {
                return;
            }
            Rank rank = new Rank(itemStack, rankNum, itemRequirement);
            if (fileName.toLowerCase().contains("block")) {
                blockRanks.add(rank);
            } else if (fileName.toLowerCase().contains("mob")) {
                mobRanks.add(rank);
            } else if (fileName.toLowerCase().contains("inventory")) {
                inventoryRanks.add(rank);
            }
        }
    }

    private void loadDefaultInventoryGenerators() {
        File file = FileUtil.setupFile(plugin, "Generators", "DefaultInventoryGenerators");
        FileConfiguration fileData = FileUtil.getFileData(file);

        for (String key : fileData.getKeys(false)) {
            ItemStack itemStack = FileUtil.getItemFromFile(key + ".Item", fileData, file, plugin);
            if (itemStack == null) {
                continue;
            }
            for (Upgrades upgrades : plugin.getUpgradesConfig().getInventoryGeneratorUpgrades()) {
                if (itemStack.isSimilar(upgrades.getItem())) {
                    defaultGenerators.add(new InventoryGenerator(-1, itemStack, 0, null, upgrades, -1, 0));
                }
            }
        }
    }

    public Set<InventoryGenerator> getDefaultGenerators() {
        return defaultGenerators;
    }

    public ItemStack getItemFromRank(int rank, Type type) {
        List<Rank> ranks;
        if (type == Type.BLOCK) {
            ranks = blockRanks;
        } else if (type == Type.MOB) {
            ranks = mobRanks;
        } else if (type == Type.INVENTORY) {
          ranks = inventoryRanks;
        } else {
            return null;
        }
        if (ranks.size() > rank && ranks.get(rank) != null) {
            return ranks.get(rank).getItemStack();
        }
        return null;
    }



}
