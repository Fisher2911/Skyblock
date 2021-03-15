package me.herobrinegoat.betterskyblock.inventories;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import me.herobrinegoat.betterskyblock.utils.ItemBuilder;
import me.herobrinegoat.menuapi.Page;
import me.herobrinegoat.menuapi.itemtypes.Button;
import me.herobrinegoat.menuapi.itemtypes.PageItem;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

import static org.bukkit.ChatColor.*;

public class ExchangeMenu {

    private BetterSkyblock plugin;

    private Page mainPage;
    Map<String, Page> pages;
    private Button mainPageButton;

    public ExchangeMenu(BetterSkyblock plugin) {
        setMainPageButton(Material.PAPER);
        this.plugin = plugin;
    }

    public void setPage(int inventorySize) {
        List<ExchangeItem> exchangeItemList = plugin.getExchangeItemsConfig().getExchangeItems();
        
        Set<ItemFlag> set = new HashSet<>();
        set.add(ItemFlag.HIDE_ENCHANTS);
        ItemStack blocksGeneratorMenuItem = new ItemBuilder(Material.COBBLESTONE).setAmount(1).setName(BLUE + "Block Generators Exchange Menu").addEnchant(Enchantment.DURABILITY, 1).setItemFlags(set).get();
        ItemStack mobGeneratorMenuItem= new ItemBuilder(Material.ROTTEN_FLESH).setAmount(1).setName(BLUE + "Mobs Generators Exchange Menu").addEnchant(Enchantment.DURABILITY, 1).setItemFlags(set).get();

        Page blocksGeneratorPage = getCategoryPage(ExchangeItem.Type.BLOCK_GENERATOR, exchangeItemList, 45, "blocksGeneratorPage");
        Page mobGeneratorPage = getCategoryPage(ExchangeItem.Type.MOB_GENERATOR, exchangeItemList, 45, "mobDropsGeneratorPage");
        
        Button blocksGeneratorsButton = new Button(blocksGeneratorMenuItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                blocksGeneratorPage.openPage(player);
            }
        };
        Button mobsGeneratorsButton = new Button(mobGeneratorMenuItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                mobGeneratorPage.openPage(player);
            }
        };

        Map<Integer, PageItem> mainPageItems = new HashMap<>();
//        mainPageItems.put(0, blocksButton);
//        mainPageItems.put(1, mobsButton);
        mainPageItems.put(0, blocksGeneratorsButton);
        mainPageItems.put(1, mobsGeneratorsButton);
        pages = new HashMap<>();
//        pages.put("blocksPage", blocksPage);
//        pages.put("mobDropsPage", mobsPage);
        pages.put("blocksGeneratorPage", blocksGeneratorPage);
        pages.put("mobDropsGeneratorPage", mobGeneratorPage);
        this.mainPage = new Page(mainPageItems, null, inventorySize, BLUE + "Exchange Menu", InventoryUtil.getFillerItem(Material.BLACK_STAINED_GLASS_PANE));
    }

    private Page getCategoryPage(ExchangeItem.Type type, List<ExchangeItem> exchangeItemsFullList, int inventorySize, String pageName) {
        Map<Integer, PageItem> pageItems = new HashMap<>();
        int slot = 0;
        for (ExchangeItem exchangeItem : exchangeItemsFullList) {
            if (exchangeItem.getItemStack() == null) {
                continue;
            }
            if (slot == getMainPageButtonSlot(inventorySize)) {
                slot++;
            }
            if (exchangeItem.getType() == type) {
                setPlaceholderLore(exchangeItem);
                ExchangeItem placeHolder = new ExchangeItem(exchangeItem) {
                    final Page transactionPage = getTransactionPage(exchangeItem, pageName);
                    @Override
                    public void run(Player player, ClickType clickType) {
                        transactionPage.openPage(player);
                    }
                };
                pageItems.put(slot, placeHolder);
                slot++;
            }
        }
        pageItems.put(getMainPageButtonSlot(inventorySize), mainPageButton);
        ItemStack nextPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Next Page").get();
        ItemStack previousPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Previous Page").get();
        return new Page(pageItems, null, inventorySize, BLUE + ChatUtil.capitalize(type.toString()), true, BLUE + "Page", BLUE.toString(), BLUE.toString(),
                nextPageItem, previousPageItem, InventoryUtil.getFillerItem(), getMainPageButtonSlot(inventorySize));
    }

    private Page getTransactionPage(ExchangeItem exchangeItem, String pageName) {
        Map<Integer, PageItem> pageItems = new HashMap<>();
        int subtract = 0;

        int moneyCost = exchangeItem.getReqMoney();
        int itemCost = exchangeItem.getReqItemAmount();

        int slot = 10;
        for (int i = 0; i < 21; i++) {
            if (i == 7) subtract = 7;
            if (i == 14) subtract = 14;
            int power = i - subtract;
            int gainedAmount = (int) Math.pow(2, power);
            int reqAmount = gainedAmount * itemCost;
            PageItem pageItem;
            if (i < 7) {
                ItemStack displayItem = new ItemBuilder(exchangeItem.getExchangedItem().clone()).setAmount(reqAmount).get();
                pageItem = new PageItem(displayItem);
            } else if (i < 14) {
                pageItem = getAnvilItem(moneyCost * gainedAmount, reqAmount, gainedAmount, exchangeItem);
            } else {
                ItemStack displayItem = new ItemBuilder(exchangeItem.getGainedItem().clone()).setAmount(gainedAmount).get();
                pageItem = new PageItem(displayItem);
            }
            pageItems.put(slot, pageItem);
            slot++;
            if (slot % 9 == 0) {
                slot++;
            } else if ((slot + 1) % 9 == 0) {
                slot+=2;
            }
        }
        Button previousPageButton = new Button(InventoryUtil.makeItem(Material.PAPER, 1, BLUE + "Go back")) {
            @Override
            public void run(Player player, ClickType clickType) {
                Page page = pages.get(pageName);
                if (page == null) {
                    return;
                }
                page.openPage(player);
            }
        };
        pageItems.put(40, previousPageButton);
        for (int i = 0; i < 45; i++) {
            if (!pageItems.containsKey(i)) {
                pageItems.put(i, new PageItem(InventoryUtil.getFillerItem()));
            }
        }
        return new Page(pageItems, null, 45, BLUE + "Exchange Menu");
    }

    private ExchangeItem getAnvilItem(int moneyCost, int itemCost, int itemsGained, ExchangeItem exchangeItem) {
        List<String> lore = new ArrayList<>();
        lore.add("");
        lore.add(ChatUtil.getDashSeparator(AQUA + "", 10));
        lore.add(GREEN + "Items required - " + BLUE + itemCost);
        lore.add(GREEN + "Money required - " + BLUE + "$" + moneyCost);
        lore.add(GREEN + "Items gained - " + BLUE + itemsGained);
        ItemStack anvilItem = InventoryUtil.makeItem(Material.ANVIL, 1);
        ItemMeta meta = anvilItem.getItemMeta();
        if (meta != null) {
            meta.setLore(lore);
            anvilItem.setItemMeta(meta);
        }
        exchangeItem.setItemStack(anvilItem);
        return new ExchangeItem(exchangeItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                User user = plugin.getUserManager().getUser(player);
                double balance = user.getBalance();
                if (balance < moneyCost) {
                    player.sendMessage(RED + "You do not have enough money!");
                    return;
                }
                if (!InventoryUtil.checkInventoryForItems(player.getInventory(), exchangeItem.getExchangedItem(), itemCost, true)) {
                    player.sendMessage(RED + "You do not have enough items!");
                    return;
                }
                user.setBalance(balance - moneyCost);
                InventoryUtil.addItemsToInventory(player, exchangeItem.getGainedItem(), itemsGained);
                player.sendMessage(GREEN + "Exchange Successful!");
            }
        };
    }

    public void setPlaceholderLore(ExchangeItem exchangeItem) {
        ItemStack item = exchangeItem.getItemStack().clone();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        List<String> lore = meta.getLore();
        if (lore == null) return;
        lore.add("");
        lore.add(ChatUtil.getDashSeparator(AQUA + "", 10));
        lore.add(GREEN + "Money Requirement - $" + exchangeItem.getReqMoney());
        lore.add(GREEN + "Item Requirement - " + exchangeItem.getReqItemAmount());
        meta.setLore(lore);
        item.setItemMeta(meta);
        exchangeItem.setItemStack(item);
    }

    public void openPage(Player player) {
        if (mainPage == null) {
            setMainPageButton(Material.CHEST);
            setPage(27);
        }
        mainPage.openPage(player);
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
