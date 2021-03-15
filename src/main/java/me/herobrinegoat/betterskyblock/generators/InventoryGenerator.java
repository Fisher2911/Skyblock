package me.herobrinegoat.betterskyblock.generators;

import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;

public class InventoryGenerator extends Generator implements Comparable<InventoryGenerator> {

    public InventoryGenerator(int id, ItemStack generatedItemStack, int totalResources, LocalDateTime lastGeneratedDate, Upgrades upgrades, int rank, int level) {
        super(id, generatedItemStack, totalResources, lastGeneratedDate, upgrades, rank, level);
    }

    public InventoryGenerator(InventoryGenerator inventoryGenerator) {
        super(inventoryGenerator.getId(), inventoryGenerator.getGeneratedItemStack(), inventoryGenerator.getTotalResources(),
                inventoryGenerator.getLastGeneratedDate(), inventoryGenerator.getUpgrades(), inventoryGenerator.getRank(), inventoryGenerator.getLevel());
    }

    @Override
    public void generate() {
        super.generate();
    }

    @Override
    public void upgrade() {
        super.upgrade();
    }

    @Override
    public Upgrades.Upgrade getUpgrade(int level) {
        return getUpgrades().getUpgrade(level);
    }

    @Override
    public int compareTo(InventoryGenerator inventoryGenerator) {
        Upgrades upgrades = getUpgrades();
        Upgrades compareUpgrades = inventoryGenerator.getUpgrades();
        int num = 0;
        if (upgrades.getUpgrades().get(1) != null && compareUpgrades.getUpgrades().get(1) != null) {
            Upgrades.Upgrade upgrade = upgrades.getUpgrades().get(1);
            Upgrades.Upgrade compareUpgrade = compareUpgrades.getUpgrades().get(1);

            int upgradePrice = upgrade.getMoneyCost();
            int comparePrice = compareUpgrade.getMoneyCost();
            if (upgradePrice > comparePrice) {
                num = 1;
            } else if (upgradePrice < comparePrice) {
                num = -1;
            } else {
                num = this.getGeneratedItemStack().getType().toString().compareTo(inventoryGenerator.getGeneratedItemStack().getType().toString());
            }
        }
        return num;
    }
}
