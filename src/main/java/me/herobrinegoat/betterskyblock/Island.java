package me.herobrinegoat.betterskyblock;

import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.generators.*;
import me.herobrinegoat.betterskyblock.inventories.GeneratorMenu;
import me.herobrinegoat.betterskyblock.inventories.IslandInventory;
import me.herobrinegoat.betterskyblock.inworld.Region;
import me.herobrinegoat.betterskyblock.managers.IslandManager;
import me.herobrinegoat.betterskyblock.managers.RankManager;
import me.herobrinegoat.betterskyblock.saving.ConnectionPool;
import me.herobrinegoat.betterskyblock.saving.Savable;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import me.herobrinegoat.betterskyblock.utils.ItemBuilder;
import me.herobrinegoat.betterskyblock.utils.ItemStackSerializer;
import me.herobrinegoat.betterskyblock.utils.TeleportUtil;
import me.herobrinegoat.menuapi.Page;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.bukkit.ChatColor.*;

public class Island implements Savable, Menu {

    private int islandId;

    private Set<UUID> members;

    private String ownerName;

    private Region region;

    private Set<Generator> generators;

    private Map<Long, Set<PlaceableGenerator>> placeableGenerators;

    private boolean isDeleted;

    private int generatorId;

    private String ownerUUID;

    private Location home;

    private Location warp;

    private Menu islandMenu;

    private int value;

    private BukkitRunnable placeableGeneratorUpdater;

    private LocalDateTime dateCreated;

    private GeneratorMenu generatorMenu;

    private List<SettingValue> settings;

    private Page islandPage;

    private boolean canBeDeleted;

    private boolean isDeleting;
    
    private boolean doneDeleting;

    public Island(Region region) {
        this.region = region;
    }

    public Island(int islandId, Set<UUID> members, Region region, Set<Generator> generators, int generatorId, String ownerUUID,
                  String ownerName, Location home, Location warp, List<SettingValue> settingValues) {
        this.islandId = islandId;
        this.members = members;
        this.region = region;
        setGenerators(generators);
        this.generatorId = generatorId;
        this.ownerUUID = ownerUUID;
        this.ownerName = ownerName;
        if (home == null) {
            home = getCenter();
        }
        if (warp == null) {
            warp = getCenter();
        }
        this.home = home;
        this.warp = warp;
        this.settings = settingValues;
        setDefaultSettings();
    }

    public Island(int islandId, Set<UUID> members, Region region, Set<Generator> generators,
                  int generatorId, String ownerUUID, String ownerName,  Location home, Location warp,
                  int value, LocalDateTime dateCreated, List<SettingValue> settingValues) {
        this(islandId, members, region, generators, generatorId, ownerUUID, ownerName, home, warp, settingValues);
        this.value = value;
        this.dateCreated = dateCreated;
    }

    public boolean placeableGeneratorUpdaterIsActive() {
        return placeableGeneratorUpdater != null;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void startPlaceableGeneratorUpdater(BetterSkyblock plugin) {
        if (placeableGeneratorUpdaterIsActive()) {
            return;
        }
        placeableGeneratorUpdater = new BukkitRunnable() {
            @Override
            public void run() {
                for (Set<PlaceableGenerator> placeableGenerators : new HashMap<>(placeableGenerators).values()) {
                    for (PlaceableGenerator generator : placeableGenerators) {
                        if (generator.isRemoved()) {
                            continue;
                        }
                        if (!(generator instanceof BlockGenerator)) {
                            continue;
                        }
                        BlockGenerator blockGenerator = (BlockGenerator) generator;
                        blockGenerator.generate();
                        Block block = blockGenerator.getLocation().getBlock();
                        Material material = blockGenerator.getBlock();
                        if (block.getType() == material) {
                            continue;
                        }
                        if (blockGenerator.getTotalResources() > 0 && block.getType() != material) {
                            BukkitRunnable sync = new BukkitRunnable() {
                                @Override
                                public void run() {
                                    block.setType(material);
                                }
                            };
                            sync.runTask(plugin);
                        }
                    }
                }
            }
        };
        placeableGeneratorUpdater.runTaskTimerAsynchronously(plugin,40,40);
    }

    public void setGeneratorDates(LocalDateTime time) {
        for (Set<PlaceableGenerator> generators : placeableGenerators.values()) {
            for (PlaceableGenerator placeableGenerator: generators) {
                placeableGenerator.setLastGeneratedDate(time);
            }
        }
    }

    public void setGenerators(Set<Generator> generators) {
        this.generators = generators;
        if (placeableGenerators == null) {
            placeableGenerators = new HashMap<>();
        }
        for (Generator generator : generators) {
            if (generator instanceof PlaceableGenerator) {
                PlaceableGenerator placeableGenerator = (PlaceableGenerator) generator;
                long chunkLong = placeableGenerator.getChunkLong();
                Set<PlaceableGenerator> placeableGenerators;
                if (!this.placeableGenerators.containsKey(chunkLong) || this.placeableGenerators.get(chunkLong) == null) {
                    placeableGenerators = new HashSet<>();
                } else {
                    placeableGenerators = this.placeableGenerators.get(chunkLong);
                }
                placeableGenerators.add(placeableGenerator);
                this.placeableGenerators.put(chunkLong, placeableGenerators);
            }
        }
    }

    public List<SettingValue> getSettings() {
        return settings;
    }

    public void setSettings(List<SettingValue> settings) {
        this.settings = settings;
    }

    public Location getCenter() {
        Location center = region.getCenter();
        center.setY(64);
        return center;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public int getIslandId() {
        return islandId;
    }

    public boolean withinIsland(Location location, boolean includeBounds) {
        return region.inRegion(location, includeBounds);
    }

    public boolean withinIsland(int x, int y, int z, boolean includeBounds) {
        return region.inRegion(x, y, z, includeBounds);
    }

    public String getOwnerName() {
        return ownerName;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<Generator> getGenerators() {
        return generators;
    }

    public Map<Long, Set<PlaceableGenerator>> getPlaceableGenerators() {
        return placeableGenerators;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public int getGeneratorId() {
        return generatorId;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public Location getHome() {
        return home;
    }

    public Location getWarp() {
        return warp;
    }

    public Menu getIslandMenu() {
        return islandMenu;
    }

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public GeneratorMenu getGeneratorMenu() {
        return generatorMenu;
    }

    public Generator getGenerator(int id) {
        for (Generator generator : generators) {
            if (generator.getId() == id) {
                return generator;
            }
        }
        return null;
    }

    public enum Setting {

        BLOCK_BREAK,

        BLOCK_PLACE,

        KILL_ANIMAL,

        KILL_MONSTER,

        ENDER_PEARL,

        ANVIL,

        ENCHANT,

        FURNACE,

        CHEST,

        HOPPER,

        BARREL,

        SHULKER_BOX,

        BOAT,

        ANIMAL_RIDE,

        ANIMAL_INTERACT,

        PICKUP_ITEM,

        DROP_ITEM,

        USE_BED,

        CROP_TRAMPLE,

        USE_ARMOR_STAND,

        GENERATOR_PLACE(SettingType.MEMBER),

        GENERATOR_COLLECT(SettingType.MEMBER),

        GENERATOR_UPGRADE(SettingType.MEMBER),

        GENERATOR_REMOVE(SettingType.MEMBER),

        TELEPORT(SettingType.VISITOR),

        ANIMAL_SPAWNING(SettingType.ISLAND),

        MONSTER_SPAWNING(SettingType.ISLAND),

        CREEPER_EXPLODE(SettingType.ISLAND),

        CREEPER_BREAK_BLOCKS(SettingType.ISLAND),

        TNT_EXPLODE(SettingType.ISLAND),

        TNT_BREAK_BLOCKS(SettingType.ISLAND);

        private SettingType settingType;

        Setting() {}

        Setting(SettingType settingType) {
            this.settingType = settingType;
        }

        public SettingType getSettingType() {
            return settingType;
        }

    }

    public Page getIslandPage(BetterSkyblock plugin) {
        if (this.islandPage == null) {
            IslandInventory islandInventory = new IslandInventory(this, plugin);
            this.islandPage = islandInventory.getPage();
        }
        return this.islandPage;
    }

    public GeneratorMenu getGeneratorPage(BetterSkyblock plugin) {
        if (this.generatorMenu == null) {
            this.generatorMenu = new GeneratorMenu(this, plugin);
        }
        return generatorMenu;
    }

    public void setGeneratorMenu(GeneratorMenu generatorMenu) {
        this.generatorMenu = generatorMenu;
    }

    public void calculateValue() {
        int totalWorth = 0;
        Set<Generator> generators = new HashSet<>(getGenerators());
        for (Generator generator : generators) {
            if (generator.getUpgrades() != null) {
                Upgrades upgrades = generator.getUpgrades();
                int level = generator.getLevel();
                Upgrades.Upgrade upgrade = upgrades.getUpgrades().get(level);
                if (level == 0 && generator instanceof InventoryGenerator) {
                    continue;
                }
                int addAmount = upgrade.getMoneyCost();
                if (generator instanceof PlaceableGenerator && ((PlaceableGenerator) generator).getStackAmount() > 0) {
                    addAmount = addAmount * ((PlaceableGenerator) generator).getStackAmount();
                }
                totalWorth += addAmount;
            }
        }
        this.value = totalWorth;
    }

    public int getValue() {
        return value;
    }

    public void inviteMember(User inviter, User invited, BetterSkyblock plugin) {

        if (inviter == null) {
            return;
        }

        Player inviterPlayer = inviter.getPlayer();

        if (invited == null) {
            inviterPlayer.sendMessage(Messages.PLAYER_NOT_ONLINE);
            return;
        }

        if (inviter.getRole() != User.Role.OWNER) {
            inviterPlayer.sendMessage(Messages.NO_PERMISSION);
            return;
        }

        if (members.contains(invited.getUuid())) {
            inviterPlayer.sendMessage(Messages.PLAYER_IN_YOUR_ISLAND);
            return;
        }

        if (invited.hasIsland()) {
            inviterPlayer.sendMessage(Messages.OTHER_PLAYER_IN_ISLAND);
            return;
        }

        invited.getIslandInvites().add(islandId);
        Player invitedPlayer = invited.getPlayer();
        for (UUID memberUUID : getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member == null) continue;
            member.sendMessage(AQUA + invitedPlayer.getName() + GREEN + " has been invited to the island.");
        }
        invitedPlayer.sendMessage(GREEN + "You have been invited to join " + AQUA + getOwnerName() + GREEN + "'s island!");
        invitedPlayer.sendMessage(BLUE + "Use /island join " + AQUA + getOwnerName() + BLUE + " to join their island. This invite lasts 30 seconds.");
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                invited.getIslandInvites().remove(islandId);
            }
        };
        r.runTaskLater(plugin, 600);
    }

    public void kickMember(User kicker, String kickedUUID, BetterSkyblock plugin) {

        if (kicker == null || kickedUUID == null) {
            return;
        }

        Player kickerPlayer = kicker.getPlayer();

        if (kicker.getRole() != User.Role.OWNER) {
            kickerPlayer.sendMessage(Messages.NO_PERMISSION);
            return;
        }

        if (!members.contains(UUID.fromString(kickedUUID))) {
            kickerPlayer.sendMessage(Messages.NOT_IN_YOUR_ISLAND);
            return;
        }

        CompletableFuture.supplyAsync(() -> plugin.getUserManager().getNameFromUUID(kickedUUID)).thenAccept(kickedName -> {
            kickerPlayer.sendMessage(GREEN + kickedName + " has been kicked from your island!");
            removeMember(kickedUUID);

            Player kickedPlayer = Bukkit.getPlayer(UUID.fromString(kickedUUID));

            if (kickedPlayer != null) {
                User user = plugin.getUserManager().getUser(kickedPlayer);
                user.removeFromIsland(plugin);
                kickedPlayer.sendMessage(RED + "You have been kicked from your island!");
                return;
            }

            plugin.getUserManager().updatePlayerIsland(kickedUUID, -1, User.Role.NO_ISLAND);
        });
    }

    public void removeMember(String uuid) {
        members.remove(UUID.fromString(uuid));
    }

    public void create(BetterSkyblock plugin, User user) {
        IslandManager islandManager = plugin.getIslandManager();
        if (user.getLastIslandCreatedTime() == null) {
            user.setLastIslandCreatedTime(LocalDateTime.now());
        } else {
            LocalDateTime dateTime = user.getLastIslandCreatedTime();
            Duration duration = Duration.between(dateTime, LocalDateTime.now());
            if (duration.getSeconds() < 300) {
                String timeLeft;
                int minutesLeft = (int) ((300 - duration.getSeconds()) / 60);
                int secondsLeft = (int) ((300 - duration.getSeconds()) % 60);
                timeLeft = minutesLeft + " minutes and " + secondsLeft;
                user.getPlayer().sendMessage(RED + "You have recently created an island, please wait " + timeLeft + " " +
                        "more seconds to create a island.");
                return;
            } else {
                user.setLastIslandCreatedTime(null);
            }
        }
        Player player = user.getPlayer();
        setBlocksInWorld(plugin);
        BukkitRunnable delay = new BukkitRunnable() {
            @Override
            public void run() {
                player.sendMessage(GREEN + "You have created a brand new island. Have Fun!");
                teleportToArea(user, plugin, home);
            }
        };
        for (Generator generator : generators) {
            generator.setLastGeneratedDate(LocalDateTime.now());
        }
        player.sendMessage(YELLOW + "Creating island, please wait...");
        delay.runTaskLater(plugin, 80);
        for (InventoryGenerator inventoryGenerator : plugin.getRankManager().getDefaultGenerators()) {
            InventoryGenerator generator = new InventoryGenerator(inventoryGenerator);
            generator.setId(generatorId);
            generator.setLastGeneratedDate(LocalDateTime.now());
            addGenerator(generator);
            generatorId++;
        }

        user.setIslandId(this.islandId);
        user.setRole(User.Role.OWNER);
        members.add(user.getUuid());
        islandManager.getIslands().put(islandId, this);

        startPlaceableGeneratorUpdater(plugin);
        this.dateCreated = LocalDateTime.now();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> save(plugin.getPool()));
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> user.save(plugin.getPool()));
    }

    public void deleteIsland(User deleter, BetterSkyblock plugin) {
        if (deleter == null) {
            return;
        }

        Player player = deleter.getPlayer();
        if (deleter.getRole() != User.Role.OWNER) {
            player.sendMessage(Messages.NO_PERMISSION);
            return;
        }

        if (!canBeDeleted) {
            player.sendMessage(RED + "[WARNING] This action cannot be undone!\n" + YELLOW + "Please type /island delete again to confirm your island deletion.");
            canBeDeleted = true;
            BukkitRunnable r = new BukkitRunnable() {
                @Override
                public void run() {
                    canBeDeleted = false;
                }
            };
            r.runTaskLater(plugin, 300);
            return;
        }
        addToResettingIslands(plugin);
        for (Player p : getOnlinePlayers()) {
            if (Bukkit.getWorld("SpawnWorld") != null) {
                p.teleport(Bukkit.getWorld("SpawnWorld").getSpawnLocation());
            }
        }
        for (UUID memberUUID : members) {
            if (memberUUID.equals(deleter.getUuid())) continue;
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                User islandMember = plugin.getUserManager().getUser(member);
                islandMember.removeFromIsland(plugin);
            } else {
                plugin.getUserManager().updatePlayerIsland(memberUUID.toString(), -1, User.Role.NO_ISLAND);
            }
        }
        placeableGeneratorUpdater.cancel();
        deleter.removeFromIsland(plugin);
        plugin.getIslandManager().getIslands().remove(this.islandId);
        deleteIslandFromDB(plugin.getPool());
        plugin.getIslandManager().saveIslandLocations();
        player.sendMessage(Messages.ISLAND_DELETED);
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
    }

    public void teleportToArea(User user, BetterSkyblock plugin, Location location) {
        TeleportUtil teleportUtil = new TeleportUtil();
        Location teleport = location.clone();
        if (teleport.getWorld() != null) {
            World world = teleport.getWorld();
            int teleportX = teleport.getBlockX();
            int teleportY = teleport.getBlockY();
            int teleportZ = teleport.getBlockZ();
            if (world.getBlockAt(teleportX, teleportY -1, teleportZ).getType() == Material.AIR || teleport.getBlock().getType() != Material.AIR ||
                    world.getBlockAt(teleportX, teleportY + 1, teleportZ).getType() != Material.AIR) {
                user.getPlayer().sendMessage(Messages.UNSAFE_AREA + "\n" + YELLOW +
                        "Attempting to teleport you to your island center, if unsuccessful please contact a server staff member for help.");
                teleport = getCenter().clone();
                if (world.getBlockAt(teleport.getBlockX(), teleport.getBlockY() - 1, teleport.getBlockZ()).getType() == Material.AIR) {
                    teleport = world.getHighestBlockAt(teleport).getLocation();
                }
            }
            teleport.setX(teleport.getBlockX() + .5);
            teleport.setZ(teleport.getBlockZ() + .5);

            teleportUtil.teleportIfNotMoving(user.getPlayer(), teleport, 3, plugin);
        } else {
            if (user != null) {
                user.getPlayer().sendMessage(RED + "Error teleporting to that world");
            }
        }
    }

    public void setHome(Location home) {
        this.home = home;
    }

    public void setWarp(Location warp) {
        this.warp = warp;
    }

    private void setDefaultSettings() {
        if (settings == null) {
            this.settings = new ArrayList<>();
        }

        List<String> sortedSettings = new ArrayList<>();
        for (Setting setting : Setting.values()) {
            sortedSettings.add(setting.toString());
        }
        Collections.sort(sortedSettings);

        for (SettingType settingType : SettingType.values()) {
            for (String settingString : sortedSettings) {
                Setting setting = Setting.valueOf(settingString);
                SettingValue settingValue = null;
                if (setting.getSettingType() != SettingType.ISLAND) {
                    if (settingType == SettingType.MEMBER && setting.getSettingType() != SettingType.VISITOR) {
                        settingValue = new SettingValue(setting, settingType, true);
                    } else if (settingType == SettingType.VISITOR && setting.getSettingType() != SettingType.MEMBER) {
                        settingValue = new SettingValue(setting, settingType, false);
                    }
                } else {
                    if (settingType == SettingType.ISLAND) {
                        settingValue = new SettingValue(setting, settingType, true);
                    }
                }
                if (settingValue != null && !this.settings.contains(settingValue)) {
                    this.settings.add(settingValue);
                }
            }
        }
    }

    public enum SettingType {

        VISITOR,

        MEMBER,

        ISLAND
    }

    public Region getRegion() {
        return region;
    }

    public PlaceableGenerator getGeneratorFromLocation(Location location) {
        long x = (location.getBlockX() >> 4);
        long z = (location.getBlockZ() >> 4);
        long l = x & 0xffffffffL | (z & 0xffffffffL) << 32;
        if (placeableGenerators.get(l) == null) {
            return null;
        }
        for (PlaceableGenerator generator : placeableGenerators.get(l)) {
            if (generator != null && generator.getLocation().equals(location) && !generator.isRemoved()) {
                return generator;
            }
        }
        return null;
    }

    public int getGeneratorId(boolean addOne) {
        if (addOne) {
            generatorId++;
        }
        return generatorId;
    }

    public void addGenerator(Generator generator) {
        this.generators.add(generator);
        if (generator.getId() < 0) {
            generatorId++;
            generator.setId(generatorId);
        }
        if (generator instanceof PlaceableGenerator) {
            long l = ((PlaceableGenerator) generator).getChunkLong();
            Set<PlaceableGenerator> placeableGeneratorsSet;
            if (placeableGenerators.containsKey(l)) {
                placeableGeneratorsSet = placeableGenerators.get(l);
            } else {
                placeableGeneratorsSet = new HashSet<>();
            }
            placeableGeneratorsSet.add((PlaceableGenerator) generator);
            placeableGenerators.put(l, placeableGeneratorsSet);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Island)) return false;
        Island island = (Island) o;
        return islandId == island.islandId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(islandId);
    }

    @Override
    public void save(ConnectionPool pool) {
        calculateValue();
        saveIsland(pool);
        saveGenerators(pool);
        savePlaceableGenerators(pool);
        saveSettings(pool);
    }

    private void saveIsland(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getIslandDataTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + "(id,owner_uuid,owner_name,lower_x,upper_x,lower_y,upper_y,lower_z,upper_z,home_x,home_y,home_z," +
                    "warp_x,warp_y,warp_z,generator_id,date_created,worth) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE owner_uuid=?, owner_name=?, lower_x=?, upper_x=?, lower_y=?, upper_y=?, lower_z=?, upper_z=?, " +
                    "home_x=?, home_y=?, home_z=?, warp_x=?, warp_y=?, warp_z=?, generator_id=?, worth=?");
                statement.setInt(1, islandId);
                statement.setString(2, ownerUUID);
                statement.setString(3, ownerName) ;
                statement.setInt(4, region.getLowerX());
                statement.setInt(5, region.getUpperX());
                statement.setInt(6, region.getLowerY());
                statement.setInt(7, region.getUpperY());
                statement.setInt(8, region.getLowerZ());
                statement.setInt(9, region.getUpperZ());
                statement.setInt(10, home.getBlockX());
                statement.setInt(11, home.getBlockY());
                statement.setInt(12, home.getBlockZ());
                statement.setInt(13, warp.getBlockX());
                statement.setInt(14, warp.getBlockY());
                statement.setInt(15, warp.getBlockZ());
                statement.setInt(16, generatorId);
                statement.setObject(17, dateCreated);
                statement.setObject(18, value);
                statement.setString(19, ownerUUID);
                statement.setString(20, ownerName);
                statement.setInt(21, region.getLowerX());
                statement.setInt(22, region.getUpperX());
                statement.setInt(23, region.getLowerY());
                statement.setInt(24, region.getUpperY());
                statement.setInt(25, region.getLowerZ());
                statement.setInt(26, region.getUpperZ());
                statement.setInt(27, home.getBlockX());
                statement.setInt(28, home.getBlockY());
                statement.setInt(29, home.getBlockZ());
                statement.setInt(30, warp.getBlockX());
                statement.setInt(31, warp.getBlockY());
                statement.setInt(32, warp.getBlockZ());
                statement.setInt(33, generatorId);
                statement.setInt(34, value);
                statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    private void savePlaceableGenerators(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getPlaceableGeneratorDataTable();
        Set<PlaceableGenerator> removedGenerators = new HashSet<>();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + "(id,island_id,item_generated,resources_generated,last_generated_date,level,type,stack_amount,world,x,y,z,generator_type) " +
                    "values (?,?,?,?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE resources_generated=?, last_generated_date=?, level=?, stack_amount=?");
            for (Set<PlaceableGenerator> placeableGenerators : placeableGenerators.values()) {
                for (PlaceableGenerator generator : placeableGenerators) {
                    if (generator.isRemoved()) {
                        removedGenerators.add(generator);
                        continue;
                    }
                    int generatorId = generator.getId();
                    String item = ItemStackSerializer.itemStackToBase64(generator.getGeneratedItemStack());
                    int resourcesGenerated = generator.getTotalResources();
                    LocalDateTime lastGeneratedData = generator.getLastGeneratedDate();
                    int level = generator.getLevel();
                    String type = null;
                    if (generator instanceof MobGenerator) {
                        type = ((MobGenerator) generator).getEntityType().toString();
                    } else if (generator instanceof BlockGenerator) {
                        type = ((BlockGenerator) generator).getBlock().toString();
                    }
                    int stackAmount = generator.getStackAmount();
                    Location location = generator.getLocation();
                    String world = null;
                    if (location.getWorld() != null) {
                        world = location.getWorld().getName();
                    }
                    int x = location.getBlockX();
                    int y = location.getBlockY();
                    int z = location.getBlockZ();
                    String generatorType = generator.getClass().getSimpleName();
                    statement.setInt(1, generatorId);
                    statement.setInt(2, islandId);
                    statement.setString(3, item);
                    statement.setInt(4, resourcesGenerated);
                    statement.setObject(5, lastGeneratedData);
                    statement.setInt(6, level);
                    statement.setString(7, type);
                    statement.setInt(8, stackAmount);
                    statement.setString(9, world);
                    statement.setInt(10, x);
                    statement.setInt(11, y);
                    statement.setInt(12, z);
                    statement.setString(13, generatorType);
                    statement.setInt(14, resourcesGenerated);
                    statement.setObject(15, lastGeneratedData);
                    statement.setInt(16, level);
                    statement.setInt(17, stackAmount);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
        deleteGenerators(removedGenerators, pool);
    }

    public void deleteGenerators(Set<PlaceableGenerator> removeGenerators, ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getPlaceableGeneratorDataTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("DELETE FROM " + table + " WHERE id=? AND island_id=? AND generator_type=?");
            for (PlaceableGenerator generator : removeGenerators) {
                statement.setInt(1, generator.getId());
                statement.setInt(2, islandId);
                statement.setString(3, generator.getClass().getSimpleName());
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public List<InventoryGenerator> getInventoryGenerators() {
        List<InventoryGenerator> inventoryGenerators = new ArrayList<>();
        for (Generator generator : generators) {
            if (generator instanceof InventoryGenerator) {
                inventoryGenerators.add((InventoryGenerator) generator);
            }
        }
        return inventoryGenerators;
    }

    private void saveGenerators(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getInventoryGeneratorDataTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + "(id,island_id,item_generated,resources_generated,last_generated_date,level) " +
                    "values (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE resources_generated=?, last_generated_date=?, level=?");
            for (Generator generator : generators) {
                if (!(generator instanceof InventoryGenerator)) {
                    continue;
                }
                    int generatorId = generator.getId();
                    String item = ItemStackSerializer.itemStackToBase64(generator.getGeneratedItemStack());
                    int resourcesGenerated = generator.getTotalResources();
                    LocalDateTime lastGeneratedDate = generator.getLastGeneratedDate();
                    int level = generator.getLevel();
                    statement.setInt(1, generatorId);
                    statement.setInt(2, islandId);
                    statement.setString(3, item);
                    statement.setInt(4, resourcesGenerated);
                    statement.setObject(5, lastGeneratedDate);
                    statement.setInt(6, level);
                    statement.setInt(7, resourcesGenerated);
                    statement.setObject(8, lastGeneratedDate);
                    statement.setInt(9, level);
                    statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    private void saveSettings(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getIslandSettingsTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("INSERT INTO " + table + " (island_id,setting,type,bool) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE bool=?");
            for (SettingValue settingValue : getSettings()) {
                String setting = settingValue.getSetting().toString();
                String type = settingValue.getSettingType().toString();
                boolean value = settingValue.getValue();
                statement.setInt(1, islandId);
                statement.setString(2, setting);
                statement.setString(3, type);
                statement.setBoolean(4, value);
                statement.setBoolean(5, value);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    public void deleteIslandFromDB(ConnectionPool pool) {
        Connection conn = null;
        PreparedStatement statement = null;
        String table = pool.getIslandDataTable();
        try {
            conn = pool.getConnection();
            statement = conn.prepareStatement("DELETE FROM " + table + " WHERE id='" + islandId + "';");
            statement.executeUpdate();
            pool.close(conn, statement, null);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            pool.close(conn, statement, null);
        }
    }

    @Override
    public void delete(ConnectionPool pool) {

    }

    public boolean hasOnlinePlayers() {
        return hasOnlinePlayersExcluding(null);
    }

    public boolean hasOnlinePlayersExcluding(Player player) {
        Player checkPlayer;
        for (UUID uuid : members) {
            if (player != null && uuid.equals(player.getUniqueId())) {
                continue;
            }
            checkPlayer = Bukkit.getPlayer(uuid);
            if (checkPlayer != null) return true;
        }
        checkPlayer = Bukkit.getPlayer(UUID.fromString(ownerUUID));
        if (player != null && ownerUUID.equalsIgnoreCase(player.getUniqueId().toString())) {
            return false;
        }
        return checkPlayer != null;
    }

    public Set<Player> getOnlinePlayers() {
        Set<Player> members = new HashSet<>();
        for (UUID uuid : this.members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) members.add(player);
            Player owner = Bukkit.getPlayer(UUID.fromString(ownerUUID));
            if (owner != null) members.add(owner);
        }
        return members;
    }

    public BukkitRunnable getPlaceableGeneratorUpdater() {
        return this.placeableGeneratorUpdater;
    }

    public void setPlaceableGeneratorUpdater(BukkitRunnable placeableGeneratorUpdater) {
        this.placeableGeneratorUpdater = placeableGeneratorUpdater;
    }

    public void setBlocksInWorld(BetterSkyblock plugin) {

        World world = getCenter().getWorld();
        int centerX = getCenter().getBlockX();
        int centerZ = getCenter().getBlockZ();
        if (world == null) {
            return;
        }

        for (int x = centerX - 3; x <= centerX + 3; x++) {
            for (int z = centerZ - 3; z <= centerZ + 3; z++) {
                for (int y = 60; y < 65; y++) {
                    Location location = new Location(world, x, y, z);
                    Material material = Material.AIR;
                    if (y == 60 && x == centerX && z == centerZ) {
                        material = Material.BEDROCK;
                    } else if (y < 63) {
                        material = Material.DIRT;
                    } else if (y == 63) {
                        material = Material.GRASS_BLOCK;
                    } else if (x == centerX && z == centerZ +2){
                        material = Material.CHEST;
                    } else if ( x== centerX && z == centerZ -3) {
                        location.add(0, 1, 0);
                        world.generateTree(location, TreeType.TREE);
                        BukkitRunnable delay = new BukkitRunnable() {
                            @Override
                            public void run() {
                                location.getBlock().setType(Material.OAK_LOG);
                                location.subtract(0, 1, 0);
                                location.getBlock().setType(Material.OAK_LOG);
                            }
                        };
                        delay.runTaskLater(plugin, 1);
                    }
                    location.getBlock().setType(material);
                    BlockState state = location.getBlock().getState();
                    if (state instanceof Chest) {
                        Chest chest = (Chest) state;
                        BukkitRunnable runnable = new BukkitRunnable() {
                            @Override
                            public void run() {
                                for (ItemStack item : getSpawnChestItems(plugin)) {
                                    chest.getBlockInventory().addItem(item);
                                }
                            }
                        };
                        runnable.runTaskLater(plugin, 1);
                    }
                }
            }
        }
    }

    private List<ItemStack> getSpawnChestItems(BetterSkyblock plugin) {
        List<ItemStack> items = new ArrayList<>();
        ItemBuilder cobble = new ItemBuilder(Material.COBBLESTONE).setName(AQUA + "Cobblestone").addLore(" ", YELLOW + "Block");
        ItemBuilder chicken = new ItemBuilder(Material.CHICKEN).setName(AQUA + "Raw Chicken").addLore(" ", YELLOW + "Mob Drop");
        RankManager rankManager = plugin.getRankManager();
        BlockGenerator blockGenerator = new BlockGenerator(-1, cobble.get(), 0, null, null,
                rankManager.getItemRank(cobble.get(), Type.BLOCK), 1, Material.COBBLESTONE, null, 1);
        blockGenerator.setRank(0);
        generatorId++;
        MobGenerator mobGenerator = new MobGenerator(-1, chicken.get(), 0, null, null,
                rankManager.getItemRank(chicken.get(), Type.MOB), 1, EntityType.CHICKEN, null, 1);
        mobGenerator.setRank(0);
        items.add(new ItemStack(Material.DIRT, 16));
        items.add(new ItemStack(Material.GRASS_BLOCK, 16));
        items.add(new ItemStack(Material.WATER_BUCKET));
        items.add(new ItemStack(Material.ICE, 4));
        items.add(new ItemStack(Material.LAVA_BUCKET));
        items.add(new ItemStack(Material.OAK_SAPLING));
        items.add(new ItemStack(Material.BONE_MEAL, 12));
        items.add(new ItemStack(Material.POTATO));
        items.add(new ItemStack(Material.WHEAT_SEEDS));
        items.add(new ItemStack(Material.CARROT));
        items.add(new ItemStack(Material.PUMPKIN_SEEDS));
        items.add(new ItemStack(Material.MELON_SEEDS));
        items.add(new ItemStack(Material.CACTUS));
        items.add(new ItemStack(Material.SUGAR_CANE));
        items.add(blockGenerator.getItemForInventory(plugin));
        items.add(mobGenerator.getItemForInventory(plugin));
        return items;
    }

    public boolean isDoneDeleting() {
        return doneDeleting;
    }

    public void setDoneDeleting(boolean doneDeleting) {
        this.doneDeleting = doneDeleting;
    }

    public void setDeleting(boolean isDeleting) {
        this.isDeleting = isDeleting;
    }

    public boolean isDeleting() {
        return isDeleting;
    }

    private void addToResettingIslands(BetterSkyblock plugin) {
        Island island = new Island(getRegion());
        island.setDeleting(false);
        island.setDoneDeleting(false);
        plugin.getIslandManager().addResettingIsland(island);
    }

    public void removeBlocks(BetterSkyblock plugin) {
        int minX = region.getLowerX();
        int maxX = region.getUpperX();
        int minZ = region.getLowerZ();
        int maxZ = region.getUpperZ();
        World world = getCenter().getWorld();
        if (world == null) {
            return;
        }

        final int[] yLimit = {8};
        final int[] yHeight = {0};

        BukkitRunnable run = new BukkitRunnable() {
            @Override
            public void run() {
                List<Location> locs = new ArrayList<>();

                for (int x = minX; x < maxX + 16; x+=16) {
                    for (int z = minZ; z < maxZ + 16; z+=16) {
                        locs.add(new Location(world, x, 0, z));
                    }
                }
                final int[] locNum = {0};

                BukkitRunnable timer = new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location loc = locs.get(locNum[0]);
                        for (int x = 0; x < 16; x++) {
                            for (int z = 0; z < 16; z++) {
                                for (int y = yHeight[0]; y < yLimit[0]; y++) {
                                    int finalX = x + loc.getBlockX();
                                    int finalZ = z + loc.getBlockZ();
                                    int finalY = y;
                                    Material mat = Material.AIR;
                                    if (!withinIsland(finalX, 100, finalZ, true)) {
                                        continue;
                                    }

                                    Block block = loc.getBlock();
                                    if (block instanceof Container) {
                                        Container container = (Container) block;
                                        container.getInventory().clear();
                                    }
                                    BukkitRunnable sync = new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            plugin.getNms().setBlockInNativeChunk(getCenter().getWorld(), finalX, finalY, finalZ, mat, false);
                                        }
                                    };
                                    sync.runTask(plugin);
                                }
                            }
                        }
                        if (yLimit[0] < 256) {
                            if (yLimit[0] == 8) {
                                yLimit[0] = 64;
                                yHeight[0] = 8;
                            } else if (yHeight[0] == 8) {
                                yHeight[0] = 64;
                                yLimit[0] = 128;
                            } else {
                                yLimit[0]+=64;
                                yHeight[0]+=64;
                            }
                            if (yLimit[0] > 255) {
                                yLimit[0] = 256;
                            }
                            return;
                        }
                        yLimit[0] = 8;
                        yHeight[0] = 0;
                        locNum[0]++;
                        if (locNum[0] == locs.size()) {
                            setDoneDeleting(true);
                            cancel();
                            plugin.getLogger().info("Island Deletion ended");
                        }
                    }
                };
                timer.runTaskTimerAsynchronously(plugin, 5, 5);
            }
        };
        run.runTaskAsynchronously(plugin);
    }



}
