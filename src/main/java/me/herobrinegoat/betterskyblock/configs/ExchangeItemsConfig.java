package me.herobrinegoat.betterskyblock.configs;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.inventories.ExchangeItem;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.FileUtil;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExchangeItemsConfig {

    BetterSkyblock plugin;

    public ExchangeItemsConfig(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    private List<ExchangeItem> exchangeItems = new ArrayList<>();

    private Map<ItemStack, EntityType> mobExchangeItems = new HashMap<>();

    private Map<ItemStack, Material> blockExchangeItems = new HashMap<>();

    public void loadExchangeItems() {
        loadExchangeItems("BlockGeneratorExchangeItems");
        loadExchangeItems("MobGeneratorExchangeItems");
    }

    private void loadExchangeItems(String fileName) {
        List<ExchangeItem> exchangeItems = new ArrayList<>();

        File file = FileUtil.setupFile(plugin, "Costs", fileName);
        FileConfiguration fileData = FileUtil.getFileData(file);
        for (String key : fileData.getKeys(false)) {
            ItemStack item = FileUtil.getItemFromFile(key + ".Item", fileData, file, false, plugin);
            ItemStack exchangedItem = FileUtil.getItemFromFile(key + ".ExchangedItem", fileData, file, false, plugin);
            ItemStack gainedItem = FileUtil.getItemFromFile(key + ".GainedItem", fileData, file, false, plugin);
            int reqMoney = fileData.getInt(key + ".RequiredMoney");
            int reqItemAmount = fileData.getInt(key + ".RequiredItemAmount");
            String typeString = fileData.getString(key + ".Type");
            Type type = Type.valueOf(typeString);
            RankManager rankManager = plugin.getRankManager();
            UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
            Upgrades upgrades;
            int rank;
            if (type == Type.BLOCK) {
                String blockType = fileData.getString(key + ".BlockType");
                upgrades = upgradesConfig.getUpgrades(exchangedItem, Type.BLOCK);
                rank = rankManager.getItemRank(exchangedItem, Type.BLOCK);
                BlockGenerator blockGenerator = new BlockGenerator(-1, exchangedItem, 0, null, upgrades, rank, 1, Material.valueOf(blockType), null, 1);
                item = blockGenerator.getItemForInventory(plugin);
                blockExchangeItems.put(exchangedItem, Material.valueOf(blockType));
                gainedItem = item;
            } else if (type == Type.MOB) {
                String entityType = fileData.getString(key + ".EntityType");
                upgrades = upgradesConfig.getUpgrades(exchangedItem, Type.MOB);
                rank = rankManager.getItemRank(exchangedItem, Type.MOB);
                MobGenerator mobGenerator = new MobGenerator(-1, exchangedItem, 0, null, upgrades, rank, 1, EntityType.valueOf(entityType), null, 1);
                item = mobGenerator.getItemForInventory(plugin);
                mobExchangeItems.put(exchangedItem, EntityType.valueOf(entityType));
                gainedItem = item;
            }
            ExchangeItem exchangeItem;
            if (type == Type.MOB) {
                exchangeItem = new ExchangeItem(item, exchangedItem, gainedItem, reqMoney, reqItemAmount, ExchangeItem.Type.MOB_GENERATOR);
            } else if (type == Type.BLOCK) {
                exchangeItem = new ExchangeItem(item, exchangedItem, gainedItem, reqMoney, reqItemAmount, ExchangeItem.Type.BLOCK_GENERATOR);
            } else {
                exchangeItem = null;
            }
            if (exchangeItem == null) {
                continue;
            }
            exchangeItems.add(exchangeItem);
        }
        for (ExchangeItem exchangeItem : exchangeItems) {
            if (!this.exchangeItems.contains(exchangeItem)) {
                this.exchangeItems.add(exchangeItem);
            }
        }
    }

    public List<ExchangeItem> getExchangeItems() {
        return exchangeItems;
    }

    public EntityType getEntityTypeFromItem(ItemStack itemStack) {
        return mobExchangeItems.get(itemStack);
    }

    public Material getMaterialFromItem(ItemStack itemStack) {
        return blockExchangeItems.get(itemStack);
    }
}
