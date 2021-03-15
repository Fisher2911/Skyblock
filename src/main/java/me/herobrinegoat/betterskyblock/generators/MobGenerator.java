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
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Animals;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

public class MobGenerator extends PlaceableGenerator {

    private EntityType entityType;

    public MobGenerator(int id, ItemStack generatedItemStack, int totalResources, LocalDateTime lastGeneratedDate, Upgrades upgrades, int rank,
                        int level, EntityType entityType, Location location, int stackAmount) {
        super(id, generatedItemStack, totalResources, lastGeneratedDate, upgrades, rank, level, location, stackAmount);
        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
    }

    @Override
    public void generate() {
        super.generate();
    }

    @Override
    public boolean place() {
        if (getLocation() != null) {
            getLocation().getBlock().setType(Material.SPAWNER);
            updateSpawner();
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getItemForInventory(BetterSkyblock plugin) {
        ItemBuilder itemBuilder = new ItemBuilder(super.getItemForInventory(plugin));
        itemBuilder.setMaterial(Material.SPAWNER);
        NamespacedKey blockKey = new NamespacedKey(plugin, "mob");
        return itemBuilder.addPersistentData(PersistentDataType.STRING, blockKey, this.entityType.toString()).get();
    }

    public static MobGenerator getGeneratorFromItem(ItemStack item, BetterSkyblock plugin) {
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
                rank = plugin.getRankManager().getItemRank(spawnedItem, Type.MOB);
            }

            NamespacedKey mobKey = new NamespacedKey(plugin, "mob");
            if (data.has(mobKey, PersistentDataType.STRING)) {
                String block = data.get(mobKey, PersistentDataType.STRING);
                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                Upgrades upgrades = upgradesConfig.getUpgrades(spawnedItem, Type.MOB);
                return new MobGenerator(id, spawnedItem, 0, null, upgrades, rank, level, EntityType.valueOf(block), null, 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateSpawner() {
        Block placed = getLocation().getBlock();
        if (placed.getState() instanceof CreatureSpawner) {
            CreatureSpawner spawner = (CreatureSpawner) placed.getState();

            int generationSpeed = (int) (getSpeed() * 20);
            if (getEntityType().getEntityClass() == null) {
                spawner.setSpawnedType(EntityType.ARROW);
            } else if (Monster.class.isAssignableFrom(getEntityType().getEntityClass())) {
                spawner.setSpawnedType(EntityType.ZOMBIE);
            } else if (Animals.class.isAssignableFrom(getEntityType().getEntityClass())) {
                spawner.setSpawnedType(EntityType.PIG);
            } else {
                spawner.setSpawnedType(EntityType.ARROW);
            }
            if (spawner.getMaxSpawnDelay() > generationSpeed - 1) {
                spawner.setMinSpawnDelay(generationSpeed - 1);
                spawner.setMaxSpawnDelay(generationSpeed + 1);
            } else {
                spawner.setMaxSpawnDelay(generationSpeed + 1);
                spawner.setMinSpawnDelay(generationSpeed - 1);
            }
            spawner.setSpawnRange(3);
            spawner.setSpawnCount(1);
            spawner.setRequiredPlayerRange(16);
            spawner.update(true, true);
        }
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
        if (!(o instanceof MobGenerator)) return false;
        if (!super.equals(o)) return false;
        MobGenerator that = (MobGenerator) o;
        return entityType == that.entityType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), entityType);
    }
}
