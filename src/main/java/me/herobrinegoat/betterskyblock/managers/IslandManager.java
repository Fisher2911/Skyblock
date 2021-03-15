package me.herobrinegoat.betterskyblock.managers;

import me.herobrinegoat.betterskyblock.*;
import me.herobrinegoat.betterskyblock.configs.UpgradesConfig;
import me.herobrinegoat.betterskyblock.generators.*;
import me.herobrinegoat.betterskyblock.inworld.Region;
import me.herobrinegoat.betterskyblock.saving.ConnectionPool;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import me.herobrinegoat.menuapi.Page;
import me.herobrinegoat.menuapi.itemtypes.PageItem;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static me.herobrinegoat.betterskyblock.Island.Setting;
import static me.herobrinegoat.betterskyblock.Island.SettingType;
import static org.bukkit.ChatColor.*;

public class IslandManager {

    ConnectionPool pool;

    private BetterSkyblock plugin;

    private Map<Integer, Island> islands;

    private List<Island> resettingIslands = new ArrayList<>();

    private Set<Region> deletedIslandLocations = new HashSet<>();

    private List<User> deletingIslands = new ArrayList<>();

    private HashMap<User, List<User>> invitedUsers = new HashMap<>();

    private World islandWorld;

    private Page topTenPage;

    private static int id;

    public int getId() {
        return id;
    }

    public void setNextId() {
        id++;
    }

    public IslandManager (BetterSkyblock plugin) {
        this.islands = new HashMap<>();
        this.plugin = plugin;
        this.pool = plugin.getPool();
        this.islandWorld = plugin.getIslandWorld();
    }

    public Map<Integer, Island> getIslands() {
        return this.islands;
    }

    public void addIsland(Island island) {
        this.islands.put(island.getIslandId(), island);
    }

    public void removeIsland(Island island) {
        this.islands.remove(island.getIslandId());
    }

    public Island getIsland(int id) {
        return islands.get(id);
    }

    public void removeDeletedIslandLocation(Region deletedIslandRegion) {
        deletedIslandLocations.remove(deletedIslandRegion);
    }

    public void addDeletedIslandLocation(Region region) {
        deletedIslandLocations.add(region);
    }

    public Set<Region> getDeletedIslandLocations() {
        return deletedIslandLocations;
    }

    public Island getResettingIsland(int index) {
        return resettingIslands.get(index);
    }

    public void removeResettingIsland(Island island) {
        resettingIslands.remove(island);
    }

    public void addResettingIsland(Island island) {
        resettingIslands.add(island);
    }

    public List<User> getInvitedUsers(User owner) {
        if (invitedUsers.get(owner) == null) {
            List<User> users = new ArrayList<>();
            invitedUsers.put(owner, users);
        }
        return invitedUsers.get(owner);
    }

    public List<Island> getResettingIslands() {
        return resettingIslands;
    }

    public void saveIslandLocations() {
        saveLastIslandLocation(plugin.getPreviousIslandLocation());
        saveResettingIslandLocations(resettingIslands);
        saveDeletedIslandLocations(deletedIslandLocations);
    }

    private boolean lastIslandLocationExists() {
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet results = null;
        String table = pool.getLastIslandLocationTable();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table);
            results = statement.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return false;
    }

    public void saveLastIslandLocation(Region region) {
        PreparedStatement statement = null;
        Connection conn = null;
        String table = pool.getLastIslandLocationTable();
        if (region == null) {
            return;
        }
        try {
            conn = pool.getConnection();
            boolean lastIslandLocationExists = lastIslandLocationExists();
            if (!lastIslandLocationExists) {
                statement = conn.prepareStatement("INSERT INTO " + table + " (lower_x,upper_x,lower_y,upper_y,lower_z,upper_z," +
                        "world,lastId) values (?,?,?,?,?,?,?,?)");
            } else {
                statement = conn.prepareStatement("UPDATE " + table + " SET lower_x=?, upper_x=?, lower_y=?, upper_y=?, lower_z=?, upper_z=?, world=?, lastId=?");
            }
            String worldName = plugin.getIslandWorld().getName();
            if (region.getWorld() != null) {
                worldName = region.getWorld().getName();
            }
            statement.setInt(1, region.getLowerX());
            statement.setInt(2, region.getUpperX());
            statement.setInt(3, region.getLowerY());
            statement.setInt(4, region.getUpperY());
            statement.setInt(5, region.getLowerZ());
            statement.setInt(6, region.getUpperZ());
            statement.setString(7, worldName);
            statement.setInt(8, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public void saveResettingIslandLocations(List<Island> islands) {
        PreparedStatement statement = null;
        Connection conn = null;
        String table = pool.getResettingIslandLocationsTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + "(lower_x,upper_x,lower_y,upper_y,lower_z,upper_z," +
                    "world) values (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE " +
                    "lower_x=?");
            for (Island island : islands) {
                Region region = island.getRegion();
                String worldName = plugin.getIslandWorld().getName();
                if (region.getWorld() != null) {
                    worldName = region.getWorld().getName();
                }
                statement.setInt(1, region.getLowerX());
                statement.setInt(2, region.getUpperX());
                statement.setInt(3, region.getLowerY());
                statement.setInt(4, region.getUpperY());
                statement.setInt(5, region.getLowerZ());
                statement.setInt(6, region.getUpperZ());
                statement.setString(7, worldName);
                statement.setInt(8, region.getLowerX());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public void saveDeletedIslandLocations(Set<Region> regions) {
        PreparedStatement statement = null;
        Connection conn = null;
        String table = pool.getDeletedIslandLocationsTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + "(lower_x,upper_x,lower_y,upper_y,lower_z,upper_z,world) values (" +
                    "?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE lower_x=?");
            for (Region region : regions) {
                String worldName = plugin.getIslandWorld().getName();
                if (region.getWorld() != null) {
                    worldName = region.getWorld().getName();
                }
                statement.setInt(1, region.getLowerX());
                statement.setInt(2, region.getUpperX());
                statement.setInt(3, region.getLowerY());
                statement.setInt(4, region.getUpperY());
                statement.setInt(5, region.getLowerZ());
                statement.setInt(6, region.getUpperZ());
                statement.setString(7, worldName);
                statement.setInt(8, region.getLowerX());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public void removeIslandLocation(Region region, String table) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("DELETE FROM " + table + " WHERE lower_x=? AND upper_x=? AND lower_y=? AND upper_y=? AND " +
                    "lower_z=? AND upper_z=?");
            statement.setInt(1, region.getLowerX());
            statement.setInt(2, region.getUpperX());
            statement.setInt(3, region.getLowerY());
            statement.setInt(4, region.getUpperY());
            statement.setInt(5, region.getLowerZ());
            statement.setInt(6, region.getUpperZ());
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public void loadIslandLocations() {
        loadLastIslandLocation();
        loadDeletedIslandLocations();
        loadResettingIslandLocations();
    }

    private void loadDeletedIslandLocations() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getDeletedIslandLocationsTable();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table);
            results = statement.executeQuery();
            while (results.next()) {
                int lowerX = results.getInt("lower_x");
                int upperX = results.getInt("upper_x");
                int lowerY = results.getInt("lower_y");
                int upperY = results.getInt("upper_y");
                int lowerZ = results.getInt("lower_z");
                int upperZ = results.getInt("upper_z");
                String world = results.getString("world");
                Region region = new Region(Bukkit.getWorld(world), lowerX, upperX, lowerY, upperY, lowerZ, upperZ);
                deletedIslandLocations.add(region);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
    }

    private void loadResettingIslandLocations() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getResettingIslandLocationsTable();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table);
            results = statement.executeQuery();
            while (results.next()) {
                int lowerX = results.getInt("lower_x");
                int upperX = results.getInt("upper_x");
                int lowerY = results.getInt("lower_y");
                int upperY = results.getInt("upper_y");
                int lowerZ = results.getInt("lower_z");
                int upperZ = results.getInt("upper_z");
                String world = results.getString("world");
                Region region = new Region(Bukkit.getWorld(world), lowerX, upperX, lowerY, upperY, lowerZ, upperZ);
                Island island = new Island(region);
                resettingIslands.add(island);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
    }

    private void loadLastIslandLocation() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getLastIslandLocationTable();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table);
            results = statement.executeQuery();
            if (!results.next()) {
                plugin.setPreviousIslandLocation(null);
                return;
            }
            int lowerX = results.getInt("lower_x");
            int upperX = results.getInt("upper_x");
            int lowerY = results.getInt("lower_y");
            int upperY = results.getInt("upper_y");
            int lowerZ = results.getInt("lower_z");
            int upperZ = results.getInt("upper_z");
            int lastId = results.getInt("lastId");
            String world = results.getString("world");
            Region region = new Region(Bukkit.getWorld(world), lowerX, upperX, lowerY, upperY, lowerZ, upperZ);
            plugin.setPreviousIslandLocation(region);
            id = lastId;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
    }


    public List<Island> getTopTenIslands() {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + pool.getIslandDataTable() + " ORDER BY (worth) DESC LIMIT 10");
            results = statement.executeQuery();
            LinkedList<Island> topIslandsList = new LinkedList<>();
            while (results.next()) {
                int id = results.getInt("id");
                Island island = getIslandFromDB(id);
                if (island.getValue() == 0) {
                    continue;
                }
                topIslandsList.add(island);
            }
            return topIslandsList;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return new LinkedList<>();
    }

    public Island getIslandFromLocation(Location location) {
        return getIslandFromLocation(location, false);
    }

    public Island getIslandFromLocation(Location location, boolean includeY) {
        for (Island island : islands.values()) {
            Region region = island.getRegion();

            if (region.inRegion(location, false, includeY)) {
                return island;
            }
        }
        return null;
    }

    public Island getIslandFromDB(int id) {
        PreparedStatement statement = null;
        Connection conn = null;
        ResultSet results = null;
        String table = pool.getIslandDataTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE id='" + id + "';");
            results = statement.executeQuery();

            if (!results.next()) {
                return null;
            }
            String ownerUUID = results.getString("owner_uuid");
            int lowerX = results.getInt("lower_x");
            int upperX = results.getInt("upper_x");
            int lowerY = results.getInt("lower_y");
            int upperY = results.getInt("upper_y");
            int lowerZ = results.getInt("lower_z");
            int upperZ = results.getInt("upper_z");
            int homeX = results.getInt("home_x");
            int homeY = results.getInt("home_y");
            int homeZ = results.getInt("home_z");
            int warpX = results.getInt("warp_x");
            int warpY = results.getInt("warp_y");
            int warpZ = results.getInt("warp_z");
            int generatorId = results.getInt("generator_id");
            int worth = results.getInt("worth");
            Region region = new Region(islandWorld, lowerX, upperX, lowerY, upperY, lowerZ, upperZ);
            List<SettingValue> settings = getIslandSettings(id);
            Set<InventoryGenerator> inventoryGenerators = getInventoryGenerators(id);
            Set<PlaceableGenerator> placeableGenerators = getPlaceableGenerators(id);
            Set<UUID> members = getMemberUUIDS(id);

            Set<Generator> generators = new HashSet<>();
            generators.addAll(placeableGenerators);
            generators.addAll(inventoryGenerators);

            Location home = new Location(islandWorld, homeX, homeY, homeZ);
            Location warp = new Location(islandWorld, warpX, warpY, warpZ);

            String ownerName = plugin.getUserManager().getNameFromUUID(ownerUUID);
            Island island = new Island(id, members, region, generators, generatorId, ownerUUID, ownerName, home, warp, settings);
            island.setValue(worth);
            return island;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return null;
    }

    private List<SettingValue> getIslandSettings(int islandId) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getIslandSettingsTable();
        List<SettingValue> settingValues = new ArrayList<>();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE island_id='" + islandId + "';");
            results = statement.executeQuery();

            while (results.next()) {
                Setting setting = Setting.valueOf(results.getString("setting"));
                SettingType settingType = SettingType.valueOf(results.getString("type"));
                boolean value = results.getBoolean("bool");
                SettingValue settingValue = new SettingValue(setting, settingType, value);
                settingValues.add(settingValue);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return settingValues;
    }

    private Set<InventoryGenerator> getInventoryGenerators(int islandId) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getInventoryGeneratorDataTable();

        Set<InventoryGenerator> inventoryGenerators = new HashSet<>();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE island_id='" + islandId + "';");
            results = statement.executeQuery();

            while (results.next()) {
                int id = results.getInt("id");
                ItemStack item = ItemStackSerializer.itemStackFromBase64(results.getString("item_generated"));
                int resourcesGenerated = results.getInt("resources_generated");
                LocalDateTime lastGeneratedDate = results.getObject("last_generated_date", LocalDateTime.class);
                int level = results.getInt("level");
                RankManager rankManager = plugin.getRankManager();
                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                Upgrades upgrades = upgradesConfig.getUpgrades(item, Type.INVENTORY);
                InventoryGenerator inventoryGenerator = new InventoryGenerator(id, item, resourcesGenerated, lastGeneratedDate, upgrades, -1, level);
                inventoryGenerators.add(inventoryGenerator);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return inventoryGenerators;
    }

    private Set<PlaceableGenerator> getPlaceableGenerators(int islandId) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getPlaceableGeneratorDataTable();

        Set<PlaceableGenerator> placeableGenerators = new HashSet<>();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE island_id='" + islandId + "'");
            results = statement.executeQuery();

            while (results.next()) {
                int id = results.getInt("id");
                ItemStack item = ItemStackSerializer.itemStackFromBase64(results.getString("item_generated"));
                int resourcesGenerated = results.getInt("resources_generated");
                LocalDateTime lastGeneratedDate = results.getObject("last_generated_date", LocalDateTime.class);
                int level = results.getInt("level");
                String type = results.getString("type");
                int stackAmount = results.getInt("stack_amount");
                String world = results.getString("world");
                int x = results.getInt("x");
                int y = results.getInt("y");
                int z = results.getInt("z");
                String generatorType = results.getString("generator_type");
                PlaceableGenerator placeableGenerator = null;
                Location location = new Location(Bukkit.getWorld(world), x, y, z);
                RankManager rankManager = plugin.getRankManager();
                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                Upgrades upgrades;
                int rank;
                if (generatorType.equalsIgnoreCase(MobGenerator.class.getSimpleName())) {
                    upgrades = upgradesConfig.getUpgrades(item, Type.MOB);
                    rank = rankManager.getItemRank(item, Type.MOB);
                    EntityType entityType = EntityType.valueOf(type);
                    placeableGenerator = new MobGenerator(id, item, resourcesGenerated, lastGeneratedDate, upgrades, rank, level, entityType, location, stackAmount);
                } else if (generatorType.equalsIgnoreCase(BlockGenerator.class.getSimpleName())) {
                    Material material = Material.valueOf(type);
                    upgrades = upgradesConfig.getUpgrades(item, Type.BLOCK);
                    rank = rankManager.getItemRank(item, Type.BLOCK);
                    placeableGenerator = new BlockGenerator(id, item, resourcesGenerated, lastGeneratedDate, upgrades, rank, level, material, location, stackAmount);
                }
                if (placeableGenerator != null) {
                    placeableGenerators.add(placeableGenerator);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return placeableGenerators;
    }

    private Set<UUID> getMemberUUIDS(int id) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getPlayerDataTable();

        Set<UUID> members = new HashSet<>();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE island_id='" + id + "';");
            results = statement.executeQuery();

            while (results.next()) {
                UUID uuid = UUID.fromString(results.getString("uuid"));
                members.add(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return members;
    }

    public int getIslandIdFromUUID(String uuid) {
        for (Map.Entry<Integer, Island> entry : islands.entrySet()) {
            Island island = entry.getValue();
            int id = entry.getKey();

            if (island.getOwnerUUID().equals(uuid)) {
                return id;
            }
        }
        return -1;
    }

    public void setTopTenInventory() {
        CompletableFuture.supplyAsync(this::getTopTenIslands).thenAccept(topIslands -> {
            Inventory inventory = Bukkit.createInventory(null, 36, BLUE + "Top Ten Islands");
            int i = 0;
            Map<Integer, PageItem> pageItemMap = new HashMap<>();
            for (Island island : topIslands) {
                if (i == 10) break;
                if (island == null) {
                    continue;
                }
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(UUID.fromString(island.getOwnerUUID()));
                int slot = 0;
                switch (i) {
                    case 0:
                        slot = 4;
                        break;
                    case 1:
                        slot = 12;
                        break;
                    case 2:
                        slot = 14;
                        break;
                    case 3:
                        slot = 20;
                        break;
                    case 4:
                        slot = 22;
                        break;
                    case 5:
                        slot = 24;
                        break;
                    case 6:
                        slot = 28;
                        break;
                    case 7:
                        slot = 30;
                        break;
                    case 8:
                        slot = 32;
                        break;
                    case 9:
                        slot = 34;
                        break;
                }
                ItemStack displayItemStack = new ItemStack(Material.PLAYER_HEAD);
                ItemMeta im = displayItemStack.getItemMeta();
                if (im != null) {
                    im.setDisplayName(GOLD + "#" + (i + 1) + " - " + AQUA + offlinePlayer.getName() + "'s " + GREEN + "Island");
                    List<String> lore = new ArrayList<>();
                    lore.add("");
                    lore.add(AQUA + "Value: " + GREEN + "$" + island.getValue());
                    im.setLore(lore);
                    if (im instanceof SkullMeta) {
                        ((SkullMeta) im).setOwningPlayer(offlinePlayer);
                    }
                    displayItemStack.setItemMeta(im);
                }
                PageItem pageItem = new PageItem(displayItemStack);
                pageItemMap.put(slot, pageItem);
                i++;
            }
            this.topTenPage = new Page(inventory, pageItemMap);
        });


    }

    public Page getTopTenPage() {
        return topTenPage;
    }

}
