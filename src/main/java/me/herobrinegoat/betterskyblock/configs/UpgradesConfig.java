package me.herobrinegoat.betterskyblock.configs;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.FileUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class UpgradesConfig {

    private BetterSkyblock plugin;

    private List<Upgrades> blockGeneratorUpgrades;

    private List<Upgrades> mobGeneratorUpgrades;

    private List<Upgrades> inventoryGeneratorUpgrades;

    public UpgradesConfig(BetterSkyblock plugin) {
        this.plugin = plugin;
        this.blockGeneratorUpgrades = new LinkedList<>();
        this.mobGeneratorUpgrades = new LinkedList<>();
        this.inventoryGeneratorUpgrades = new LinkedList<>();
    }

    private void loadInventoryGeneratorUpgrades() {
        List<Upgrades> upgradesList = new ArrayList<>();
        File file = FileUtil.setupFile(plugin, "Generators", "InventoryGeneratorUpgrades");
        FileConfiguration fileData = FileUtil.getFileData(file);
        for (String key : fileData.getKeys(false)) {
            ItemStack itemStack = FileUtil.getItemFromFile(key + ".Item", fileData, file, plugin);
            ConfigurationSection config = fileData.getConfigurationSection(key);
            if (config == null) {
                continue;
            }
            Upgrades upgrades = new Upgrades(itemStack);
            for (String level : config.getKeys(false)) {
                if (level.equalsIgnoreCase("Item")) continue;
                float speed = (float) config.getDouble(level + ".speed");
                int itemLimit = config.getInt(level + ".itemLimit");
                int itemPrice = config.getInt(level + ".itemPrice");
                int moneyPrice = config.getInt(level + ".moneyPrice");
                if (!NumberUtils.isCreatable(level)) {
                    continue;
                }
                Upgrades.Upgrade upgrade = upgrades.new Upgrade(moneyPrice, itemPrice, speed, itemLimit);
                upgrades.putUpgrade(Integer.parseInt(level), upgrade);
            }
            upgradesList.add(upgrades);
        }
        this.inventoryGeneratorUpgrades = upgradesList;
    }

    public void loadUpgrades() {
        loadInventoryGeneratorUpgrades();
        loadPlaceableGeneratorUpgrades("BlockGenerators");
        loadPlaceableGeneratorUpgrades("MobGenerators");
    }

    private void loadPlaceableGeneratorUpgrades(String type) {
        LinkedList<Upgrades> upgradesList = new LinkedList<>();
        File file = FileUtil.setupFile(plugin, "Generators", type);
        FileConfiguration fileData = FileUtil.getFileData(file);
        for (String key : fileData.getKeys(false)) {
            ItemStack itemStack = FileUtil.getItemFromFile(key + ".Item", fileData, file, plugin);
            ItemStack requiredItem = FileUtil.getItemFromFile(key + ".requiredItem", fileData, file, plugin);
            int requiredPlayerAmount = fileData.getInt(key + ".amountRequirement");
            Upgrades upgrades = new Upgrades(itemStack);

            ConfigurationSection config = fileData.getConfigurationSection(key);
            if (config == null) {
                continue;
            }
            for (String level : config.getKeys(false)) {
                if (level.equalsIgnoreCase("Item")) continue;
                float speed = (float) config.getDouble(level + ".speed");
                int itemLimit = config.getInt(level + ".itemLimit");
                int itemPrice = config.getInt(level + ".itemPrice");
                int moneyPrice = config.getInt(level + ".moneyPrice");
                int maxInStack = config.getInt(level + ".maxInStack");
                if (!NumberUtils.isCreatable(level)) {
                    continue;
                }
                Upgrades.PlaceableGeneratorUpgrade upgrade = upgrades.new PlaceableGeneratorUpgrade(moneyPrice, itemPrice, speed, itemLimit, maxInStack);
                upgrades.putUpgrade(Integer.parseInt(level), upgrade);
            }
            upgradesList.add(upgrades);
        }
        if (type.toLowerCase().contains("mob")) {
            this.mobGeneratorUpgrades = upgradesList;
        } else if (type.toLowerCase().contains("block")) {
            this.blockGeneratorUpgrades = upgradesList;
        }
    }

    public List<Upgrades> getBlockGeneratorUpgrades() {
        return blockGeneratorUpgrades;
    }

    public List<Upgrades> getMobGeneratorUpgrades() {
        return mobGeneratorUpgrades;
    }

    public List<Upgrades> getInventoryGeneratorUpgrades() {
        return inventoryGeneratorUpgrades;
    }

    public Upgrades getUpgrades(ItemStack itemStack, Type upgradesType) {
        List<Upgrades> upgradesList;
        if (upgradesType == Type.BLOCK) {
            upgradesList = this.blockGeneratorUpgrades;
        } else if (upgradesType == Type.MOB) {
            upgradesList = this.mobGeneratorUpgrades;
        } else if (upgradesType == Type.INVENTORY) {
            upgradesList = this.inventoryGeneratorUpgrades;
        } else {
            return null;
        }
        for (Upgrades upgrades : upgradesList) {
            if (upgrades.getItem().equals(itemStack)) {
                return upgrades;
            }
        }
        return null;
    }
}
