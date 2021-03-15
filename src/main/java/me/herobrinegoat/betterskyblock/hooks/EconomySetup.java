package me.herobrinegoat.betterskyblock.hooks;


import me.herobrinegoat.betterskyblock.BetterSkyblock;
import me.herobrinegoat.betterskyblock.User;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class EconomySetup implements Economy {

    BetterSkyblock plugin;

    public EconomySetup(BetterSkyblock plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 0;
    }

    @Override
    public String format(double v) {
        return null;
    }

    @Override
    public String currencyNamePlural() {
        return null;
    }

    @Override
    public String currencyNameSingular() {
        return null;
    }

    @Override
    public boolean hasAccount(String s) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean hasAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean hasAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }

    @Override
    public double getBalance(String s) {
        Player player = Bukkit.getPlayer(s);
        double money = 0;
        if (player != null) {
            money = plugin.getUserManager().getUser(player).getBalance();
        }
        return money;
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        String name = offlinePlayer.getName();
        if (offlinePlayer.isOnline()) {
            return getBalance(name);
        }

        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> plugin.getUserManager().getBalanceFromName(name)).thenApplyAsync(balance -> balance);
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public double getBalance(String s, String s1) {
        return getBalance(s);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer, String s) {
        return getBalance(offlinePlayer);
    }

    @Override
    public boolean has(String s, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, double v) {
        return false;
    }

    @Override
    public boolean has(String s, String s1, double v) {
        return false;
    }

    @Override
    public boolean has(OfflinePlayer offlinePlayer, String s, double v) {
        return false;
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, double money) {
        Player player = Bukkit.getPlayer(s);
        if (player != null) {
            User user = plugin.getUserManager().getUser(player);
            double oldMoney = user.getBalance();
            user.setBalance(oldMoney - money);
        }
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double money) {
        String name = offlinePlayer.getName();
        if (offlinePlayer.isOnline()) {
            withdrawPlayer(name, money);
            return null;
        }
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> plugin.getUserManager().getBalanceFromName(name)).thenApplyAsync(balance -> balance);
        try {
            double oldMoney = future.get();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getUserManager().setBalanceFromUUID(offlinePlayer.getUniqueId().toString(), oldMoney - money));
            return null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String s, String s1, double money) {
        withdrawPlayer(s, money);
        return null;
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, String s, double money) {
        withdrawPlayer(offlinePlayer, money);
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(String s, double money) {
        Player player = Bukkit.getPlayer(s);
        if (player != null) {
            User user = plugin.getUserManager().getUser(player);
            double oldMoney = user.getBalance();
            user.setBalance(oldMoney + money);
        }
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double money) {
        String name = offlinePlayer.getName();
        if (offlinePlayer.isOnline()) {
            depositPlayer(name, money);
            return null;
        }
        CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> plugin.getUserManager().getBalanceFromName(name)).thenApplyAsync(balance -> balance);
        try {
            double oldMoney = future.get();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> plugin.getUserManager().setBalanceFromUUID(offlinePlayer.getUniqueId().toString(), oldMoney + money));
            return null;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public EconomyResponse depositPlayer(String s, String s1, double money) {
        depositPlayer(s, money);
        return null;
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, String s, double money) {
        depositPlayer(offlinePlayer, money);
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse createBank(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse deleteBank(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankBalance(String s) {
        return null;
    }

    @Override
    public EconomyResponse bankHas(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankWithdraw(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse bankDeposit(String s, double v) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankOwner(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, String s1) {
        return null;
    }

    @Override
    public EconomyResponse isBankMember(String s, OfflinePlayer offlinePlayer) {
        return null;
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String s) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(String s, String s1) {
        return false;
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer offlinePlayer, String s) {
        return false;
    }
}
