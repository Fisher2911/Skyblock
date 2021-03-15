package me.herobrinegoat.betterskyblock.generators;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.inworld.Placeable;
import me.herobrinegoat.betterskyblock.managers.UserManager;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.*;
import me.herobrinegoat.menuapi.Page;
import me.herobrinegoat.menuapi.itemtypes.Button;
import me.herobrinegoat.menuapi.itemtypes.PageItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.time.LocalDateTime;
import java.util.*;

import static org.bukkit.ChatColor.*;

public abstract class PlaceableGenerator extends Generator implements Placeable {

    private Location location;
    private int stackAmount;
    private boolean removed;
    private Page page;

    public PlaceableGenerator(int id, ItemStack generatedItemStack, int totalResources, LocalDateTime lastGeneratedDate,  Upgrades upgrades, int rank, int level,
                              Location location, int stackAmount) {
        super(id, generatedItemStack, totalResources, lastGeneratedDate, upgrades, rank, level);
        this.location = location;
        this.stackAmount = stackAmount;
        this.removed = false;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public ItemStack getItemForInventory(BetterSkyblock plugin) {
        ItemStack generatedItem = getGeneratedItemStack();
        if (generatedItem == null) {
            return null;
        }
        ItemBuilder itemBuilder = new ItemBuilder(generatedItem.getType());
        String itemName = ChatUtil.capitalize(generatedItem.getType().toString());
        List<String> lore = new ArrayList<>();
        if (generatedItem.getItemMeta() != null) {
            ItemMeta im = generatedItem.getItemMeta();
            String displayName = im.getDisplayName();
            List<String> itemLore = im.getLore();
            if (!displayName.trim().equals("")) {
                itemName = displayName;
            }
            if (itemLore != null && !itemLore.isEmpty()) {
                lore = itemLore;
            }
        }

        NamespacedKey levelKey = new NamespacedKey(plugin, "level");
        NamespacedKey spawnedItemKey = new NamespacedKey(plugin, "spawnedItem");
        NamespacedKey rankKey = new NamespacedKey(plugin, "rank");

        lore.add("");
        lore.add(ChatUtil.getDashSeparator(GRAY.toString(), 30));
        addGeneratorLore(lore, "Level", getLevel());

        Set<ItemFlag> set = new HashSet<>();
        set.add(ItemFlag.HIDE_ENCHANTS);
        itemBuilder.setAmount(1).addLore(lore).addEnchant(Enchantment.DURABILITY, 1).setItemFlags(set)
                .addPersistentData(PersistentDataType.INTEGER, levelKey, getLevel())
                .addPersistentData(PersistentDataType.STRING, spawnedItemKey, ItemStackSerializer.itemStackToBase64(generatedItem))
                .addPersistentData(PersistentDataType.INTEGER, rankKey, getRank());
        if (!itemName.equalsIgnoreCase(ChatUtil.capitalize(generatedItem.getType().toString()))) {
            itemBuilder.setName(ChatUtil.capitalize(itemName) + " Generator");
        } else {
            itemBuilder.setName(RESET + ChatUtil.capitalize(ChatUtil.capitalize(generatedItem.getType().toString()) + " Generator"));
        }
        return itemBuilder.get();
    }

    protected void addGeneratorLore(List<String> list, String key, float value) {
        StringUtil.addItemWithInfoToList(list, AQUA + key, GREEN + String.valueOf(value), GRAY.toString());
    }

    public static boolean isGeneratorItem(BetterSkyblock plugin, ItemStack item) {
        if (item == null || item.getItemMeta() == null) {
            return false;
        }

        ItemMeta im = item.getItemMeta();

        NamespacedKey levelKey = new NamespacedKey(plugin, "level");
        NamespacedKey spawnedItemKey = new NamespacedKey(plugin, "spawnedItem");

        PersistentDataContainer data = im.getPersistentDataContainer();
        return data.has(levelKey, PersistentDataType.INTEGER) &&
                data.has(spawnedItemKey, PersistentDataType.STRING);
    }

    public int getMaxInStack() {
        if (getCurrentUpgrade() instanceof Upgrades.PlaceableGeneratorUpgrade) {
            return ((Upgrades.PlaceableGeneratorUpgrade) getCurrentUpgrade()).getStackSize();
        }
        return 1;
    }

    private void setPage(BetterSkyblock plugin) {
        if (getUpgrades() == null) {
            return;
        }
        if (!(getUpgrade(getLevel() + 1) instanceof Upgrades.PlaceableGeneratorUpgrade) && !isMaxLevel()) {
            return;
        }
        Upgrades.PlaceableGeneratorUpgrade upgrade = (Upgrades.PlaceableGeneratorUpgrade) getUpgrade(getLevel() + 1);
        Map<Integer, PageItem> pageItems = new HashMap<>();
        String limit = "Max Blocks";
        if (this instanceof MobGenerator) {
            limit = "Max Mobs";
        }

        ItemStack stackAmountItem = new ItemBuilder(Material.PAPER).setAmount(Math.min(64, stackAmount)).setName(BLUE.toString() + stackAmount + "x Generators").get();
        ItemStack levelItem = new ItemBuilder(Material.EXPERIENCE_BOTTLE).setAmount(1).setName(AQUA + "Level - " + AQUA.toString() + getLevel()).addLore("",
                GREEN + "Speed - " + AQUA.toString() + getSpeed(), GREEN + "Max Stack Size - " + AQUA.toString() + getMaxInStack(), GREEN + limit + " - " + AQUA + getMaxResources()).get();
        ItemStack removeItem = new ItemBuilder(Material.BARRIER).setAmount(1).setName(RED + "Remove Generator").addLore("", BLUE + "Right Click - Remove One",
                BLUE + "Shift + Right Click - Remove 5", BLUE + "Middle Click - Remove All").get();
        ItemStack upgradeItem;
        PageItem stackAmountPageItem = new PageItem(stackAmountItem);
        PageItem levelPageItem = new PageItem(levelItem);

        PageItem upgradeButton;

        if (getLevel() < getUpgrades().getMaxLevel()) {
            upgradeItem = new ItemBuilder(Material.ANVIL).setAmount(Math.max(1, getLevel())).setName(AQUA + "Upgrade").addLore("",
                    BLUE + "Money Price -" + GREEN + " $" + (upgrade.getMoneyCost() * stackAmount), BLUE + "Item Price - " +
                            GREEN + (upgrade.getItemCost() * stackAmount),
                    GREEN + "Upgraded Speed - " + AQUA + upgrade.getSpeed(), GREEN + "Upgraded Max Stack Size " + AQUA + upgrade.getStackSize(),
                    GREEN + "Upgraded Max Blocks - " + AQUA + upgrade.getMaxItems()).get();
            upgradeButton = new Button(upgradeItem) {
                @Override
                public void run(Player player, ClickType clickType) {
                    User user = plugin.getUserManager().getUser(player);
                    Island island = plugin.getIslandManager().getIslandFromLocation(location);
                    if (user == null) {
                        return;
                    }
                    if (!user.hasPermission(island, "GENERATOR_UPGRADE", plugin)) {
                        player.sendMessage(RED + "You do not have permission for this!");
                        return;
                    }
                    double balance = user.getBalance();
                    if (balance < (upgrade.getMoneyCost() * stackAmount)) {
                        player.sendMessage(RED + "You do not have enough money!");
                        return;
                    }
                    if (!InventoryUtil.checkInventoryForItems(player.getInventory(), getGeneratedItemStack(), (upgrade.getItemCost() * stackAmount), true)) {
                        player.sendMessage(RED + "You do not have enough items!");
                        return;
                    }
                    user.setBalance(balance - upgrade.getMoneyCost());
                    upgrade();
                    player.sendMessage(GREEN + "Your generator has been upgraded!");
                    player.closeInventory();
                }
            };
        } else {
            upgradeItem = new ItemBuilder(Material.ANVIL).setAmount(Math.max(1, getLevel())).setName(GREEN + "Max Level").get();
            upgradeButton = new PageItem(upgradeItem);
        }
        Button removeButton = new Button(removeItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                User user = plugin.getUserManager().getUser(player);
                Island island = plugin.getIslandManager().getIslandFromLocation(location);
                if (user == null) {
                    return;
                }
                if (!user.hasPermission(island, "GENERATOR_REMOVE", plugin)) {
                    player.sendMessage(RED + "You do not have permission for this!");
                    return;
                }
                if (clickType == ClickType.RIGHT) {
                    removeGenerator(user, 1, plugin);
                } else if (clickType == ClickType.SHIFT_RIGHT) {
                    removeGenerator(user, 5, plugin);
                } else if (clickType == ClickType.MIDDLE) {
                    removeGenerator(user, stackAmount, plugin);
                }
                player.closeInventory();;
            }
        };
        pageItems.put(0, levelPageItem);
        pageItems.put(1, stackAmountPageItem);
        pageItems.put(2, upgradeButton);
        pageItems.put(3, removeButton);
        String name;
        if (getGeneratedItemStack().getItemMeta() == null || getGeneratedItemStack().getItemMeta().getDisplayName().trim().equals("")) {
            name = getGeneratedItemStack().getType().toString();
        } else {
            name = getGeneratedItemStack().getItemMeta().getDisplayName();
        }
        this.page = new Page(pageItems, null, 27, name +  " Generator", InventoryUtil.getFillerItem(Material.BLACK_STAINED_GLASS_PANE));
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public void setLocation(Location location) {
        this.location = location;
    }

    public long getChunkLong() {
        long x = (location.getBlockX() >> 4);
        long z = (location.getBlockZ() >> 4);
        return x & 0xffffffffL | (z & 0xffffffffL) << 32;
    }

    public void setStackAmount(int stackAmount) {
        this.stackAmount = stackAmount;
    }

    public int getStackAmount() {
        return stackAmount;
    }

    public boolean placeInWorld(BlockFace direction, Location clickedLocation, Material setMaterial, User user, BetterSkyblock plugin) {
        Location setLoc = clickedLocation.getBlock().getRelative(direction, 1).getLocation();
        if (user == null || !user.isOnline()) {
            return false;
        }
        boolean canPlace;
        Island island;
        if (!user.hasIsland()) {
            island = plugin.getIslandManager().getIslandFromLocation(clickedLocation);
        } else {
            island = user.getIsland(plugin);
        }
        if (!island.withinIsland(setLoc, false)) {
            user.getPlayer().sendMessage(Messages.OUT_OF_ISLAND_RANGE);
            return false;
        }
        canPlace = user.hasPermission(island, "GENERATOR_PLACE", plugin);
        Player player = user.getPlayer();
        if (!canPlace) {
            player.sendMessage(RED + "You do not have permission for this!");
            return false;
        }
        PlaceableGenerator generatorAtLoc = island.getGeneratorFromLocation(clickedLocation);
        if (generatorAtLoc != null && !player.isSneaking()) {
            if (generatorAtLoc.getClass().equals(this.getClass())) {
                setId(generatorAtLoc.getId());
                setLocation(generatorAtLoc.getLocation());
                if (generatorAtLoc.getLevel() == getLevel()) {

                    int addAmount = 1;
                    if (player.isSneaking()) {
                        if (player.getInventory().getItemInMainHand().equals(getItemForInventory(plugin))) {
                            addAmount = player.getInventory().getItemInMainHand().getAmount();
                        }
                    }

                    int amountInStack = generatorAtLoc.getStackAmount();
                    if (addAmount + amountInStack <= generatorAtLoc.getMaxInStack()) {
                        generatorAtLoc.setStackAmount(addAmount + generatorAtLoc.getStackAmount());
                        ItemStack itemInHand = player.getInventory().getItemInMainHand();
                        player.getInventory().getItemInMainHand().setAmount(itemInHand.getAmount() - 1);
                        player.sendMessage(GREEN.toString() + addAmount +  "x Generators Added!");
                        this.setLocation(setLoc);
                        setStackAmount(generatorAtLoc.getStackAmount());
                        return true;
                    } else {
                        player.sendMessage(RED + "That is more than the max stack size!");
                        return false;
                    }

                } else {
                    player.sendMessage(RED + "These generators are not the same level, so you cannot combine them!");
                    return false;
                }
            }
            return false;
        }
        if (setMaterial.isBlock()) {
            setLoc.getBlock().setType(setMaterial);
        } else {
            setLoc.getBlock().setType(Material.RED_WOOL);
        }
        this.setLocation(setLoc);
        this.setId(user.getIsland(plugin).getGeneratorId(true));
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        player.getInventory().getItemInMainHand().setAmount(itemInHand.getAmount() - 1);
        island.addGenerator(this);
        setLastGeneratedDate(LocalDateTime.now());
        player.sendMessage(GREEN + "Generator Placed");
        if (this instanceof MobGenerator) {
            ((MobGenerator) this).updateSpawner();
        } else {
           location.getBlock().setType(Material.BEDROCK);
        }
        return true;
    }

    public void removeGenerator(User user, int amount, BetterSkyblock plugin) {
        if (!user.isOnline()) {
            return;
        }
        UserManager userManager = plugin.getUserManager();
        if (amount >= stackAmount) {
            location.getBlock().setType(Material.AIR);
            setRemoved(true);
        }
        int removeAmount = Math.min(amount, stackAmount);
        ItemStack generatorItem = getItemForInventory(plugin);
        if (removeAmount >= stackAmount) {
            setRemoved(true);
        } else {
            setStackAmount(stackAmount - removeAmount);
        }
        Player player = user.getPlayer();
        InventoryUtil.addItemsToInventory(player, generatorItem, removeAmount);
        player.sendMessage(GREEN.toString() + amount +  "x Generators Removed");
    }

    public void openPage(Player player, BetterSkyblock plugin) {
        setPage(plugin);
        if (page != null) {
            page.openPage(player);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaceableGenerator)) return false;
        if (!super.equals(o)) return false;
        PlaceableGenerator generator = (PlaceableGenerator) o;
        return Objects.equals(location, generator.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), location);
    }
}
