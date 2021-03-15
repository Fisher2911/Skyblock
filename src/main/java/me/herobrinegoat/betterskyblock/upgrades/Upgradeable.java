package me.herobrinegoat.betterskyblock.upgrades;

public interface Upgradeable {

    void upgrade();
    Upgrades getUpgrades();
    Upgrades.Upgrade getUpgrade(int level);

}
