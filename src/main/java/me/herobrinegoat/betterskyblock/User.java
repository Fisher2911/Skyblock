package me.herobrinegoat.betterskyblock;

import me.herobrinegoat.betterskyblock.configs.UpgradesConfig;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.saving.ConnectionPool;
import me.herobrinegoat.betterskyblock.saving.Savable;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.ChatColor.*;

public class User implements Savable {

    private UUID uuid;

    private String playerName;

    private double balance;

    private int islandId;

    private Role role;

    private Set<Integer> islandInvites;

    private LocalDateTime lastIslandCreatedTime;

    private LocalDateTime dateJoined;

    private LocalDateTime lastOnline;

    private int blockRank;

    private int mobRank;

    private Map<ItemStack, Integer> blockRequirements;

    private Map<ItemStack, Integer> mobRequirements;

    private boolean teleporting;

    public User (OfflinePlayer player, Map<ItemStack, Integer> blockRequirements, Map<ItemStack, Integer> mobRequirements) {
        this.uuid = player.getUniqueId();
        this.playerName = player.getName();
        this.balance = 100;
        this.islandId = -1;
        this.role = Role.NO_ISLAND;
        this.islandInvites = new HashSet<>();
        this.lastIslandCreatedTime = null;
        this.blockRank = 0;
        this.mobRank = 0;
        this.blockRequirements = blockRequirements;
        this.mobRequirements = mobRequirements;
    }

    public User(UUID uuid, Role role, String playerName, double balance, int islandId, Set<Integer> islandInvites, LocalDateTime lastIslandCreatedTime,
                Map<ItemStack, Integer> blockRequirements, Map<ItemStack, Integer> mobRequirements, int blockRank, int mobRank) {
        this.uuid = uuid;
        this.role = role;
        this.playerName = playerName;
        this.balance = balance;
        this.islandId = islandId;
        this.islandInvites = islandInvites;
        this.lastIslandCreatedTime = lastIslandCreatedTime;
        this.blockRequirements = blockRequirements;
        this.mobRequirements = mobRequirements;
        this.blockRank = blockRank;
        this.mobRank = mobRank;
    }

    public enum Role {

        OWNER,

        MEMBER,

        NO_ISLAND;

    }

    public LocalDateTime getDateJoined() {
        return dateJoined;
    }

    public void setDateJoined(LocalDateTime dateJoined) {
        this.dateJoined = dateJoined;
    }

    public LocalDateTime getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(LocalDateTime lastOnline) {
        this.lastOnline = lastOnline;
    }

    public int getBlockRank() {
        return blockRank;
    }

    public void setBlockRank(int blockRank) {
        this.blockRank = blockRank;
    }

    public int getMobRank() {
        return mobRank;
    }

    public void setMobRank(int mobRank) {
        this.mobRank = mobRank;
    }

    public Map<ItemStack, Integer> getBlockRequirements() {
        return blockRequirements;
    }

    public Map<ItemStack, Integer> getMobRequirements() {
        return mobRequirements;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public int getIslandId() {
        return islandId;
    }

    public void setIslandId(int islandId) {
        this.islandId = islandId;
    }

    public boolean hasIsland() {
        return islandId >= 0;
    }

    public Island getIsland(BetterSkyblock plugin) {
        return plugin.getIslandManager().getIsland(islandId);
    }

    public boolean isOnline() {
        return Bukkit.getPlayer(uuid) != null;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isTeleporting() {
        return teleporting;
    }

    public void setTeleporting(boolean teleporting) {
        this.teleporting = teleporting;
    }

    public String getPlayerUUIDAsString() {
        return this.uuid.toString();
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void addBlockItemToRequirements(ItemStack item) {
        addBlockItemToRequirements(item, 1);
    }

    public void addMobItemToRequirements(ItemStack item) {
        addMobItemToRequirements(item, 1);
    }

    public void addBlockItemToRequirements(ItemStack item, int amount) {
        int currentAmount = 0;
        if (this.blockRequirements.containsKey(item)) currentAmount = this.blockRequirements.get(item);
        this.blockRequirements.put(item, currentAmount + amount);
    }

    public void addMobItemToRequirements(ItemStack item, int amount) {
        int currentAmount = 0;
        if (this.mobRequirements.containsKey(item)) currentAmount = this.mobRequirements.get(item);
        this.mobRequirements.put(item, currentAmount + amount);
    }

    public boolean hasPermission(Island island, String permission, BetterSkyblock plugin) {
        if (island == null) {
            return false;
        }
        int index;
        SettingValue settingValue = new SettingValue(Island.Setting.valueOf(permission), Island.SettingType.VISITOR, true);
        if (!hasIsland() || !island.equals(getIsland(plugin))) {
            index = island.getSettings().indexOf(settingValue);
            if (index < 0 || index > island.getSettings().size() + 1) {
                return false;
            }
            return island.getSettings().get(index).getValue();
        }
        if (island == getIsland(plugin)) {
            if (role == Role.OWNER) {
                return true;
            }
            for (SettingValue s : island.getSettings()) {
                if (s.getSettingType().toString().equalsIgnoreCase(role.toString())) {
                    if (s.getSetting().toString().equals(permission)) {
                        settingValue = new SettingValue(Island.Setting.valueOf(permission), Island.SettingType.valueOf(role.toString()), true);
                        index = island.getSettings().indexOf(settingValue);
                        if (index < 0 || index > island.getSettings().size() + 1) {
                            return false;
                        }
                        return island.getSettings().get(index).getValue();
                    }
                } else if (s.getSettingType() == Island.SettingType.ISLAND) {
                    settingValue = new SettingValue(Island.Setting.valueOf(permission), Island.SettingType.ISLAND, true);
                    index = island.getSettings().indexOf(settingValue);
                    if (index < 0 || index > island.getSettings().size() + 1) {
                        return false;
                    }
                    return island.getSettings().get(index).getValue();
                }
            }
        }
        return false;
    }

    public void removeFromIsland(BetterSkyblock plugin) {
        Island island = plugin.getIslandManager().getIsland(islandId);
        island.removeMember(uuid.toString());
        this.islandId = -1;
        this.role = Role.NO_ISLAND;
    }

    public void setLastIslandCreatedTime(LocalDateTime lastIslandCreatedTime) {
        this.lastIslandCreatedTime = lastIslandCreatedTime;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isOwner() {
        return role == Role.OWNER;
    }

    @Override
    public void save(ConnectionPool pool) {
       saveUser(pool);
       savePlayerItemRequirements(pool);
    }

    private void saveUser(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;

        try {
            conn = pool.getConnection();

            statement = conn.prepareStatement("INSERT INTO " + pool.getPlayerDataTable() + " (uuid,island_id,role,name,balance,block_rank,mob_rank,last_online) VALUE " +
                    "(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE island_id=?, role=?, name=?, balance=?, block_rank=?, mob_rank=?, last_online=?");
            statement.setString(1, uuid.toString());
            statement.setInt(2, islandId);
            statement.setString(3, role.toString());
            statement.setString(4, playerName);
            statement.setDouble(5, balance);
            statement.setInt(6, blockRank);
            statement.setInt(7, mobRank);
            statement.setObject(8, lastOnline);
            statement.setInt(9, islandId);
            statement.setString(10, role.toString());
            statement.setString(11, playerName);
            statement.setDouble(12, balance);
            statement.setInt(13, blockRank);
            statement.setInt(14, mobRank);
            statement.setObject(15, lastOnline);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
        savePlayerItemRequirements(pool);
    }

    private void savePlayerItemRequirements(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getPlayerRequirementsTable();
        Map<ItemStack, Integer> blocksBroken = getBlockRequirements();
        Map<ItemStack, Integer> mobsKilled = getMobRequirements();
        String playerUUID = uuid.toString();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + " (player_id,item_id,item,amount,type) VALUE " +
                    "(?,?,?,?,?) ON DUPLICATE KEY UPDATE item=?, amount=?");
            int itemId = 0;
            for (Map.Entry<ItemStack, Integer> entry : blocksBroken.entrySet()) {
                ItemStack item = entry.getKey();
                int amountBroken = entry.getValue();
                String itemString = ItemStackSerializer.itemStackToBase64(item);
                statement.setString(1, playerUUID);
                statement.setInt(2, itemId);
                statement.setString(3, itemString);
                statement.setInt(4, amountBroken);
                statement.setString(5, "block");
                statement.setString(6, itemString);
                statement.setInt(7, amountBroken);
                itemId++;
                statement.addBatch();
            }
            statement.executeBatch();

            statement.clearBatch();
            for (Map.Entry<ItemStack, Integer> entry : mobsKilled.entrySet()) {
                ItemStack item = entry.getKey();
                int amountBroken = entry.getValue();
                String itemString = ItemStackSerializer.itemStackToBase64(item);
                statement.setString(1, playerUUID);
                statement.setInt(2, itemId);
                statement.setString(3, itemString);
                statement.setInt(4, amountBroken);
                statement.setString(5, "mob");
                statement.setString(6, itemString);
                statement.setInt(7, amountBroken);
                itemId++;
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    @Override
    public void delete(ConnectionPool pool) {

    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Role getRole() {
        return role;
    }

    public Set<Integer> getIslandInvites() {
        return islandInvites;
    }

    public LocalDateTime getLastIslandCreatedTime() {
        return lastIslandCreatedTime;
    }

    public void setScoreBoard(BetterSkyblock plugin) {
        BukkitRunnable sync = new BukkitRunnable() {
            @Override
            public void run() {
                ScoreboardManager manager = Bukkit.getScoreboardManager();
                if (manager == null) {
                    cancel();
                    return;
                }
                Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
                final Objective obj = board.registerNewObjective(GOLD + BOLD.toString() + "Skyblock", "dummy", AQUA.toString() + AQUA.toString() + "Skyblock");
                if (!isOnline()) {
                    cancel();
                    return;
                }
                Player player = getPlayer();

                obj.setDisplaySlot(DisplaySlot.SIDEBAR);
                final Team blankSpace = board.registerNewTeam("Blank");
                final Team blankSpace2 = board.registerNewTeam("Blank2");
                final Team blankSpace3 = board.registerNewTeam("Blank3");
                final Team islandOwner = board.registerNewTeam("Island Owner");
                final Team userBalanceAmount = board.registerNewTeam("Balance Amount");
                final Team blocksBroken = board.registerNewTeam("Blocks");
                final Team blocksBrokenAmount = board.registerNewTeam("Blocks Amount");
                final Team mobsKilled = board.registerNewTeam("Mobs");
                final Team mobsKilledAmount = board.registerNewTeam("Mobs Amount");
                final Team fillerLine = board.registerNewTeam("Filler Line");
                islandOwner.addEntry(BLUE.toString());
                userBalanceAmount.addEntry(DARK_GREEN.toString());

                blankSpace2.addEntry(LIGHT_PURPLE.toString());

                blocksBroken.addEntry(BLUE + BLUE.toString());
                blocksBrokenAmount.addEntry(AQUA + AQUA.toString());

                blankSpace3.addEntry(LIGHT_PURPLE + LIGHT_PURPLE.toString());

                mobsKilled.addEntry(GREEN + GREEN.toString());
                mobsKilledAmount.addEntry(GOLD + GOLD.toString());

                fillerLine.addEntry(BOLD.toString());
                fillerLine.addEntry(BLACK.toString());
                blankSpace.addEntry(GREEN.toString());

                obj.getScore(AQUA.toString()).setScore(15);
                obj.getScore(BOLD.toString()).setScore(14);
                obj.getScore(BLUE.toString()).setScore(13);
                obj.getScore(GREEN.toString()).setScore(12);
                obj.getScore(DARK_GREEN.toString()).setScore(11);
                obj.getScore(LIGHT_PURPLE.toString()).setScore(10);
                obj.getScore(BLUE + BLUE.toString()).setScore(9);
                obj.getScore(AQUA + AQUA.toString()).setScore(8);
                obj.getScore(LIGHT_PURPLE + LIGHT_PURPLE.toString()).setScore(7);
                obj.getScore(GREEN + GREEN.toString()).setScore(6);
                obj.getScore(GOLD + GOLD.toString()).setScore(5);
                obj.getScore(BLACK.toString()).setScore(4);

                StringBuilder fillLine = new StringBuilder(GOLD.toString());
                for (int i = 32; i > 0; i--) {
                    fillLine.append("\u2509");
                }
                fillerLine.setPrefix(String.valueOf(fillLine));
                if (!player.isOnline()) {
                    return;
                }
                String arrow = "\u27A4";
                if (!hasIsland()) {
                    islandOwner.setPrefix(GREEN + arrow + " Island Owner" + GRAY.toString() + " - " + RED + "No Island");
                } else {
                    String ownerName = getIsland(plugin).getOwnerName();
                    islandOwner.setPrefix(GREEN + arrow + " Island Owner" + GRAY.toString() + " - " + AQUA.toString() + ownerName);
                }
                userBalanceAmount.setPrefix(GREEN + arrow +  " Balance" + GRAY.toString() + " - " + AQUA.toString() + "$" + balance);
                blocksBroken.setPrefix(GREEN + "Blocks Broken:");

                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                RankManager rankManager = plugin.getRankManager();

                ItemStack blockItem = rankManager.getItemFromRank(getBlockRank(), Type.BLOCK);
                ItemStack mobItem = rankManager.getItemFromRank(getMobRank(), Type.MOB);

                Upgrades blockUpgrades = upgradesConfig.getUpgrades(blockItem, Type.BLOCK);
                Upgrades mobUpgrades = upgradesConfig.getUpgrades(mobItem, Type.MOB);

                int blockReq = rankManager.getBlockRequirement(rankManager.getItemFromRank(getBlockRank() + 1, Type.BLOCK));
                int mobReq = rankManager.getMobItemRequirement(rankManager.getItemFromRank(getMobRank() + 1, Type.MOB));

                if (blockUpgrades != null && blockRequirements != null && (blockReq != -1 || getBlockRank() == 0)) {
                    int blockAmount = blockRequirements.get(blockItem);
                    String name = blockItem.getType().toString();
                    if (blockItem.getItemMeta() != null) {
                        name = blockItem.getItemMeta().getDisplayName();
                    }
                    blocksBrokenAmount.setPrefix(GREEN + arrow + " " +  name + GRAY + " - " + AQUA + blockAmount +
                            GRAY + " / " + AQUA + blockReq);
                } else {
                    blocksBrokenAmount.setPrefix(GREEN + arrow + GREEN + " Completed!");
                }
                mobsKilled.setPrefix(GREEN + "Mobs Killed:");

                if (mobUpgrades != null && mobRequirements != null && (mobReq != -1 || getMobRank() == 0)) {
                    int mobAmount = mobRequirements.get(mobItem);
                    String name = mobItem.getType().toString();
                    if (mobItem.getItemMeta() != null) {
                        name = mobItem.getItemMeta().getDisplayName();
                    }
                    mobsKilledAmount.setPrefix(GREEN + arrow + " " + name + GRAY + " - " + AQUA + mobAmount +
                            GRAY + " / " + AQUA + mobReq);
                } else {
                    mobsKilledAmount.setPrefix(GREEN + arrow + GREEN + " Completed!");
                }
                player.setScoreboard(board);
            }
        };
        sync.runTask(plugin);
    }

    public void updateScoreBoard(BetterSkyblock plugin) {
        BukkitRunnable sync = new BukkitRunnable() {
            @Override
            public void run() {
                if (!isOnline()) {
                    cancel();
                    return;
                }
                Player player = getPlayer();
                Scoreboard board = player.getScoreboard();

                final Team islandOwner = board.getTeam("Island Owner");
                final Team userBalanceAmount = board.getTeam("Balance Amount");
                final Team blocksBrokenAmount = board.getTeam("Blocks Amount");
                final Team mobsKilledAmount = board.getTeam("Mobs Amount");

                if (islandOwner == null || userBalanceAmount == null || blocksBrokenAmount == null ||
                        mobsKilledAmount == null) {
                    cancel();
                    return;
                }
                String arrow = "\u27A4";
                if (!hasIsland()) {
                    islandOwner.setPrefix(GREEN + arrow + " Island Owner" + GRAY.toString() + " - " + RED + "No Island");
                } else {
                    if (getIsland(plugin) == null) {
                        return;
                    }
                    String ownerName = getIsland(plugin).getOwnerName();
                    islandOwner.setPrefix(GREEN + arrow + " Island Owner" + GRAY.toString() + " - " + AQUA.toString() + ownerName);
                }
                userBalanceAmount.setPrefix(GREEN + arrow + " Balance" + GRAY.toString() + " - " + AQUA.toString() + "$" + balance);

                UpgradesConfig upgradesConfig = plugin.getUpgradesConfig();
                RankManager rankManager = plugin.getRankManager();

                ItemStack blockItem = rankManager.getItemFromRank(getBlockRank(), Type.BLOCK);
                ItemStack mobItem = rankManager.getItemFromRank(getMobRank(), Type.MOB);

                Upgrades blockUpgrades = upgradesConfig.getUpgrades(blockItem, Type.BLOCK);
                Upgrades mobUpgrades = upgradesConfig.getUpgrades(mobItem, Type.MOB);

                int blockReq = rankManager.getBlockRequirement(rankManager.getItemFromRank(getBlockRank() + 1, Type.BLOCK));
                int mobReq = rankManager.getMobItemRequirement(rankManager.getItemFromRank(getMobRank() + 1, Type.MOB));

                if (blockUpgrades != null && blockRequirements != null && (blockReq != -1 || getBlockRank() == 0)) {
                    int blockAmount = blockRequirements.get(blockItem);
                    String name = blockItem.getType().toString();
                    if (blockItem.getItemMeta() != null) {
                        name = blockItem.getItemMeta().getDisplayName();
                    }
                    blocksBrokenAmount.setPrefix(GREEN + arrow + " " +  name + GRAY + " - " + AQUA + blockAmount +
                            GRAY + " / " + AQUA + blockReq);
                } else {
                    blocksBrokenAmount.setPrefix(GREEN + arrow + GREEN + " Completed!");
                }

                if (mobUpgrades != null && mobRequirements != null && (mobReq != -1 || getMobRank() == 0)) {
                    int mobAmount = mobRequirements.get(mobItem);
                    String name = mobItem.getType().toString();
                    if (mobItem.getItemMeta() != null) {
                        name = mobItem.getItemMeta().getDisplayName();
                    }
                    mobsKilledAmount.setPrefix(GREEN + arrow + " " + name + GRAY + " - " + AQUA + mobAmount +
                            GRAY + " / " + AQUA + mobReq);
                } else {
                    mobsKilledAmount.setPrefix(GREEN + arrow + GREEN + " Completed!");
                }
                cancel();
            }
        };
        sync.runTask(plugin);
    }

    public void listIslandMembers(BetterSkyblock plugin) {
        Player player = getPlayer();
        if (!hasIsland()) {
            ChatUtil.sendNoIsland(player);
        }

        Island island = getIsland(plugin);
        CompletableFuture.supplyAsync(() ->  {
            List<String> members = new ArrayList<>();
            for (UUID member : island.getMembers()) {
                if (!uuid.equals(member)) {
                    String role = "[Member]";
                    if (member.toString().equals(island.getOwnerUUID())) {
                        role = "[Owner]";
                    }
                    String finalRole = role;
                    String memberName = plugin.getUserManager().getNameFromUUID(member.toString());
                    members.add(finalRole + " " + memberName);
                }
            }
            return members;
        }).thenAccept(members -> {
            player.sendMessage(ChatUtil.getFancyStringFromList(members, "Island Members:", GOLD.toString(), AQUA.toString()));
        });
    }

    public void joinIsland(String playerName, BetterSkyblock plugin) {
        Player player = getPlayer();

        CompletableFuture.supplyAsync(() -> plugin.getUserManager().getUUIDFromName(playerName)).thenAccept(uuid -> {
            if (islandInvites == null || islandInvites.isEmpty()) {
                player.sendMessage(RED + "You have not been invited to any islands!");
            }

            int islandId = plugin.getIslandManager().getIslandIdFromUUID(uuid);

            if (islandInvites.contains(islandId)) {
                this.islandId = islandId;
                this.role = Role.MEMBER;
                plugin.getIslandManager().getIsland(islandId).getMembers().add(this.uuid);
            } else {
                player.sendMessage(RED + "You have not been invited to that island!");
                return;
            }
            player.sendMessage(GREEN + "You have joined " + AQUA.toString() + playerName + GREEN + "'s island!");
            Island island = getIsland(plugin);
            for (UUID memberUUID : island.getMembers()) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member == null) continue;;
                member.sendMessage(AQUA.toString() + player.getName() + GREEN + " has joined the island.");
            }
        });
    }

    public void leaveIsland(BetterSkyblock plugin) {
        Player player = getPlayer();
        if (!hasIsland()) {
            ChatUtil.sendNoIsland(player);
            return;
        }

        if (getRole() == Role.OWNER) {
            player.sendMessage(RED + "You are the island owner! You must delete your island!");
            return;
        }
        Island island = getIsland(plugin);
        for (UUID memberUUID : island.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;;
            member.sendMessage(RED + playerName + " has left the island.");
        }
        removeFromIsland(plugin);
        player.sendMessage(GREEN + "You have left your island!");
    }

    public void payPlayer(User payedUser, double amount) {
        Player player = getPlayer();
        if (payedUser == null) {
            player.sendMessage(RED + "That player does not exist!");
            return;
        }

        if (amount < 0) {
            player.sendMessage(RED + "You cannot pay negative money!");
            return;
        }

        if (amount > this.balance) {
            player.sendMessage(RED + "You do not have enough money for this!");
            return;
        }

        double playerBalance = payedUser.getBalance();
        payedUser.setBalance(playerBalance+amount);
        setBalance(balance - amount);
        player.sendMessage(GREEN + "You have payed " + payedUser.getPlayerName() + " $" + amount + ".");
        payedUser.getPlayer().sendMessage(GREEN + "You have received $" + amount + " from " + player.getName() + ".");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(uuid, user.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }
}
