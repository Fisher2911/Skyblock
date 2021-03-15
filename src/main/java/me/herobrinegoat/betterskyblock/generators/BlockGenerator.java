package me.herobrinegoat.betterskyblock.generators;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.configs.UpgradesConfig;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.ItemBuilder;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public class BlockGenerator extends PlaceableGenerator {

    private Material block;

    public BlockGenerator(int id, ItemStack generatedItemStack, int totalResources, LocalDateTime lastGeneratedDate, Upgrades upgrades,
                          int rank, int level, Material block, Location location, int stackAmount) {
        super(id, generatedItemStack, totalResources, lastGeneratedDate, upgrades, rank, level, location, stackAmount);
        this.block = block;
    }

    public Material getBlock() {
        return block;
    }

    public void setBlock(Material block) {
        this.block = block;
    }

    @Override
    public void generate() {
        super.generate();
    }

    @Override
    public ItemStack getItemForInventory(BetterSkyblock plugin) {
        ItemBuilder itemBuilder = new ItemBuilder(super.getItemForInventory(plugin));
        itemBuilder.setMaterial(block);
        NamespacedKey blockKey = new NamespacedKey(plugin, "block");
        return itemBuilder.addPersistentData(PersistentDataType.STRING, blockKey, this.block.toString()).get();
    }

    public static BlockGenerator getGeneratorFromItem(ItemStack item, BetterSkyblock plugin) {
        if (item == null) {
            return null;
        }
        if (item.getItemMeta() == null) {
            return null;
        }

        if (!isGeneratorItem(plugin, item)) {
            return null;
        }

        ItemMeta im = item.getItemMeta();
        PersistentDataContainer data = im.getPersistentDataContainer();
        NamespacedKey levelKey = new NamespacedKey(plugin, "level");
        NamespacedKey spawnedItemKey = new NamespacedKey(plugin, "spawnedItem");
        NamespacedKey rankKey = new NamespacedKey(plugin, "rank");

        int id = -1;
        Integer level = data.get(levelKey, PersistentDataType.INTEGER);
        Integer rank = data.get(rankKey, PersistentDataType.INTEGER);
        String spawnedItemStr = data.get(spawnedItemKey, PersistentDataType.STRING);

        try {
            ItemStack spawnedItem = ItemStackSerializer.itemStackFromBase64(spawnedItemStr);

            if (level == null) {
                level = 0;
            }
            if (rank == null) {
                rank = plugin.getRankManager().getItemRank(spawnedItem, Type.BLOCK);
            }

            NamespacedKey blockKey = new NamespacedKey(plugin, "block");
            if (data.has(blockKey, PersistentDataType.STRING)) {
                String block = data.get(blockKey, PersistentDataType.STRING);
                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                Upgrades upgrades = upgradesConfig.getUpgrades(spawnedItem, Type.BLOCK);
                return new BlockGenerator(id, spawnedItem, 0, null, upgrades, rank, level, Material.valueOf(block), null, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @return true if block place, false if block unable to be placed
     */

    @Override
    public boolean place() {
        if (block != null && getLocation() != null) {
            getLocation().getBlock().setType(block);
            return true;
        }
        return false;
    }

    /**
     * @return true if block place, false if block unable to be placed
     */
    @Override
    public boolean remove() {
        if (getLocation() != null) {
            getLocation().getBlock().setType(Material.AIR);
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockGenerator)) return false;
        if (!super.equals(o)) return false;
        BlockGenerator that = (BlockGenerator) o;
        return block == that.block;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), block);
    }
}
