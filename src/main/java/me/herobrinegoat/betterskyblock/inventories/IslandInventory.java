package me.herobrinegoat.betterskyblock.inventories;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.SettingValue;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import me.herobrinegoat.menuapi.Page;
import me.herobrinegoat.menuapi.itemtypes.Button;
import me.herobrinegoat.menuapi.itemtypes.PageItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static me.herobrinegoat.betterskyblock.Island.Setting;
import static me.herobrinegoat.betterskyblock.Island.SettingType;
import static org.bukkit.ChatColor.*;

public class IslandInventory {

    private BetterSkyblock plugin;

    private Button mainPageButton;
    private Page mainPage;
    private Page deletePage;
    private HashMap<String, Page> settingsPages;


    public IslandInventory(Island island, BetterSkyblock plugin) {
        settingsPages = new HashMap<>();
        setMainPageButton(Material.CHEST);
        setSettings(island, 54);
        setDeletePage(island, 27);
        setMainPage(island, 27);
        this.plugin = plugin;
    }

    public Page getPage() {
        return mainPage;
    }

    private void setSettings(Island island, int inventorySize) {
        List<SettingValue> settings = island.getSettings();
        int slot = 0;
        for (SettingType settingType : SettingType.values()) {
            if (settingType == null) {
                continue;
            }
            Map<Integer, PageItem> pageItems = new HashMap<>();
            pageItems.put(getMainPageButtonSlot(inventorySize), getMainPageButton());
            String name = BLUE + ChatUtil.capitalize(settingType.toString()) + " Settings";
            for (Setting setting : Setting.values()) {
                SettingValue settingValue = new SettingValue(setting, settingType, false);
                if (settingValue.getSetting() == null) {
                    continue;
                }
                if (settingType == SettingType.ISLAND && settingValue.getSetting().getSettingType() != SettingType.ISLAND) {
                    continue;
                }
                if (settingValue.getSetting().getSettingType() == SettingType.ISLAND && settingType != SettingType.ISLAND) {
                    continue;
                }

                if (!settings.contains(settingValue)) continue;

                for (SettingValue s : settings) {
                    if (s.equals(settingValue)) {
                        settingValue.setValue(s.getValue());
                    }
                }

                boolean value = settingValue.getValue();
                Button button;

                ItemStack trueItem = InventoryUtil.makeItem(Material.GREEN_STAINED_GLASS_PANE, 1, GREEN + ChatUtil.capitalize(setting.toString()));
                ItemStack falseItem = InventoryUtil.makeItem(Material.RED_STAINED_GLASS_PANE, 1, RED + ChatUtil.capitalize(setting.toString()));

                ItemStack neatItem;
                if (value) {
                    neatItem = trueItem;
                } else {
                    neatItem = falseItem;
                }

                if (value) {
                    button = new Button(neatItem) {
                        @Override
                        public void run(Player player, ClickType clickType) {
                            User user = plugin.getUserManager().getUser(player);
                            if (user.getRole() != User.Role.OWNER) {
                                player.sendMessage(RED + "You are not the island owner!");
                                return;
                            }
                            for (SettingValue islandSettingValue : island.getSettings()) {
                                if (islandSettingValue.getSettingType() == settingType && islandSettingValue.getSetting() == settingValue.getSetting()) {
                                    if (islandSettingValue.getValue()) {
                                        setItemStack(falseItem);
                                        islandSettingValue.setValue(false);
                                    } else {
                                        setItemStack(trueItem);
                                        islandSettingValue.setValue(true);
                                    }
                                    settingsPages.get(name).updateInventory(player);
                                }
                            }
                        }
                    };
                } else {
                    button = new Button(neatItem) {
                        @Override
                        public void run(Player player, ClickType clickType) {
                            User user = plugin.getUserManager().getUser(player);
                            if (user.getRole() != User.Role.OWNER) {
                                player.sendMessage(RED + "You are not the island owner!");
                                return;
                            }
                            for (SettingValue islandSettingValue : island.getSettings()) {
                                if (islandSettingValue.getSettingType() == settingType && islandSettingValue.getSetting() == settingValue.getSetting()) {
                                    if (islandSettingValue.getValue()) {
                                        setItemStack(falseItem);
                                        islandSettingValue.setValue(false);
                                    } else {
                                        setItemStack(trueItem);
                                        islandSettingValue.setValue(true);
                                    }
                                    settingsPages.get(name).updateInventory(player);
                                }
                            }
                        }
                    };
                }
                if (slot == getMainPageButtonSlot(inventorySize)) {
                    slot++;
                }
                pageItems.put(slot, button);
                slot++;
            }

            ItemStack nextPageItem = InventoryUtil.makeItem(Material.ARROW, 1, WHITE + "Next Page");
            ItemStack previousPage = InventoryUtil.makeItem(Material.ARROW, 1, WHITE + "Previous Page");
            Page page = new Page(pageItems, null, inventorySize, BLUE + ChatUtil.capitalize(settingType.toString()) + " Settings",
                    true, GREEN + "Page", AQUA + "", GOLD + "", nextPageItem, previousPage,
                    InventoryUtil.getFillerItem(Material.BLACK_STAINED_GLASS_PANE), getMainPageButtonSlot(inventorySize));
            this.settingsPages.put(name, page);
            slot = 0;
        }
    }

    public void setDeletePage(Island island, int inventorySize) {
        HashMap<Integer, PageItem> pageItems = new HashMap<>();
        ItemStack deleteItem = InventoryUtil.makeItem(Material.BARRIER, 1, RED + "Delete Island", "", AQUA + "WARNING - This cannot be undone!");
        Button button = new Button(deleteItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                User user = plugin.getUserManager().getUser(player);
                island.deleteIsland(user, plugin);
                player.closeInventory();
            }
        };
        pageItems.put(inventorySize / 2, button);
        pageItems.put(getMainPageButtonSlot(inventorySize), getMainPageButton());
        this.deletePage = new Page(pageItems, null, inventorySize, BLUE + "Delete Island", InventoryUtil.getFillerItem(Material.BLACK_STAINED_GLASS_PANE), getMainPageButtonSlot(inventorySize));
    }

    private void setMainPage(Island island, int inventorySize) {
        Map<Integer, PageItem> pageItems = new HashMap<>();
        ItemStack memberItem = InventoryUtil.makeItem(Material.PLAYER_HEAD, 1, BLUE + "Island Members");
        ItemStack deleteIslandItem = InventoryUtil.makeItem(Material.BARRIER, 1, RED + "Delete Island");
        ItemStack homeItem = InventoryUtil.makeItem(Material.WHITE_BED, 1, BLUE + "Teleport Home");

        Button memberButton = new Button(memberItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                plugin.getUserManager().getUser(player).listIslandMembers(plugin);
                player.closeInventory();
            }
        };
        int i = 2;
        for (Map.Entry<String, Page> entry : this.settingsPages.entrySet()) {
            String name = entry.getKey();
            Page page = entry.getValue();
            ItemStack settingsItem = InventoryUtil.makeItem(Material.REPEATER, 1, name);
            Button button = new Button(settingsItem) {
                @Override
                public void run(Player player, ClickType clickType) {
                    page.openPage(player);
                }
            };
            pageItems.put(i, button);
            i++;
        }
        Button deleteButton = new Button(deleteIslandItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                User user = plugin.getUserManager().getUser(player);
                if (user.getRole() != User.Role.OWNER) {
                    player.sendMessage(RED + "You do not have permission for this!");
                    return;
                }
                island.deleteIsland(user, plugin);
                deletePage.openPage(player);
            }
        };
        Button homeButton = new Button(homeItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                User user = plugin.getUserManager().getUser(player);
                user.getIsland(plugin).teleportToArea(user, plugin, island.getHome());
                player.closeInventory();
            }
        };
        pageItems.put(0, homeButton);
        pageItems.put(1, memberButton);
        pageItems.put(i, deleteButton);
        this.mainPage = new Page(pageItems, null, inventorySize, BLUE + "Island Menu", InventoryUtil.getFillerItem(Material.BLACK_STAINED_GLASS_PANE));
    }

    private void setMainPageButton(Material material) {
        ItemStack itemStack = InventoryUtil.makeItem(material, 1, BLUE + "Return to menu");
        this.mainPageButton = new Button(itemStack) {
            @Override
            public void run(Player player, ClickType clickType) {
                mainPage.openPage(player);
            }
        };
    }

    private Button getMainPageButton() {
        return this.mainPageButton;
    }

    private int getMainPageButtonSlot(int inventorySize) {
        return inventorySize - 5;
    }

}
