package me.herobrinegoat.betterskyblock.saving;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConnectionPool {

    private BetterSkyblock plugin;

    public String getPlayerDataTable() {
        return playerDataTable;
    }

    public String getIslandDataTable() {
        return islandDataTable;
    }

    public String getPlayerRequirementsTable() {
        return playerRequirementsTable;
    }

    public String getResettingIslandLocationsTable() {
        return resettingIslandLocationsTable;
    }

    public String getDeletedIslandLocationsTable() {
        return deletedIslandLocationsTable;
    }

    public String getLastIslandLocationTable() {
        return lastIslandLocationTable;
    }

    public String getInventoryGeneratorDataTable() {
        return inventoryGeneratorDataTable;
    }

    public String getPlaceableGeneratorDataTable() {
        return placeableGeneratorDataTable;
    }

    public String getIslandSettingsTable() {
        return islandSettingsTable;
    }

    private HikariDataSource dataSource;

    public ConnectionPool(BetterSkyblock plugin) {
        this.plugin = plugin;
        init();
        setupPool();
        makeTable(playerDataTable + " (uuid char(36), island_id char(36), role char(20), name char(30), balance float(30), block_rank int, mob_rank int, last_online datetime, PRIMARY KEY (uuid)");
        makeTable(islandDataTable + " (id int, owner_uuid char(36), owner_name char(36), lower_x int, upper_x int, lower_y int, upper_y int, lower_z int, upper_z int, home_x int, home_y int, home_z int, " +
                "warp_x int, warp_y int, warp_z int, generator_id int, date_created datetime, worth int, PRIMARY KEY (id)");
        makeTable(inventoryGeneratorDataTable + " (id int, island_id int, item_generated text, resources_generated int, last_generated_date datetime, level int, " +
                "FOREIGN KEY (island_id) REFERENCES " + islandDataTable + "(id) ON DELETE CASCADE, UNIQUE " +
                "(id,island_id)");
        makeTable(placeableGeneratorDataTable + " (id int, island_id int, item_generated text, resources_generated int, last_generated_date datetime, level int, " +
                "type char(50), stack_amount int, world char(50), x int, y int, z int, generator_type char(50), FOREIGN KEY (island_id) REFERENCES " + islandDataTable + "(id) ON DELETE CASCADE, UNIQUE " +
                "(id,island_id,x,y,z)");
        makeTable(islandSettingsTable + " (island_id int, setting char(100), type char(100), bool boolean, FOREIGN KEY " +
                "(island_id) REFERENCES " + islandDataTable + "(id) ON DELETE CASCADE, UNIQUE (setting, type, island_id)");
        makeTable(playerRequirementsTable + " (player_id char(36), item_id int, item text(65532), amount int, type char(10), PRIMARY KEY (player_id, item_id, type)");
        makeTable(resettingIslandLocationsTable + " (lower_x int, upper_x int, lower_y int, upper_y int,  lower_z int, upper_z int, world char(30), UNIQUE (lower_x,upper_x,lower_y,upper_y,lower_z,upper_z)");
        makeTable(deletedIslandLocationsTable + " (lower_x int, upper_x int, lower_y int, upper_y int,  lower_z int, upper_z int, world char(30), UNIQUE (lower_x,upper_x,lower_y,upper_y,lower_z,upper_z)");
        makeTable(lastIslandLocationTable + " (lower_x int, upper_x int, lower_y int, upper_y int,  lower_z int, upper_z int, world char(30), lastId int");

    }

    private String hostname;
    private String database;
    private String username;
    private String password;

    private String playerDataTable;
    private String inventoryGeneratorDataTable;
    private String placeableGeneratorDataTable;
    private String islandDataTable;
    private String islandSettingsTable;
    private String playerRequirementsTable;
    private String resettingIslandLocationsTable;
    private String deletedIslandLocationsTable;
    private String lastIslandLocationTable;
    private String port;

/*    private int minimumConnections;
    private int maximumConnections;
    private long connectionTimeout;*/

    private void init() {
        File file = FileUtil.setupFile(plugin, "MySQL");
        FileConfiguration fileData = FileUtil.getFileData(file);

        if (file.length() <= 0) {
            fileData.set("hostname", "hostname");
            fileData.set("port", "port");
            fileData.set("database", "database");
            fileData.set("username", "username");
            fileData.set("password", "password");
            FileUtil.saveFileData(fileData, file);
        } else {
            hostname = fileData.getString("hostname");
            port = fileData.getString("port");
            database = fileData.getString("database");
            username = fileData.getString("username");
            password = fileData.getString("password");
        }

        playerDataTable = "player_data";
        inventoryGeneratorDataTable = "inventory_generator_data";
        placeableGeneratorDataTable = "placeable_generator_type_data";
        islandSettingsTable = "island_settings_table";
        islandDataTable = "island_data";
        playerRequirementsTable = "player_requirements";
        resettingIslandLocationsTable = "resetting_island_locations";
        deletedIslandLocationsTable = "deleted_island_locations";
        lastIslandLocationTable = "last_island_location";
    }

    void setupPool() {
        if (dataSource == null) {
            plugin.getLogger().info(ChatColor.RED + "DATASOURCE ENABLED");
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(
                    "jdbc:mysql://" +
                            hostname +
                            ":" +
                            port +
                            "/" +
                            database
            );

            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setUsername(username);
            config.setPassword(password);
            config.setConnectionTimeout(30000);
            config.setLeakDetectionThreshold(30000);

            dataSource = new HikariDataSource(config);
        }
    }

    public void close(Connection conn, PreparedStatement ps, ResultSet res) {
        if (conn != null) try { conn.close(); } catch (SQLException ignored) {}
        if (ps != null) try { ps.close(); } catch (SQLException ignored) {}
        if (res != null) try { res.close(); } catch (SQLException ignored) {}
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            if (dataSource.isClosed()) {
                Bukkit.getConsoleSender().sendMessage("CLOSED");
            } else {
                Bukkit.getConsoleSender().sendMessage("NOT CLOSED");
            }
        } else {
            Bukkit.getConsoleSender().sendMessage("NOT CLOSED");
        }
    }

    private void makeTable(String table) {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = getConnection();
            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + table + ")");
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(conn, ps, null);
        }
    }

    public void executeUpdate(String statementString, Object[] objects) {
        ConnectionPool pool = plugin.getPool();
        Connection conn = null;
        PreparedStatement statement = null;
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement(statementString);

            for (int i = 1; i < objects.length +1; i++) {
                Object object = objects[i -1];

                if (object instanceof Integer) {
                    statement.setInt(i, (int) object);
                } else if (object instanceof String) {
                    statement.setString(i, (String) object);
                } else if (object instanceof Boolean) {
                    statement.setBoolean(i, (boolean) object);
                } else {
                    statement.setObject(i, object);
                }
            }
            statement.executeUpdate();
            pool.close(conn, statement, null);

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            close(conn, statement, null);
        }
    }
}
