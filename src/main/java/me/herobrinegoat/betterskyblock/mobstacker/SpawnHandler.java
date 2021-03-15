package me.herobrinegoat.betterskyblock.mobstacker;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.StoredPersistentData;
import me.herobrinegoat.betterskyblock.inworld.Region;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.NumberUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.math.NumberUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

import static org.bukkit.ChatColor.AQUA;
import static org.bukkit.ChatColor.RESET;

public class SpawnHandler {

    BetterSkyblock plugin;

    public SpawnHandler(BetterSkyblock plugin) {
        this.plugin = plugin;
        amountKey = new NamespacedKey(plugin, "amount");
        ignoreKey = new NamespacedKey(plugin, "ignore");
    }

    private NamespacedKey amountKey;
    private NamespacedKey ignoreKey;

    public Entity addEntities(Entity entity, Location location, float xRange, float yRange, float zRange, int spawnAmount, int maxAmount, boolean dead, List<String> ignoreKeyValues) {

        if (spawnAmount == 0) {
            return null;
        }

        World world = location.getWorld();
        if (world == null) {
            return entity;
        }

        if (ignoreKeyValues == null) {
            ignoreKeyValues = new ArrayList<>();
            ignoreKeyValues.add(amountKey.getKey());
        }

        EntityType entityType = entity.getType();

        Map<String, StoredPersistentData> entityPersistentData = getEntityPersistentData(entity);

        int locY = location.getBlockY();
        int distanceFromBuildLimit = 256 - locY;

        String name;

        if (!dead) {
            Collection<Entity> nearbyEntities;

            if (yRange > distanceFromBuildLimit) {
                nearbyEntities = world.getNearbyEntities(location, xRange, distanceFromBuildLimit, zRange, (e) -> e.getType() == entityType);
            } else {
                nearbyEntities = world.getNearbyEntities(location, xRange, yRange, zRange, (e) -> e.getType() == entityType);
            }
            outerLoop:
            for (Entity nearbyEntity : nearbyEntities) {
                if (nearbyEntity.equals(entity)) {
                    continue;
                }
                Map<String, StoredPersistentData> nearbyEntityPersistentData = getEntityPersistentData(nearbyEntity);
                for (Map.Entry<String, StoredPersistentData> entry : nearbyEntityPersistentData.entrySet()) {
                    String keyName = entry.getKey();
                    StoredPersistentData nearbyStoredPersistentData = entry.getValue();
                    StoredPersistentData persistentData = entityPersistentData.get(keyName);
                    if (persistentData == null && !ignoreKeyValues.contains(keyName)) {
                        continue outerLoop;
                    } else if (persistentData != null) {
                        if (!persistentData.equals(nearbyStoredPersistentData) && !ignoreKeyValues.contains(keyName)) {
                            continue outerLoop;
                        } else if (persistentData.getPersistentDataType() != nearbyStoredPersistentData.getPersistentDataType()) {
                            continue outerLoop;
                        }
                    }

                }
                Integer amount = nearbyEntity.getPersistentDataContainer().get(amountKey, PersistentDataType.INTEGER);
                if (amount == null) {
                    amount = 1;
                }
                for (StoredPersistentData storedPersistentData : entityPersistentData.values()) {
                    nearbyEntity.getPersistentDataContainer().set(storedPersistentData.getKey(), storedPersistentData.getPersistentDataType(), storedPersistentData.getValue());
                }
                amount = amount + spawnAmount;
                if (amount > maxAmount) {
                    amount = maxAmount;
                }
                name = nearbyEntity.getCustomName();
                if (name == null || !name.contains("x")) {
                    name = getEntityAmountString(amount, entityType, true);
                } else {
                    if (NumberUtils.isCreatable(ChatColor.stripColor(name).split("x")[0])) {
                        name = getEntityAmountString(amount, entityType, false) + name.split("x")[1];
                    } else {
                        name = getEntityAmountString(amount, entityType, true);
                    }
                }
                nearbyEntity.setCustomName(name);
                nearbyEntity.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, (amount));
                entity.remove();
                return nearbyEntity;
            }
        }
        name = entity.getCustomName();
        if (name == null || name.trim().equals("")) {
            name = getEntityAmountString(spawnAmount, entityType, true);
        } else {
            name = getEntityAmountString(spawnAmount, entityType, false) + " " + name;
        }
        entity.getPersistentDataContainer().set(amountKey, PersistentDataType.INTEGER, spawnAmount);
        entity.setCustomName(name);
        return entity;
    }

    public Entity spawnIfInIsland(Location location, Island island, Entity entity, int spawnAmount, int xRange, int yRange, int zRange, int maxMobs, List<String> ignoreKeyValues) {
        if (island == null) {
            return null;
        }
        Region region = island.getRegion();
        int maxX = region.getUpperX();
        int maxZ = region.getUpperZ();
        int minX = region.getLowerX();
        int minZ = region.getLowerZ();

        int entityX = location.getBlockX();
        int entityZ = location.getBlockZ();

        if (entityX > maxX && entityX < minX) {
            xRange = maxX - entityX;
        } else if (entityX > maxX || entityX < minX) {
            xRange = NumberUtil.getLowerNumber(maxX - entityX, entityX - minX);
        }
        if (entityZ > maxZ && entityZ < minZ) {
            zRange = maxZ - entityZ;
        } else if (entityZ > maxZ || entityZ < minZ) {
            zRange = NumberUtil.getLowerNumber(maxZ - entityZ, entityZ - minZ);
        }
        return addEntities(entity, location, xRange, yRange, zRange, spawnAmount, maxMobs, false, ignoreKeyValues);
    }

    public Entity addOneWithIsland(Location location, Island island, Entity entity, int xRange, int yRange, int zRange, int maxMobs, List<String> ignoreKeyValues) {
        return spawnIfInIsland(location, island, entity, 1, xRange, yRange, zRange, maxMobs, ignoreKeyValues);
    }

    private String getEntityAmountString(int amount, EntityType entityType,  boolean includeEntityType) {
        String name = AQUA.toString() + amount + "x" + RESET;
        if (includeEntityType) {
            name = name + ChatUtil.capitalize(entityType.toString());
        }
        return name;
    }

    public Map<String, StoredPersistentData> getEntityPersistentData(Entity entity) {
        Map<String, StoredPersistentData> persistentData = new HashMap<>();
        PersistentDataContainer data = entity.getPersistentDataContainer();
        for (NamespacedKey key : data.getKeys()) {
            StoredPersistentData storedPersistentData = null;
            if (data.has(key, PersistentDataType.STRING)) {
                storedPersistentData = new StoredPersistentData(key, PersistentDataType.STRING, data.get(key, PersistentDataType.STRING));
            } else if (data.has(key, PersistentDataType.INTEGER)) {
                storedPersistentData = new StoredPersistentData(key, PersistentDataType.INTEGER, data.get(key, PersistentDataType.INTEGER));
            }
            if (storedPersistentData != null) {
                persistentData.put(key.getKey(), storedPersistentData);
            }
        }
        return persistentData;
    }
}
