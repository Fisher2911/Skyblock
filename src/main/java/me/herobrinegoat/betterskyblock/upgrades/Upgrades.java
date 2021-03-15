package me.herobrinegoat.betterskyblock.upgrades;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Upgrades {

    private final Map<Integer, Upgrade> upgrades;
    private ItemStack item;

    public Upgrades(ItemStack item) {
        this.upgrades = new HashMap<>();
        this.item = item;
    }

    public Upgrades(HashMap<Integer, Upgrade> upgrades) {
        this.upgrades = upgrades;
    }

    public void putUpgrade(int level, Upgrade upgrade) {
        this.upgrades.put(level, upgrade);
    }

    public void removeUpgrade(int level) {
        this.upgrades.remove(level);
    }

    public Map<Integer, Upgrade> getUpgrades() {
        return this.upgrades;
    }

    public Upgrade getUpgrade(int level) {
        return upgrades.get(level);
    }

    public boolean hasUpgrades(int level) {
        return upgrades.containsKey(level);
    }

    public int getMaxLevel() {
        int maxLevel = 0;
        for (int i : upgrades.keySet()) {
            if (i > maxLevel) {
                maxLevel = i;
            }
        }
        return maxLevel;
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack item) {
        this.item = item;
    }

    public class Upgrade {
        private int moneyCost;
        private int itemCost;
        private float speed;
        private int maxItems;

        public Upgrade(int moneyCost, int itemCost, float speed, int maxItems) {
            this.moneyCost = moneyCost;
            this.itemCost = itemCost;
            this.speed = speed;
            this.maxItems = maxItems;
        }

        public int getMoneyCost() {
            return moneyCost;
        }

        public void setMoneyCost(int moneyCost) {
            this.moneyCost = moneyCost;
        }

        public int getItemCost() {
            return itemCost;
        }

        public void setItemCost(int itemCost) {
            this.itemCost = itemCost;
        }

        public float getSpeed() {
            return speed;
        }

        public void setSpeed(float speed) {
            this.speed = speed;
        }

        public int getMaxItems() {
            return maxItems;
        }

        public void setMaxItems(int maxItems) {
            this.maxItems = maxItems;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Upgrade)) return false;
            Upgrade upgrade = (Upgrade) o;
            return moneyCost == upgrade.moneyCost &&
                    itemCost == upgrade.itemCost &&
                    Float.compare(upgrade.speed, speed) == 0 &&
                    maxItems == upgrade.maxItems;
        }

        @Override
        public int hashCode() {
            return Objects.hash(moneyCost, itemCost, speed, maxItems);
        }
    }

    public class PlaceableGeneratorUpgrade extends Upgrades.Upgrade {

        private int stackSize;

        public PlaceableGeneratorUpgrade(int moneyCost, int itemCost, float speed, int maxItems, int stackSize) {
            super(moneyCost, itemCost, speed, maxItems);
            this.stackSize = stackSize;
        }

        public int getStackSize() {
            return stackSize;
        }

        public void setStackSize(int stackSize) {
            this.stackSize = stackSize;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PlaceableGeneratorUpgrade)) return false;
            if (!super.equals(o)) return false;
            PlaceableGeneratorUpgrade that = (PlaceableGeneratorUpgrade) o;
            return stackSize == that.stackSize;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), stackSize);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Upgrades)) return false;
        Upgrades upgrades1 = (Upgrades) o;
        return Objects.equals(upgrades, upgrades1.upgrades) &&
                Objects.equals(item, upgrades1.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(upgrades, item);
    }
}
