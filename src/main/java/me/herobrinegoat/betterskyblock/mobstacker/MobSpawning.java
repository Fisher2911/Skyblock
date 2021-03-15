package me.herobrinegoat.betterskyblock.mobstacker;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.generators.PlaceableGenerator;
import me.herobrinegoat.betterskyblock.managers.IslandManager;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class MobSpawning implements Listener {

    BetterSkyblock plugin;
    SpawnHandler spawnHandler;

    public MobSpawning(BetterSkyblock plugin) {
        this.plugin = plugin;
        spawnHandler = new SpawnHandler(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void creatureSpawnEvent(CreatureSpawnEvent event) {
        Entity entity = event.getEntity();
        SpawnReason spawnReason = event.getSpawnReason();
        if (spawnReason == SpawnReason.SPAWNER || event.isCancelled()) {
            return;
        }
        NamespacedKey deadKey = new NamespacedKey(plugin, "dead");
        BukkitRunnable delay = new BukkitRunnable() {
            @Override
            public void run() {
                if (entity.getPersistentDataContainer().has(deadKey, PersistentDataType.STRING)) {
                    String ignore = entity.getPersistentDataContainer().get(deadKey, PersistentDataType.STRING);
                    if (ignore != null && ignore.equalsIgnoreCase("true")) {
                        entity.getPersistentDataContainer().remove(deadKey);
                        return;
                    }
                }
                IslandManager islandManager = plugin.getIslandManager();
                Island island = islandManager.getIslandFromLocation(entity.getLocation());
                spawnHandler.addOneWithIsland(entity.getLocation(), island, entity, 16, 256, 16, 50, null);
            }
        };
        delay.runTaskLater(plugin, 1);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void spawnerSpawnEvent(SpawnerSpawnEvent event) {
        CreatureSpawner spawner = event.getSpawner();
        Location location = spawner.getLocation();
        Entity entity = event.getEntity();
        Island island = plugin.getIslandManager().getIslandFromLocation(location);

        if (island == null) {
            return;
        }
        if (event.isCancelled()) {
            return;
        }

        NamespacedKey dropKey = new NamespacedKey(plugin, "drop");
        NamespacedKey generatorIdKey = new NamespacedKey(plugin, "generatorId");
        PlaceableGenerator generator = island.getGeneratorFromLocation(location);
        Location entityLocation = event.getLocation();
        entity.remove();
        if (entityLocation.getWorld() == null) {
            return;
        }
        if (generator instanceof MobGenerator) {
            Entity e = entityLocation.getWorld().spawnEntity(entityLocation, ((MobGenerator) generator).getEntityType());
            NamespacedKey deadKey = new NamespacedKey(plugin, "dead");
            e.getPersistentDataContainer().set(deadKey, PersistentDataType.STRING, "true");
            if (entityLocation.getWorld() != null) {
                if (e instanceof Slime) {
                    ((Slime) e).setSize(1);
                    String name = "";
                    if (generator.getGeneratedItemStack().getItemMeta() != null) {
                        name = generator.getGeneratedItemStack().getItemMeta().getDisplayName();
                    }
                    MobGenerator mobGenerator = (MobGenerator) generator;
                    mobGenerator.updateSpawner();
                    mobGenerator.generate();
                    if (mobGenerator.getTotalResources() > 0) {
                        mobGenerator.setTotalResources(0, false);
                    }
                    e.getPersistentDataContainer().set(dropKey, PersistentDataType.STRING, ItemStackSerializer.itemStackToBase64(generator.getGeneratedItemStack()));
                    e.getPersistentDataContainer().set(generatorIdKey, PersistentDataType.INTEGER, generator.getId());
                    e.setCustomName(name);
                    Entity spawnedEntity = spawnHandler.spawnIfInIsland(e.getLocation(), island, e, generator.getStackAmount(), 16, 256, 16,
                            generator.getMaxResources() * generator.getStackAmount(), null);
                    spawnedEntity.setPersistent(true);
                    return;
                }
            }
            String name = "";
            if (generator.getGeneratedItemStack().getItemMeta() != null) {
                name = generator.getGeneratedItemStack().getItemMeta().getDisplayName();
            }
            MobGenerator mobGenerator = (MobGenerator) generator;
            mobGenerator.updateSpawner();
            e.getPersistentDataContainer().set(dropKey, PersistentDataType.STRING, ItemStackSerializer.itemStackToBase64(generator.getGeneratedItemStack()));
            e.getPersistentDataContainer().set(generatorIdKey, PersistentDataType.INTEGER, generator.getId());
            e.setCustomName(name);
            Entity spawnedEntity = spawnHandler.spawnIfInIsland(e.getLocation(), island, e, generator.getStackAmount(), 16, 256, 16,
                    generator.getMaxResources() * generator.getStackAmount(), null);
            if (spawnedEntity instanceof Ageable) {
                ((Ageable) spawnedEntity).setAdult();
            }
            spawnedEntity.setPersistent(true);

        }
    }
}
