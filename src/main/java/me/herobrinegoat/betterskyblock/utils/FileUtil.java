package me.herobrinegoat.betterskyblock.utils;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.io.FileUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class FileUtil {

    public static File setupFile(Plugin plugin, String folderName, String fileName) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        File folder = new File(plugin.getDataFolder() + File.separator + folderName);

        if (!folder.exists()) {
            folder.mkdir();
        }

        File file = new File(plugin.getDataFolder() + File.separator + folderName, fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static  File setupFile(Plugin plugin, String fileName) {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }


        File file = new File(plugin.getDataFolder(), fileName);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static  FileConfiguration getFileData(File file) {
        return YamlConfiguration.loadConfiguration(file);
    }

    public static void saveFileData(FileConfiguration fileConfiguration, File file) {
        try {
            fileConfiguration.save(file);
        }
        catch (IOException e) {
            Bukkit.getServer().getLogger().severe(ChatColor.RED + "Could not save file!");
        }
    }

    public static  void saveLocation(Location loc, String key, FileConfiguration fileData, File file) {
        if (loc == null || loc.getWorld() == null) {
            return;
        }
        fileData.set(key + ".World", loc.getWorld().getName());
        fileData.set(key + ".x", loc.getBlockX());
        fileData.set(key + ".y", loc.getBlockY());
        fileData.set(key + ".z", loc.getBlockZ());
        saveFileData(fileData, file);
    }

    public static  Location getLocation(String key, FileConfiguration fileData, BetterSkyblock plugin) {
        String worldName = fileData.getString(key + ".world");
        int x = fileData.getInt(key + ".x");
        int y = fileData.getInt(key + ".y");
        int z = fileData.getInt(key + ".z");
        World world;
        if (worldName == null) {
            world = plugin.getIslandWorld();
        } else {
            world = Bukkit.getWorld(worldName);
        }
        return new Location(world, x ,y , z);
    }

    public static  void saveItemToFile(ItemStack item, String key, FileConfiguration fileData, File file) {
        if (item == null) {
            return;
        }
        String itemType = item.getType().toString();
        int itemAmount = item.getAmount();
        String itemName = "";
        List<String> lore = new ArrayList<>();
        List<String> itemFlagStrings = new ArrayList<>();
        Map<Enchantment, Integer> enchants = new HashMap<>();
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta != null) {
            String displayName = itemMeta.getDisplayName();
            if (!displayName.equalsIgnoreCase("")) {
                itemName = displayName;
            }
            List<String> newLore = itemMeta.getLore();
            if (newLore != null) {
                lore = newLore;
            }
            Set<ItemFlag> itemFlags = itemMeta.getItemFlags();
            if (!itemFlags.isEmpty()) {
                for (ItemFlag itemFlag : itemFlags) {
                    itemFlagStrings.add(itemFlag.toString());
                }
            }
            Map<Enchantment, Integer> enchanments = itemMeta.getEnchants();
            for (Map.Entry<Enchantment, Integer> entry : enchanments.entrySet()) {
                enchants.put(entry.getKey(), entry.getValue());
            }
        }
        fileData.set(key + ".ItemType", itemType);
        fileData.set(key + ".ItemAmount", itemAmount);
        fileData.set(key + ".ItemName", itemName);
        fileData.set(key + ".Lore", lore);
        fileData.set(key + ".ItemFlags", itemFlagStrings);
        int i = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            fileData.set(key + ".Enchants." + i + ".Enchantment", entry.getKey().getKey());
            fileData.set(key + ".Enchants." + i + ".Level", entry.getValue());
            i++;
        }
        saveFileData(fileData, file);
    }

    public static ItemStack getItemFromFile(String key, FileConfiguration fileData, File file, BetterSkyblock plugin) {
        return getItemFromFile(key, fileData, file, true, plugin);
    }

    public static ItemStack getItemFromFile(String key, FileConfiguration fileData, File file, boolean sendWarning, BetterSkyblock plugin) {
        String itemType = fileData.getString(key + ".ItemType");
        int itemAmount = fileData.getInt(key + ".ItemAmount");
        String itemName = fileData.getString(key + ".ItemName");
        List<String> lore = fileData.getStringList(key + ".Lore");
        List<String> itemFlagStrings = fileData.getStringList(key + ".ItemFlags");

        if (itemType == null || Material.matchMaterial(itemType) == null) {
            if (sendWarning) Bukkit.getConsoleSender().sendMessage("[WARNING] " + itemType + " is not a valid item in file "
                    + file.getName() + " at key + " + key + "!");
            return null;
        }

        List<String> tempLore = new ArrayList<>();
        for (String s : lore) {
            tempLore.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        lore = tempLore;

        if (itemName != null) {
            itemName = ChatColor.translateAlternateColorCodes('&', itemName);
        }

        ItemBuilder itemBuilder = new ItemBuilder(Material.matchMaterial(itemType)).setAmount(itemAmount).setName(itemName).addLore(lore);

        ConfigurationSection section = fileData.getConfigurationSection("NBT");
        if (section != null) {
            for (String nbtKey : section.getKeys(false)) {
                String type = section.getString(nbtKey + ".type");
                String storedKey = section.getString(nbtKey + ".storedKey");

                if (type == null || storedKey == null) continue;
                NamespacedKey namespacedKey = new NamespacedKey(plugin, storedKey);
                if (type.equals("string")) {
                    String storedData = section.getString(nbtKey + ".storedData");
                    PersistentDataType<String, String> persistentDataType = PersistentDataType.STRING;
                    itemBuilder.addPersistentData(persistentDataType, namespacedKey, storedData);
                } else if (type.equals("int")) {
                    int storedData = section.getInt(nbtKey + ".storedData");
                    PersistentDataType<Integer, Integer> persistentDataType = PersistentDataType.INTEGER;
                    itemBuilder.addPersistentData(persistentDataType, namespacedKey, storedData);
                }
            }
        }

        ItemStack item = itemBuilder.get();

        if (item.hasItemMeta()) {
            ItemMeta itemMeta = item.getItemMeta();
            assert itemMeta != null;
            List<ItemFlag> itemFlags = new ArrayList<>();
            for (String itemFlag : itemFlagStrings) {
                itemFlags.add(ItemFlag.valueOf(itemFlag));
            }
            ConfigurationSection config = fileData.getConfigurationSection(key + ".Enchants");
            if (config != null) {
                for (String configKey : config.getKeys(false)) {
                    String enchantString = config.getString(configKey + ".Enchantment");
                    int enchantLevel = config.getInt(configKey + ".Level");
                    if (enchantString != null) {
                        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(enchantString));
                        if (enchant != null) itemMeta.addEnchant(enchant, enchantLevel, true);
                    }
                }
            }
            for (ItemFlag itemFlag : itemFlags) {
                itemMeta.addItemFlags(itemFlag);
            }
            item.setItemMeta(itemMeta);
        }
        return item;
    }

    public static  void saveBackupFile(File file, FileConfiguration fileData, BetterSkyblock plugin) {
        File backupFile = setupFile(plugin, "Backups", file.getName());
        FileConfiguration backupData = getFileData(backupFile);
        for (String s : backupData.getKeys(false)) {
            backupData.set(s, null);
        }
        saveFileData(backupData, backupFile);
        try {
            FileUtils.copyFile(file, backupFile);
            for (String key : fileData.getKeys(false)) {
                fileData.set(key, null);
            }
            saveFileData(fileData, file);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }
}
