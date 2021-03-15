package me.herobrinegoat.betterskyblock.events;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.Island;
import me.herobrinegoat.betterskyblock.SettingValue;
import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.configs.Messages;
import me.herobrinegoat.betterskyblock.utils.ChatUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.ShulkerBox;
import org.bukkit.block.data.type.Bed;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.EnumUtils;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import static org.bukkit.ChatColor.RED;

public class IslandProtection implements Listener {

    BetterSkyblock plugin;

    public IslandProtection(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void blockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (cancelEvent(player, event.getBlock().getLocation(), "BLOCK_BREAK")) event.setCancelled(true);
    }

    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (cancelEvent(player, event.getBlock().getLocation(), "BLOCK_PLACE")) event.setCancelled(true);
    }

    @EventHandler
    public void bucketPlace(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (cancelEvent(player, event.getBlock().getLocation(), "BLOCK_PLACE")) event.setCancelled(true);
    }

    @EventHandler
    public void hurtMob(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity damaged = event.getEntity();

            if (damaged instanceof Animals) {
                if (cancelEvent(player, damaged.getLocation(), "KILL_ANIMAL")) event.setCancelled(true);
            } else {
                if (cancelEvent(player, damaged.getLocation(), "KILL_MONSTER")) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void teleportEvent(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location loc = event.getTo();
        Location from = event.getFrom();
        if (loc == null) return;
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            if (cancelEvent(player, loc, "ENDER_PEARL")) event.setCancelled(true);
            player.getInventory().addItem(new ItemStack(Material.ENDER_PEARL, 1));
        }
        if(event.getCause().equals(PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) && plugin.getIslandWorld().equals(from.getWorld())){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockClick(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getClickedBlock() == null) return;
            Location clickedLoc = event.getClickedBlock().getLocation();
            Material typeClicked = event.getClickedBlock().getType();
            if (typeClicked.toString().toUpperCase().contains("ANVIL")) {
                if (cancelEvent(player, clickedLoc, "ANVIL")) event.setCancelled(true);
                return;
            }
            if (typeClicked == Material.ENCHANTING_TABLE) {
                if (cancelEvent(player, clickedLoc, "ENCHANT")) event.setCancelled(true);
                return;
                }
            if (typeClicked == Material.FURNACE) {
                if (cancelEvent(player, clickedLoc, "FURNACE")) event.setCancelled(true);
                return;
            }
            if (typeClicked.toString().toUpperCase().contains("CHEST")) {
                if (cancelEvent(player, clickedLoc, "CHEST")) event.setCancelled(true);
                return;
            }
            if (typeClicked == Material.HOPPER) {
                if (cancelEvent(player, clickedLoc, "HOPPER")) event.setCancelled(true);
                return;
            }
            if (typeClicked == Material.BARREL) {
                if (cancelEvent(player, clickedLoc, "BARREL")) event.setCancelled(true);
                return;
            }
            if (event.getClickedBlock().getState() instanceof ShulkerBox) {
                if (cancelEvent(player, clickedLoc, "SHULKER_BOX")) event.setCancelled(true);
                return;
            }
            if (event.getClickedBlock().getBlockData() instanceof Bed) {
                if (cancelEvent(player, clickedLoc, "USE_BED")) event.setCancelled(true);
                return;
            }
        }
        if (event.getAction() == Action.PHYSICAL) {
            if (event.getClickedBlock() == null) return;
            if (event.getClickedBlock().getType().equals(Material.FARMLAND)) {
                Location clickedLoc = event.getClickedBlock().getLocation();
                if (cancelEvent(player, clickedLoc, "CROP_TRAMPLE")) event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void clickEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();
        Location entityLoc = entity.getLocation();
        if (entity instanceof Boat) {
            if (cancelEvent(player, entityLoc, "BOAT")) event.setCancelled(true);
            return;
        }
        if (entity instanceof Vehicle && entity instanceof LivingEntity) {
            if (cancelEvent(player, entityLoc, "ANIMAL_RIDE")) event.setCancelled(true);
            return;
        }
        if (entity.getType() == EntityType.ARMOR_STAND) {
            if (cancelEvent(player, entityLoc, "USE_ARMOR_STAND")) event.setCancelled(true);
            return;
        }
        if (cancelEvent(player, entityLoc, "ANIMAL_INTERACT")) event.setCancelled(true);
    }

    @EventHandler
    public void pickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Location location = player.getLocation();
            if (cancelEvent(player, location, "PICKUP_ITEM")) event.setCancelled(true);
        }
    }

    @EventHandler
    public void dropItem(EntityDropItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            Location location = player.getLocation();
            if (cancelEvent(player, location, "DROP_ITEM")) event.setCancelled(true);
        }
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            User user = plugin.getUserManager().getUser(player);
            if (user != null && user.hasIsland()) {
                if (user.getRole() == User.Role.OWNER) {
                    user.getIsland(plugin).setCanBeDeleted(false);
                }
            }
        }
    }

    @EventHandler
    public void entityExplodeEvent(EntityExplodeEvent event) {
        Entity entity = event.getEntity();
        event.setCancelled(true);
    }

    @EventHandler
    public void entityDamageEvent(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            User user = plugin.getUserManager().getUser(player);
            if (user == null || !user.hasIsland()) {
                return;
            }
            if (plugin.getIslandManager().getIslandFromLocation(player.getLocation(), false) == null ||
                    !plugin.getIslandManager().getIslandFromLocation(player.getLocation(), false).equals(user.getIsland(plugin))) {
                if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                    if (user.hasIsland()) {
                        player.teleport(user.getIsland(plugin).getCenter());
                    } else {
                        player.teleport(plugin.getIslandWorld().getSpawnLocation());
                    }
                    player.setInvulnerable(true);
                    BukkitRunnable delay = new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.setInvulnerable(false);
                        }
                    };
                    delay.runTaskLater(plugin, 5);
                }
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void entitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        Location location = entity.getLocation();
        if (entity.getType() == EntityType.WITHER || entity.getType() == EntityType.PHANTOM) {
            event.setCancelled(true);
        }
        if (entity instanceof Monster || entity instanceof Slime) {
            if (cancelEvent(location, "MONSTER_SPAWNING")) event.setCancelled(true);
            return;
        }
        if (entity instanceof Animals) {
            if (cancelEvent(location, "ANIMAL_SPAWNING")) event.setCancelled(true);
        }
    }

    boolean cancelEvent(Player player, Location location, String setting) {
        User user = plugin.getUserManager().getUser(player);
        if (player.isOp()) {
            Island island = plugin.getIslandManager().getIslandFromLocation(location);
            if (island == null) {
                return false;
            }
            if (island.getGeneratorFromLocation(location) == null) {
                return false;
            }
            if (!island.equals(user.getIsland(plugin))) {
                player.sendMessage(RED + "You cannot break that block because it is a generator");
                return true;
            }
        }
        World world = location.getWorld();
        if (world == null) {
            return false;
        }

        boolean isIslandWorld = world.equals(plugin.getIslandWorld());

        if (!isIslandWorld) {
            return false;
        }

        if (user == null) {
            return true;
        }
        if (user.hasIsland()) {
            Island island = user.getIsland(plugin);
            if (island == null) {
                ChatUtil.sendActionBar(player, RED + "You cannot do this here!");
                return true;
            }
            if (island.withinIsland(location, false)) {
                if (user.getRole() == User.Role.OWNER) {
                    return false;
                }
                return !hasSettingPermission(user, island, setting);
            }
        }

        Island island = plugin.getIslandManager().getIslandFromLocation(location);
        if (island == null) {
            player.sendMessage(Messages.OUT_OF_ISLAND_RANGE);
            return true;
        }
        if (island.getGeneratorFromLocation(location) != null) return true;
        if (hasSettingPermission(user, island, setting)) return false;
        ChatUtil.sendActionBar(player, RED + "You cannot do this here!");
        return true;
    }

    public boolean hasSettingPermission(User user, Island island, String setting) {
        if (island == null) {
            return false;
        }
        User.Role role = user.getRole();
        Island.SettingType settingType = null;
        if (EnumUtils.isValidEnum(Island.SettingType.class, role.toString())) {
            settingType = Island.SettingType.valueOf(role.toString());
        }
        if (!island.equals(user.getIsland(plugin))) {
            settingType = Island.SettingType.VISITOR;
        }
        if (settingType == null) settingType = Island.SettingType.VISITOR;
        SettingValue settingValue = new SettingValue(Island.Setting.valueOf(setting), settingType, false);
        if (!island.getSettings().contains(settingValue)) {
            return false;
        }
        for (SettingValue s : island.getSettings()) {
            if (settingValue.equals(s)) {
                if (s.getValue()) return true;
                break;
            }
        }
        return false;
    }

    public boolean cancelEvent(Location location, String setting) {
        World world = location.getWorld();
        if (world == null) {
            return true;
        }

        if (!world.equals(plugin.getIslandWorld())) return false;
        Island island = plugin.getIslandManager().getIslandFromLocation(location);
        if (island == null) return true;
        SettingValue settingValue = new SettingValue(Island.Setting.valueOf(setting), Island.SettingType.valueOf("ISLAND"), false);
        if (!island.getSettings().contains(settingValue)) {
            return true;
        }
        for (SettingValue s : island.getSettings()) {
            if (settingValue.equals(s)) {
                if (s.getValue()) return false;
                break;
            }
        }
        return true;
    }

    public boolean isCenter(Player player, Location location) {
        World world = location.getWorld();
        if (world == null) {
            return true;
        }

        if (!world.equals(plugin.getIslandWorld())) return false;
        Island island = plugin.getIslandManager().getIslandFromLocation(location);
        if (island == null) return true;
        int centerX = island.getCenter().getBlockX();
        int centerZ = island.getCenter().getBlockZ();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (x >= centerX -1 && x <= centerX + 1 &&
                z >= centerZ -1 && z <= centerZ + 1 && y > 60 && y < 70) {
            ChatUtil.sendActionBar(player, RED + "You cannot do this here!");
             return true;
        }
        return false;
    }
}
