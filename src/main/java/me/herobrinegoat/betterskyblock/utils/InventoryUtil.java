package me.herobrinegoat.betterskyblock.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class InventoryUtil {


     public static Inventory createInventory(Player p, int invSize, String title) {
         if (p != null) {
             return createInventory((InventoryHolder) p, invSize, title);
         }
         return null;
    }

    public static Inventory createInventory(InventoryHolder holder, int invSize, String title) {
        return Bukkit.createInventory(holder, invSize, title);
    }

     public static Inventory createInventory(int invSize, String title) {
        return Bukkit.createInventory(null, invSize, title);
    }

    public static ItemStack makeItem(Material m, int amount, String name, Enchantment enchant, int enchantLevel, List<String> lore, ItemFlag... itemFlags) {
        ItemStack item = makeItem(m, amount, name);
        if (metaNull(item)) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setLore(lore);
        meta.addEnchant(enchant, enchantLevel, true);
        item.setItemMeta(meta);
        for (ItemFlag flag : itemFlags) {
            if (!meta.getItemFlags().contains(flag)) {
                meta.addItemFlags(itemFlags);
            }
        }
        return item;
    }

    public static ItemStack makeItem(Material m, String name, int amount, Enchantment enchant, int enchantLevel, String... desc) {
        ItemStack item = makeItem(m, amount, name, desc);
        if (metaNull(item)) {
            return item;
        }
        if (item.getItemMeta() == null) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        meta.addEnchant(enchant, enchantLevel, true);
        item.setItemMeta(meta);
        return item;
    }

     public static ItemStack makeItem(Material m, int amount, String name, String... desc) {
        ItemStack item = makeItem(m, amount, name);
         if (metaNull(item)) {
             return item;
         }
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        for (String s : desc) {
            if (s == null) {
                continue;
            }
            lore.add(s);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material m, int amount, String name, List<String> desc) {
        ItemStack item = makeItem(m, amount, name);
        if (metaNull(item)) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        for (String s : desc) {
            if (s == null) {
                continue;
            }
            lore.add(s);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack makeItem(Material m, int amount, List<String> desc) {
        ItemStack item = makeItem(m, amount);
        if (metaNull(item)) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        ArrayList<String> lore = new ArrayList<String>();
        for (String s : desc) {
            if (s == null) {
                continue;
            }
            lore.add(s);
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

     public static ItemStack makeItem(Material m, int amount, String name) {
        ItemStack item = makeItem(m, amount);
        if (metaNull(item)) {
            return item;
        }
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

     public static ItemStack makeItem(Material m, int amount) {
        return new ItemStack(m, amount);
    }

    public static void addLoreToItem(ItemStack item, String... lore) {
         ItemMeta itemMeta = item.getItemMeta();
         if (itemMeta == null) {
             return;
         }
        List<String> newLore = new ArrayList<>(Arrays.asList(lore));
         itemMeta.setLore(newLore);
         item.setItemMeta(itemMeta);
    }

    public static void setItemInInventory(Inventory inv, ItemStack item, int slot) {
        inv.setItem(slot, item);
    }

    public static boolean metaNull(ItemStack item) {
         return item.getItemMeta() == null;
    }

    public static ItemStack getFillerItem(Material material) {
         return makeItem(material, 1, " ");
    }

    public static ItemStack getFillerItem() {
        return makeItem(Material.BLACK_STAINED_GLASS_PANE, 1, " ");
    }

    //Returns if inventory contains items
    public static boolean checkInventoryForItems(Inventory inv, ItemStack removeItem, int itemAmount, boolean removeItems) {
        int totalItemAmount = 0;

        for (ItemStack item : inv) {
            if (item != null) {
                if (item.isSimilar(removeItem)) {
                    totalItemAmount = totalItemAmount + item.getAmount();
                }
            }
        }

        int totalItemsSold = 0;
        int amountLeft = itemAmount - totalItemsSold;

        if (itemAmount <= totalItemAmount) {
            if (removeItems) {
                for (int i = 0; i < inv.getSize(); i++) {
                    ItemStack item = inv.getItem(i);
                    if (item != null) {
                        if (item.isSimilar(removeItem)) {
                            if (amountLeft > 0) {
                                if (item.getAmount() <= amountLeft) {
                                    totalItemsSold = totalItemsSold + item.getAmount();
                                    inv.setItem(i, new ItemStack(Material.AIR, 1));
                                } else {
                                    totalItemsSold = totalItemsSold + item.getAmount();
                                    item.setAmount(item.getAmount() - amountLeft);
                                }
                                amountLeft = itemAmount - totalItemsSold;
                            } else {
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public static List<ItemStack> sortItemsInList(List<ItemStack> items) {
        List<ItemStack> sortedItems = new ArrayList<>();
        TreeMap<String, ItemStack> itemMap = new TreeMap<>();
        for (ItemStack item : items) {
            if (item.getItemMeta() == null || item.getItemMeta().getDisplayName().equalsIgnoreCase("")) {
                itemMap.put(item.getType().toString(), item);
                continue;
            }
            itemMap.put(item.getItemMeta().getDisplayName(), item);
        }
        for (Map.Entry<String, ItemStack> sortedEntry : itemMap.entrySet()) {
            sortedItems.add(sortedEntry.getValue());
        }
        return sortedItems;
    }

    public static void givePlayerItems(Player player, ItemStack item, int amount) {
         ItemStack cloneItem = item.clone();
         int stackAmount = amount / 64;
         int leftOver = amount % 64;

         for (int i = 1; i <= stackAmount; i++) {
             cloneItem.setAmount(64);
            dropItem(player, cloneItem);
         }
         cloneItem.setAmount(leftOver);
        dropItem(player, cloneItem);
    }

    private static void dropItem(Player player, ItemStack item) {
         Inventory inventory = player.getInventory();
        if (inventory.firstEmpty() == -1) {
            player.getLocation().getWorld().dropItem(player.getLocation(), item);
        } else {
            player.getInventory().addItem(item);
        }
    }

    public static void setFillerItem(Inventory inv, ItemStack item) {
        int size = inv.getSize() - 1;
        for (int i = size; i >= 0; i--) {
            ItemStack itemCheck = inv.getItem(i);
            if (itemCheck == null){
                inv.setItem(i, item);
            }
        }
    }

    public static int getAmountOfItemsCanBeAdded(Player player, ItemStack checkItem) {
         int totalItems = 0;
         if (checkItem == null) {
             return totalItems;
         }

         Inventory inventory = player.getInventory();

         for (ItemStack item : inventory.getStorageContents()) {
//             ItemStack item = inventory.getItem(i);
             if (item == null) {
                 totalItems+=64;
                 continue;
             }

             if (item.isSimilar(checkItem)) {
                 int itemAmount = item.getAmount();
                 totalItems+=(64-itemAmount);
             }
         }
         return totalItems;
    }

    public static HashMap<Integer, ItemStack> getItemsWithFillers(List<ItemStack> items, Material fillerMaterial, int inventorySize) {
         HashMap<Integer, ItemStack> itemsInInventory = new HashMap<>();
         int slot = 0;
         ItemStack fillerItem = getFillerItem(fillerMaterial);
         for (ItemStack itemStack : items) {
             if (slot < 9 || slot > inventorySize - 9) {
                 itemsInInventory.put(slot, fillerItem);
                 slot++;
                 continue;
             }
             if (slot % 9 == 0) {
                 itemsInInventory.put(slot, fillerItem);
                 slot++;
             } else if ((slot + 1) % 9 == 0) {
                 itemsInInventory.put(slot, fillerItem);
                 slot++;
                 itemsInInventory.put(slot, fillerItem);
                 slot++;
             }
             itemsInInventory.put(slot, itemStack);
         }
         return itemsInInventory;
    }

    public static void addItemsToInventory(Player player, ItemStack checkItemToBeCloned, int amount) {
        Inventory inventory = player.getInventory();
        ItemStack cloned = checkItemToBeCloned.clone();
        int amountLeft = amount;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (amountLeft <= 0) {
                break;
            }
            if (i > 35) continue;
            ItemStack itemAtSlot = inventory.getItem(i);
            if (itemAtSlot == null || itemAtSlot.getType() == Material.AIR) {
                if (amountLeft > 64) {
                    cloned.setAmount(64);
                    amountLeft -= 64;
                } else {
                    cloned.setAmount(amountLeft);
                    amountLeft = 0;
                }
                inventory.setItem(i, cloned);
            } else if (itemAtSlot.isSimilar(cloned)) {
                int amountOpen = 64 - itemAtSlot.getAmount();
                if (amountOpen < 0) continue;
                if (amountOpen > amountLeft) {
                    itemAtSlot.setAmount(itemAtSlot.getAmount() + amountLeft);
                    amountLeft = 0;
                } else {
                    itemAtSlot.setAmount(64);
                    amountLeft -= amountOpen;
                }
            }
        }
        Location location = player.getLocation();
        if (location.getWorld() == null) return;
        World world = location.getWorld();
        while (amountLeft > 0) {
            if (amountLeft > 64) {
                cloned.setAmount(64);
                amountLeft -= 64;
            } else {
                cloned.setAmount(amountLeft);
                amountLeft = 0;
            }
            world.dropItem(location, cloned);
        }
    }



    public static int getTotalItems(Player player, ItemStack item) {
         int totalAmount = 0;
         Inventory inventory = player.getInventory();
         for (int i = 0; i < inventory.getSize(); i++) {
             ItemStack invItem = inventory.getItem(i);
             if (invItem == null) continue;
             if (invItem.isSimilar(item)) totalAmount+=invItem.getAmount();
         }
         return totalAmount;
    }

    public static String getItemName(ItemStack itemStack) {
         String name;
         if (itemStack == null || itemStack.getItemMeta() == null || itemStack.getItemMeta().getDisplayName().trim().equals("")) {
             if (itemStack != null) {
                 name = ChatUtil.capitalize(itemStack.getType().toString());
             } else {
                 name = null;
             }
         } else {
             name = itemStack.getItemMeta().getDisplayName();
         }
         return name;
    }
}
