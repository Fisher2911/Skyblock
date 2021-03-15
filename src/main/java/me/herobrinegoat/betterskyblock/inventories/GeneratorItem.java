package me.herobrinegoat.betterskyblock.inventories;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.generators.InventoryGenerator;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import me.herobrinegoat.betterskyblock.utils.ItemBuilder;
import me.herobrinegoat.menuapi.itemtypes.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static org.bukkit.ChatColor.*;

public class GeneratorItem extends Button {

    private InventoryGenerator inventoryGenerator;
    private BetterSkyblock plugin;

    public GeneratorItem(ItemStack itemStack, InventoryGenerator inventoryGenerator, BetterSkyblock plugin) {
        super(itemStack);
        this.inventoryGenerator = inventoryGenerator;
        setItemStack(getGeneratorItem());
        this.plugin = plugin;
    }

    public InventoryGenerator getInventoryGenerator() {
        return inventoryGenerator;
    }

    public void setInventoryGenerator(InventoryGenerator inventoryGenerator) {
        this.inventoryGenerator = inventoryGenerator;
    }

    public void setGeneratorItem() {
        setItemStack(getGeneratorItem());
    }

    private ItemStack getGeneratorItem() {
        ItemStack generatedItem = inventoryGenerator.getGeneratedItemStack();
        int resources = inventoryGenerator.getTotalResources();
        String name = ChatUtil.capitalize(generatedItem.getType().toString());
        List<String> lore = new ArrayList<>();
        ItemMeta im = generatedItem.getItemMeta();
        if (im != null) {
            if (!im.getDisplayName().trim().equalsIgnoreCase("")) {
                name = im.getDisplayName();
            }
            if (im.getLore() != null && im.getLore().isEmpty()) {
                lore = im.getLore();
            }
        }

        int level = inventoryGenerator.getLevel();
        String separator = ChatUtil.getDashSeparator(GRAY.toString(), 20);
        Upgrades.Upgrade upgrade = inventoryGenerator.getUpgrade(level + 1);
        lore.add(" ");
        lore.add(YELLOW + "Right Click to Collect Items");
        lore.add(YELLOW + "Left Click to Upgrade Generator");
        lore.add(separator);
        if (level <= 0) {
            lore.add(RED + "Broken");
            lore.add(YELLOW + "Upgrade your generator to start generating items.");
        } else {
            lore.add(BLUE + "Resources Generated - " + GREEN + resources);
            lore.add(AQUA + "Level - " + GREEN + level);
            lore.add(AQUA + "Speed - " + GREEN + inventoryGenerator.getSpeed());
            lore.add(AQUA + "Max Items - " + GREEN + inventoryGenerator.getMaxResources());
        }
        if (!inventoryGenerator.isMaxLevel()) {
            lore.add(separator);
            lore.add(YELLOW + "Money Price - " + GREEN + "$" + upgrade.getMoneyCost());
            lore.add(YELLOW + "Item Price - " + GREEN + upgrade.getItemCost());
            lore.add(AQUA + "Upgraded Speed - " + GREEN + upgrade.getSpeed());
            lore.add(AQUA + "Upgraded Max Items - " + GREEN + upgrade.getMaxItems());
        }
        int amount = Math.min(resources, 64);
        amount = Math.max(1, amount);
        return new ItemBuilder(generatedItem.getType()).setAmount(amount).setName(name).
                addLore(lore).get();
    }

    @Override
    public void run(Player player, ClickType clickType) {
        ItemStack generatedItem = inventoryGenerator.getGeneratedItemStack();
        int level = inventoryGenerator.getLevel();
        Upgrades.Upgrade upgrade = inventoryGenerator.getUpgrade(level + 1);
        User user = plugin.getUserManager().getUser(player);
        if (user == null || !user.hasIsland()) {
            return;
        }
        int resourcesGenerated = inventoryGenerator.getTotalResources();
        if (clickType == ClickType.RIGHT) {
            if (resourcesGenerated <= 0 || level <= 0) {
                return;
            }
            if (!user.hasPermission(user.getIsland(plugin), "GENERATOR_COLLECT", plugin)) {
                player.sendMessage(Messages.NO_PERMISSION_TO_COLLECT);
                return;
            }
            InventoryUtil.addItemsToInventory(player, generatedItem, resourcesGenerated);
            inventoryGenerator.setTotalResources(0, true);
            player.sendMessage(GREEN + "You have collected " + resourcesGenerated + " items");
        } else if (clickType == ClickType.LEFT) {
            if (inventoryGenerator.isMaxLevel()) {
                player.sendMessage(RED + "This inventoryGenerator is already max level.");
                return;
            }
            if (user.getBalance() < upgrade.getMoneyCost()) {
                player.sendMessage(RED + "You do not have enough money");
                return;
            }
            if (!InventoryUtil.checkInventoryForItems(player.getInventory(), generatedItem, upgrade.getItemCost(), true)) {
                player.sendMessage(RED + "You do not have enough items!");
                return;
            }
            user.setBalance(user.getBalance() - upgrade.getMoneyCost());
            inventoryGenerator.upgrade();
            player.sendMessage(GREEN + "You have upgraded your inventoryGenerator!");
            if (inventoryGenerator.getLevel() == 1) {
                inventoryGenerator.setTotalResources(0, true);
            }
        }
    }
}
