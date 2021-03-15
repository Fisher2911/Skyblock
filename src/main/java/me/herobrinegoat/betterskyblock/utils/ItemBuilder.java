package me.herobrinegoat.betterskyblock.utils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemBuilder {

    private Material material;
    private int amount;
    private ItemMeta im;

    public ItemBuilder(ItemStack item) {
        this.material = item.getType();
        this.amount = 1;
        if (item.getItemMeta() != null) {
            this.im = item.getItemMeta();
        } else {
            im = Bukkit.getItemFactory().getItemMeta(material);
        }
    }

    public ItemBuilder(Material material) {
        this.material = material;
        this.amount = 1;
        im = Bukkit.getItemFactory().getItemMeta(material);
    }

    public ItemBuilder setMaterial(Material material) {
        this.material = material;
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.amount = amount;
        return this;
    }

    public ItemBuilder setName(String name) {
        if (im != null) {
            im.setDisplayName(name);
        }
        return this;
    }

    public ItemBuilder addLore(List<String> lore) {
        List<String> addLore = new ArrayList<>();
        if (im != null) {
            if (im.getLore() != null) {
                addLore = im.getLore();
            }
            for (String s : lore) {
                addLore.add(s);
            }
            im.setLore(addLore);
        }
        return this;
    }

    public ItemBuilder addLore(String... lores) {
        List<String> lore = new ArrayList<>();
        if (im != null) {
            if (im.getLore() != null) {
                lore = im.getLore();
            }
            for (String s : lores) {
                lore.add(s);
            }
            im.setLore(lore);
        }
        return this;
    }

    public ItemBuilder setEnchants(Map<Enchantment, Integer> enchants) {
        if (im != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                im.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        if (im != null) {
            im.addEnchant(enchantment, level, true);
        }
        return this;
    }

    public ItemBuilder setItemFlags(Set<ItemFlag> itemFlags) {
        if (im != null) {
            for (ItemFlag flag : itemFlags) {
                im.addItemFlags(flag);
            }
        }
        return this;
    }

    public ItemBuilder setUnbreakable(boolean unbreakable) {
        im.setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder addPersistentData(PersistentDataType type, NamespacedKey key, Object stored) {
        PersistentDataContainer data = im.getPersistentDataContainer();
        data.set(key, type, stored);
        return this;
    }

    public ItemStack get() {
        ItemStack item = new ItemStack(this.material, this.amount);
        item.setItemMeta(this.im);
        return item;
    }

}
