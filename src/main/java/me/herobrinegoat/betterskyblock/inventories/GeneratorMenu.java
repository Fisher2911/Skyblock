package me.herobrinegoat.betterskyblock.inventories;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.generators.InventoryGenerator;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.InventoryUtil;
import me.herobrinegoat.betterskyblock.utils.ItemBuilder;
import me.herobrinegoat.menuapi.Page;
import me.herobrinegoat.menuapi.itemtypes.Button;
import me.herobrinegoat.menuapi.itemtypes.PageItem;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bukkit.ChatColor.*;

public class GeneratorMenu {

    private BetterSkyblock plugin;

    private Page mainPage;
    private Page mobDropGeneratorsPage;
    private Page blockGeneratorsPage;
    private Button mainPageButton;

    public GeneratorMenu(Island island, BetterSkyblock plugin) {
        this.plugin = plugin;
        setMainPageButton(Material.PAPER);
        setBlockAndMobGeneratorsPage(island, 45);
        setMainPage(island, 45);
    }

    public void openPage(Island island, Player player) {
        if (mainPage == null) {
            setMainPage(island, 45);
        }
//        setBlockAndMobGeneratorsPage(island, 45);
        mainPage.openPage(player);
    }

    private void setMainPage(Island island, int inventorySize) {
        ItemStack blockGeneratorsItem = new ItemBuilder(Material.COBBLESTONE).setName(YELLOW + "Block Generators").get();
        ItemStack mobGeneratorsItem = new ItemBuilder(Material.ROTTEN_FLESH).setName(YELLOW + "Mob Drop Generators").get();
        ItemStack nextPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Next Page").get();
        ItemStack previousPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Previous Page").get();
        Button blockGeneratorsButton = new Button(blockGeneratorsItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                if (blockGeneratorsPage != null) {
                    blockGeneratorsPage.openPage(player);
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            boolean cancel = true;
                            for (Page page : blockGeneratorsPage.getAllPages()) {
                                if (page.getInventory().getViewers().contains(player)) {
                                    cancel = false;
                                    break;
                                }
                            }
                            if (cancel) {
                                cancel();
                                return;
                            }
                            Page page = blockGeneratorsPage.getPlayersOpenPage(player);
                            for (PageItem pageItem : page.getPageItems().values()) {
                                if (!(pageItem instanceof GeneratorItem)) {
                                    continue;
                                }
                                GeneratorItem generatorItem = (GeneratorItem) pageItem;
                                generatorItem.getInventoryGenerator().generate();
                                generatorItem.setGeneratorItem();
                            }
                            page.updateInventory(player);
                        }
                    };
                    runnable.runTaskTimer(plugin, 20, 20);
                }
            }
        };
        Button mobDropGeneratorsButton = new Button(mobGeneratorsItem) {
            @Override
            public void run(Player player, ClickType clickType) {
                if (mobDropGeneratorsPage != null) {
                    mobDropGeneratorsPage.openPage(player);
                    BukkitRunnable runnable = new BukkitRunnable() {
                        @Override
                        public void run() {
                            boolean cancel = true;
                            for (Page page : mobDropGeneratorsPage.getAllPages()) {
                                if (page.getInventory().getViewers().contains(player)) {
                                    cancel = false;
                                    break;
                                }
                            }
                            if (cancel) {
                                cancel();
                                return;
                            }
                            Page page = mobDropGeneratorsPage.getPlayersOpenPage(player);
                            for (PageItem pageItem : page.getPageItems().values()) {
                                if (!(pageItem instanceof GeneratorItem)) {
                                    continue;
                                }
                                GeneratorItem generatorItem = (GeneratorItem) pageItem;
                                generatorItem.getInventoryGenerator().generate();
                                generatorItem.setGeneratorItem();
                            }
                            page.updateInventory(player);
                        }
                    };
                    runnable.runTaskTimer(plugin, 20, 20);
                }
            }
        };
        Map<Integer, PageItem> pageItems = new HashMap<>();
        pageItems.put(0, blockGeneratorsButton);
        pageItems.put(1, mobDropGeneratorsButton);
        this.mainPage = new Page(pageItems, null, 27, BLUE + "Generators", InventoryUtil.getFillerItem());
    }

    private void setBlockAndMobGeneratorsPage(Island island, int inventorySize) {
        List<InventoryGenerator> inventoryGenerators = island.getInventoryGenerators();

        Collections.sort(inventoryGenerators);

        Map<Integer, PageItem> blockPageItems = getGeneratorItems(inventoryGenerators, inventorySize, Type.BLOCK);
        Map<Integer, PageItem> mobPageItems = getGeneratorItems(inventoryGenerators, inventorySize, Type.MOB);
        ItemStack nextPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Next Page").get();
        ItemStack previousPageItem = new ItemBuilder(Material.ARROW).setName(BLUE + "Previous Page").get();
        int mainButtonSlot = getMainPageButtonSlot(inventorySize);
        this.blockGeneratorsPage = new Page(blockPageItems, null, inventorySize, BLUE + "Block Generators", true, BLUE + "[Page]", GREEN.toString(),
                AQUA.toString(), nextPageItem, previousPageItem, InventoryUtil.getFillerItem(), mainButtonSlot);
        this.mobDropGeneratorsPage = new Page(mobPageItems, null, inventorySize, BLUE + "Mob Drop Generators", true, BLUE + "[Page]", GREEN.toString(),
                AQUA.toString(), nextPageItem, previousPageItem, InventoryUtil.getFillerItem(), mainButtonSlot);
        for (Page page : blockGeneratorsPage.getAllPages()) {
            page.getPageItems().put(getMainPageButtonSlot(inventorySize), getMainPageButton());
        }
        for (Page page : mobDropGeneratorsPage.getAllPages()) {
            page.getPageItems().put(getMainPageButtonSlot(inventorySize), getMainPageButton());
        }
    }

    private Map<Integer, PageItem> getGeneratorItems(List<InventoryGenerator> inventoryGenerators, int inventorySize, Type type) {
        Map<Integer, PageItem> pageItems = new HashMap<>();
        int i = 0;
        for (InventoryGenerator inventoryGenerator : inventoryGenerators) {
            int mainPageSlot = getMainPageButtonSlot(inventorySize);
            if (i % mainPageSlot == 0 && i >= mainPageSlot) {
                pageItems.put(i, getMainPageButton());
                i++;
            }
            ItemStack generatedItem = inventoryGenerator.getGeneratedItemStack();
            if (generatedItem.getItemMeta() == null || generatedItem.getItemMeta().getLore() == null ||
                    !generatedItem.getItemMeta().getLore().get(1).contains(YELLOW + ChatUtil.capitalize(type.toString()))) {
                continue;
            }
            GeneratorItem generatorItem = new GeneratorItem(generatedItem, inventoryGenerator, plugin);
            pageItems.put(i, generatorItem);
            i++;
        }
        return pageItems;
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
