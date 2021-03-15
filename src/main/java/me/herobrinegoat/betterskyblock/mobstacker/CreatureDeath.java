package me.herobrinegoat.betterskyblock.mobstacker;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.StoredPersistentData;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.generators.Generator;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class CreatureDeath implements Listener {

    BetterSkyblock plugin;

    public CreatureDeath(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void creatureDeathEvent(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getLastDamageCause() == null || entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.VOID ||
                entity.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.CUSTOM) {
            return;
        }
        SpawnHandler spawnHandler = new SpawnHandler(plugin);
        NamespacedKey dropKey = new NamespacedKey(plugin, "drop");
        NamespacedKey amountKey = new NamespacedKey(plugin, "amount");
        String itemString = entity.getPersistentDataContainer().get(dropKey, PersistentDataType.STRING);
        Integer amount = entity.getPersistentDataContainer().get(amountKey, PersistentDataType.INTEGER);
        if (amount == null) {
            amount = 1;
        }
        Location loc = entity.getLocation();
        World world = loc.getWorld();
        if (world == null) {
            return;
        }
        if (amount - 1 > 0) {
            NamespacedKey deadKey = new NamespacedKey(plugin, "dead");
            Entity spawnEntity = world.spawnEntity(loc, entity.getType());
            if (spawnEntity instanceof Slime) {
                ((Slime) spawnEntity).setSize(1);
            }
            spawnEntity.getPersistentDataContainer().set(deadKey, PersistentDataType.STRING, "true");
            Map<String, StoredPersistentData> persistentDataSet = spawnHandler.getEntityPersistentData(entity);
            for (StoredPersistentData persistentData : persistentDataSet.values()) {
                spawnEntity.getPersistentDataContainer().set(persistentData.getKey(), persistentData.getPersistentDataType(), persistentData.getValue());
            }
            spawnHandler.addEntities(spawnEntity, loc, 0, 0, 0, (amount - 1), amount, true, null);
        }
        if (itemString == null) {
            return;
        }

        List<ItemStack> drops = event.getDrops();

        try {
            ItemStack drop = ItemStackSerializer.itemStackFromBase64(itemString);

            Player killer = event.getEntity().getKiller();

            User user;
            boolean dropSet = false;
            if (killer != null) {
                user = plugin.getUserManager().getUser(killer);
            } else {
                for (int i = 0; i < drops.size(); i++) {
                    ItemStack currentDrop = drops.get(i);
                    if (drop.getType().toString().contains(currentDrop.getType().toString())) {
                        dropSet = true;
                        drops.set(i, drop);
                    }
                }
                if (!dropSet) {
                    drops.add(drop);
                }
                return;
            }

            NamespacedKey generatorIdKey = new NamespacedKey(plugin, "generatorId");
            int generatorId = -1;
            Integer generatorIdCheck = entity.getPersistentDataContainer().get(generatorIdKey, PersistentDataType.INTEGER);
            if (generatorIdCheck != null) {
                generatorId = generatorIdCheck;
            }
            if (user == null || generatorId == -1) {
                return;
            }
            Island island = plugin.getIslandManager().getIslandFromLocation(entity.getLocation());
            if (island == null) {
                return;
            }
            Generator generator = island.getGenerator(generatorId);
            if (generator == null) {
                return;
            }
            if (generator instanceof MobGenerator) {

                if (!plugin.getRankManager().hasMobRequirements(generator.getRank(), user, true)) {
                    killer.sendMessage(Messages.NOT_UNLOCKED_YET + " The mob drops will be normal until you have unlocked it!");
                    return;
                }
            }
            user.addMobItemToRequirements(drop);
            for (int i = 0; i < drops.size(); i++) {
                ItemStack currentDrop = drops.get(i);
                if (drop.getType().toString().contains(currentDrop.getType().toString())) {
                    drops.set(i, drop);
                    dropSet = true;
                    drops.set(i, drop);
                }
            }
            if (!dropSet) {
                drops.add(drop);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
