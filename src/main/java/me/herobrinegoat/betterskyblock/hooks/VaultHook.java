package me.herobrinegoat.betterskyblock.hooks;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class VaultHook {

    BetterSkyblock plugin;

    private EconomySetup provider;

    public VaultHook(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        this.provider = plugin.getEconomySetup();
        Bukkit.getServicesManager().register(Economy.class, this.provider, plugin, ServicePriority.Normal);
        plugin.getLogger().info("Vault Enabled");
    }

    public void unHook() {
        Bukkit.getServicesManager().unregister(Economy.class, this.provider);
        plugin.getLogger().info("Vault Disabled");
    }



}
