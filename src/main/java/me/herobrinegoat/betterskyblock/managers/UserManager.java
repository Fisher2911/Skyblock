package me.herobrinegoat.betterskyblock.managers;

import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Type;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.saving.ConnectionPool;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class UserManager {

    private ConnectionPool pool;

    private BetterSkyblock plugin;

    private Map<UUID, User> users;

    private LinkedHashMap<String, Integer> topBalances = new LinkedHashMap<>();

    public UserManager(BetterSkyblock plugin) {
        this.users = new HashMap<>();
        this.plugin = plugin;
        this.pool = plugin.getPool();
    }

    public Map<UUID, User> getUsers() {
        return this.users;
    }

    public User getUser(OfflinePlayer player) {
        return this.users.get(player.getUniqueId());
    }

    public void addUser(User user) {
        if (!users.containsKey(user.getUuid())) this.users.put(user.getUuid(), user);
    }

    public void removeUser(User user) {
        this.users.remove(user.getUuid());
    }

    public User getUserFromDatabase(OfflinePlayer player) {
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        try {
            String playerUUID = player.getUniqueId().toString();
            conn = pool.getConnection();
            statement = conn.prepareStatement(
                    "SELECT * FROM " + pool.getPlayerDataTable() + " WHERE uuid=?");
            statement.setString(1, playerUUID);
            results = statement.executeQuery();
            if (!results.next()) {
                RankManager rankManager = plugin.getRankManager();
                LinkedHashMap<ItemStack, Integer> blockRequirements = new LinkedHashMap<>();
                LinkedHashMap<ItemStack, Integer> mobRequirements = new LinkedHashMap<>();
                blockRequirements.put(rankManager.getItemFromRank(0, Type.BLOCK), 0);
                mobRequirements.put(rankManager.getItemFromRank(0, Type.MOB), 0);
                User user = new User(player, blockRequirements, mobRequirements);
                user.save(pool);
                return user;
            }
            int islandId = results.getInt("island_id");
            String role = results.getString("role");
            double balance = results.getDouble("balance");
            int blockRank = results.getInt("block_rank");
            int mobRank = results.getInt("mob_rank");
            Map<ItemStack, Integer> blocksBroken = getPlayersBrokenBlocksRequirement(playerUUID);
            Map<ItemStack, Integer> mobsKilled = getPlayerMobsKilledRequirement(playerUUID);
            return new User(player.getUniqueId(), User.Role.valueOf(role), player.getName(), balance, islandId, new HashSet<>(), null, blocksBroken, mobsKilled, blockRank, mobRank);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            pool.close(conn, statement, results);
        }
    }

    private Map<ItemStack, Integer> getPlayersBrokenBlocksRequirement(String playerUUID) {
        LinkedHashMap<ItemStack, Integer> blocksBroken = new LinkedHashMap<>();
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getPlayerRequirementsTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE player_id='" + playerUUID + "' AND " +
                    "type='" + "block" + "';");
            results = statement.executeQuery();

            while (results.next()) {
                String itemStackString = results.getString("item");
                int amount = results.getInt("amount");
                ItemStack itemStack = ItemStackSerializer.itemStackFromBase64(itemStackString);
                blocksBroken.put(itemStack, amount);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return blocksBroken;
    }

    private LinkedHashMap<ItemStack, Integer> getPlayerMobsKilledRequirement(String playerUUID) {
        LinkedHashMap<ItemStack, Integer> mobsKilled = new LinkedHashMap<>();
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        String table = pool.getPlayerRequirementsTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + table + " WHERE player_id='" + playerUUID + "' AND " +
                    "type='" + "mob" + "';");
            results = statement.executeQuery();

            while (results.next()) {
                String itemStackString = results.getString("item");
                int amount = results.getInt("amount");
                ItemStack itemStack = ItemStackSerializer.itemStackFromBase64(itemStackString);
                mobsKilled.put(itemStack, amount);
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return mobsKilled;
    }

    public String getNameFromUUID(String uuid) {
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + pool.getPlayerDataTable() + " WHERE uuid=?");
            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                String result = results.getString("name");
                pool.close(conn, statement, results);
                return result;
            }
            pool.close(conn, statement, results);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
        return null;
    }

    public void updatePlayerIsland(String uuid, int islandId, User.Role role) {
        ConnectionPool pool = plugin.getPool();
        String statement = "UPDATE " + pool.getPlayerDataTable() + " SET island_id=?, role=? WHERE uuid=?";
        Object[] objects = {islandId, role.toString(), uuid};
        pool.executeUpdate(statement, objects);
    }

    public String getUUIDFromName(String name) {
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + pool.getPlayerDataTable() + " WHERE name=?");
            statement.setString(1, name);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return results.getString("uuid");
            }
            pool.close(conn, statement, results);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
        return null;
    }

    public double getBalanceFromName(String name) {
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + pool.getPlayerDataTable() + " WHERE name=?");
            statement.setString(1, name);
            ResultSet results = statement.executeQuery();
            if (results.next()) {
                return results.getInt("balance");
            }
            pool.close(conn, statement, results);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
        return -1;
    }

    public void setBalanceFromUUID(String uuid, double money) {
        ConnectionPool pool = plugin.getPool();
        String statement = "UPDATE " + pool.getPlayerDataTable() + " SET balance=? UUID=?";
        Object[] objects = {money, uuid};
        pool.executeUpdate(statement, objects);
    }

    public LinkedHashMap<String, Integer> loadTopTenBalances(int amount) {
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        ResultSet results = null;
        LinkedHashMap<String, Integer> topBalances = new LinkedHashMap<>();

        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("SELECT * FROM " + pool.getPlayerDataTable() + " ORDER BY (balance) DESC LIMIT " + amount + "");
            results = statement.executeQuery();

            while (results.next()) {
                String name = results.getString("name");
                int balance = (int) results.getDouble("balance");
                topBalances.put(name, balance);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, results);
        }
        return topBalances;
    }

    public void loadTopBalances() {
        CompletableFuture<LinkedHashMap<String, Integer>> future = CompletableFuture.supplyAsync(() -> loadTopTenBalances(10)).thenApplyAsync(topTen -> topTen);
        try {
            this.topBalances = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public LinkedHashMap<String, Integer> getTopTenBalances() {
        return topBalances;
    }

}
