package me.herobrinegoat.betterskyblock;


import me.herobrinegoat.betterskyblock.commands.*;
import me.herobrinegoat.betterskyblock.configs.ExchangeItemsConfig;
import me.herobrinegoat.betterskyblock.configs.UpgradesConfig;
import me.herobrinegoat.betterskyblock.events.*;
import me.herobrinegoat.betterskyblock.hooks.EconomySetup;
import me.herobrinegoat.betterskyblock.hooks.VaultHook;
import me.herobrinegoat.betterskyblock.inventories.ExchangeMenu;
import me.herobrinegoat.betterskyblock.inworld.Region;
import me.herobrinegoat.betterskyblock.managers.IslandManager;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.managers.UserManager;
import me.herobrinegoat.betterskyblock.mobstacker.CreatureDeath;
import me.herobrinegoat.betterskyblock.mobstacker.MobSpawning;
import me.herobrinegoat.betterskyblock.nms.NMS;
import me.herobrinegoat.betterskyblock.saving.ConnectionPool;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class BetterSkyblock extends JavaPlugin {

    private ConnectionPool pool;

    private IslandManager islandManager;

    private UserManager userManager;

    private RankManager rankManager;

    private UpgradesConfig upgradesConfig;

    private ExchangeItemsConfig exchangeItemsConfig;

    private NMS nms;

    private Region previousIslandLocation;

    private EconomySetup economySetup;

    private VaultHook vaultHook;

    private ExchangeMenu exchangeMenu;

    private BukkitRunnable timer;

    private BetterSkyblock instance;

    @Override
    public void onEnable() {
        instance = this;
        instanceClasses();
        registerCommands();
        registerEvents();
        vaultHook.hook();

        try {
            nms = (NMS) Class.forName("me.herobrinegoat.betterskyblock.nms." + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]).newInstance();
        } catch (ClassNotFoundException e) {
            //Unsupported Version
            getLogger().info("Unsupported Version Detected: " + Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]);
            getLogger().info("Try updating from spigot");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        loadData();
        final long[] timePassed = {-1};
        timer = new BukkitRunnable() {
            @Override
            public void run() {
                timePassed[0]++;
                if (timePassed[0] == 55) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        User user = userManager.getUser(player);
                        if (user != null) {
                            continue;
                        }
                        user = userManager.getUserFromDatabase(player);
                        userManager.addUser(user);
                        if (!user.hasIsland()) continue;
                        Island island = user.getIsland(instance);
                        if (island != null) continue;
                        island = islandManager.getIslandFromDB(user.getIslandId());
                        if (island == null) continue;
                        islandManager.addIsland(island);
                    }
                }

                if (timePassed[0] % 100 == 0 && timePassed[0] >=100) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                         User user = userManager.getUser(player);
                        if (user != null) {
                            user.updateScoreBoard(instance);
                        }
                    }
                }

                if ((timePassed[0] % 400 == 0 && timePassed[0] >= 400) || timePassed[0] == 100) {
                    resetIslands();
                }

                if (timePassed[0] >= 3000 && timePassed[0] % 3000 == 0) {
                    saveData();
                    islandManager.setTopTenInventory();
                    userManager.loadTopBalances();
                }
            }
        };
        timer.runTaskTimerAsynchronously(this, 0, 1);
    }

    @Override
    public void onDisable() {
        timer.cancel();
        vaultHook.unHook();
        saveData();
        pool.closePool();
    }

    public void instanceClasses() {
        pool = new ConnectionPool(this);
        vaultHook = new VaultHook(this);
        economySetup = new EconomySetup(this);
        islandManager = new IslandManager(this);
        userManager = new UserManager(this);
        rankManager = new RankManager(this);
        upgradesConfig = new UpgradesConfig(this);
        exchangeItemsConfig = new ExchangeItemsConfig(this);
        exchangeMenu = new ExchangeMenu(this);
    }

    public void registerEvents() {
        this.getServer().getPluginManager().registerEvents(new PlayerJoin(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerLeave(this), this);
        this.getServer().getPluginManager().registerEvents(new IslandProtection(this), this);
        this.getServer().getPluginManager().registerEvents(new GeneratorEvents(this), this);
        this.getServer().getPluginManager().registerEvents(new MobSpawning(this), this);
        this.getServer().getPluginManager().registerEvents(new CreatureDeath(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerDeath(this), this);
    }

    public void registerCommands() {
        getCommand("island").setExecutor(new IslandCommand(this));
        getCommand("generators").setExecutor(new GeneratorCommand(this));
        getCommand("pay").setExecutor(new PayCommand(this));
        getCommand("balance").setExecutor(new EconomyCommand(this));
        getCommand("exchange").setExecutor(new ExchangeCommand(this));
        getCommand("help").setExecutor(new HelpCommand(this));
        getCommand("spawn").setExecutor(new SpawnCommand(this));
        getCommand("tutorial").setExecutor(new TutorialCommand(this));
        getCommand("setrank").setExecutor(new RankCommand(this));
    }

    public void saveData() {
        for (User user : userManager.getUsers().values()) {
            if (user == null) {
                continue;
            }
            user.save(pool);
        }
        for (Island island : islandManager.getIslands().values()) {
            if (island == null) {
                continue;
            }
            island.save(pool);
        }
        islandManager.saveIslandLocations();
    }

    public void loadData() {
        islandManager.loadIslandLocations();
        userManager.loadTopBalances();
        upgradesConfig.loadUpgrades();
        rankManager.loadRanks();
        exchangeItemsConfig.loadExchangeItems();
        islandManager.setTopTenInventory();
    }

    public ConnectionPool getPool() {
        return pool;
    }

    public World getIslandWorld() {
        WorldCreator wc = new WorldCreator("DefaultIslandWorld");
        wc.generator("VoidGenerator:PLAINS");
        return wc.createWorld();
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public RankManager getRankManager() {
        return rankManager;
    }

    public UpgradesConfig getUpgradesConfig() {
        return upgradesConfig;
    }

    public ExchangeItemsConfig getExchangeItemsConfig() {
        return exchangeItemsConfig;
    }

    public NMS getNms() {
        return nms;
    }

    public EconomySetup getEconomySetup() {
        return economySetup;
    }

    public VaultHook getVaultHook() {
        return vaultHook;
    }

    public ExchangeMenu getExchangeMenu() {
        return exchangeMenu;
    }

    public void resetIslands() {
        boolean cycle = true;
        while (cycle) {
            if (islandManager.getResettingIslands().size() < 1) {
                break;
            }
            Island island = islandManager.getResettingIsland(0);
            if (!island.isDeleting() && !island.isDoneDeleting()) {
                getLogger().info("Starting reset of island at " + island.getCenter().toString());
                island.removeBlocks(this);
                island.setDeleting(true);
            } else if (island.isDoneDeleting()) {
                getLogger().info("Reset of island at " + island.getCenter().toString() + " complete");
                islandManager.addDeletedIslandLocation(island.getRegion());
                islandManager.removeResettingIsland(island);
                Bukkit.getScheduler().runTaskAsynchronously(this, () -> islandManager.removeIslandLocation(island.getRegion(), pool.getResettingIslandLocationsTable()));
                getLogger().info("Deleted Islands - " + islandManager.getDeletedIslandLocations());
                continue;
            }
            cycle = false;
        }
    }

    public Region getNextIslandLocation() {
        Set<Region> deletedIslandLocations = islandManager.getDeletedIslandLocations();
        if (!deletedIslandLocations.isEmpty()) {
            Region reg = null;
            for (Region region : deletedIslandLocations) {
                reg = region;
                break;
            }
            islandManager.removeDeletedIslandLocation(reg);
            Region finalReg = reg;
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> islandManager.removeIslandLocation(finalReg, pool.getDeletedIslandLocationsTable()));
            return reg.clone();
        }

        if (previousIslandLocation == null) {
            World defaultWorld = getIslandWorld();
            Region previousIslandLocation = new Region(defaultWorld, -150, 150, 0, 256, -150, 150);
            this.previousIslandLocation = previousIslandLocation.clone();
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> islandManager.saveLastIslandLocation(previousIslandLocation));
            return this.previousIslandLocation.clone();
        }

        World defaultWorld = getIslandWorld();
        if (previousIslandLocation.getWorld() == null) {
            previousIslandLocation.setWorld(defaultWorld);
        }
        double distance = 500;
        int x = previousIslandLocation.getCenter().getBlockX();
        int z = previousIslandLocation.getCenter().getBlockZ();
        if (x < z) {
            if (-1 * x < z) {
                x += distance;
            } else {
                z += distance;
            }
        } else if (x > z) {
            if (-1 * x >= z) {
                x -= distance;
            } else {
                z -= distance;
            }
        } else { // x == z
            if (x <= 0) {
                z += distance;
            } else {
                z -= distance;
            }
        }
        previousIslandLocation.setLowerX(x - 150);
        previousIslandLocation.setUpperX(x + 150);
        previousIslandLocation.setLowerY(0);
        previousIslandLocation.setUpperY(256);
        previousIslandLocation.setLowerZ(z - 150);
        previousIslandLocation.setUpperZ(z + 150);
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> islandManager.saveLastIslandLocation(previousIslandLocation));
        return previousIslandLocation.clone();
    }

    public Region getPreviousIslandLocation() {
        return previousIslandLocation;
    }

    public void setPreviousIslandLocation(Region previousIslandLocation) {
        this.previousIslandLocation = previousIslandLocation;
    }
}
