package me.herobrinegoat.betterskyblock.generators;

import me.herobrinegoat.betterskyblock.Menu;
import me.herobrinegoat.betterskyblock.Rankable;
import me.herobrinegoat.betterskyblock.upgrades.Upgradeable;
import me.herobrinegoat.betterskyblock.upgrades.Upgrades;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Generator implements Menu, Upgradeable, Generatable, Rankable {

    private int id;
    private int rank;
    private int level;
    private final ItemStack generatedItemStack;
    private int totalResources;
    private LocalDateTime lastGeneratedDate;
    private Upgrades upgrades;

    public Generator(int id, ItemStack generatedItemStack, int totalResources, LocalDateTime lastGeneratedDate, Upgrades upgrades, int rank, int level) {
        this.id = id;
        this.generatedItemStack = generatedItemStack;
        this.totalResources = totalResources;
        this.lastGeneratedDate = lastGeneratedDate;
        this.upgrades = upgrades;
        this.rank = rank;
        this.level = level;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ItemStack getGeneratedItemStack() {
        return generatedItemStack;
    }

    public int getTotalResources() {
        return totalResources;
    }

    public LocalDateTime getLastGeneratedDate() {
        return lastGeneratedDate;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setTotalResources(int resources, boolean resetTime) {
        if (resources > getMaxResources()) {
            if (resetTime) lastGeneratedDate = LocalDateTime.now();
            this.totalResources = getMaxResources();
            return;
        }
        if (resetTime) lastGeneratedDate = LocalDateTime.now();
        this.totalResources = resources;
    }

    @Override
    public Upgrades getUpgrades() {
        return upgrades;
    }

    private int getMaxLevel() {
        return upgrades.getMaxLevel();
    }

    public boolean isMaxLevel() {
        return getLevel() >= getMaxLevel();
    }

    public float getSpeed() {
        if (!hasUpgrades() || getCurrentUpgrade() == null) {
            return -1;
        }
        return getCurrentUpgrade().getSpeed();
    }

    public int getMaxResources() {
        if (!hasUpgrades() || getCurrentUpgrade() == null) {
            return -1;
        }
        return getCurrentUpgrade().getMaxItems();
    }


    public Upgrades.Upgrade getCurrentUpgrade() {
        if (hasUpgrades()) {
            return upgrades.getUpgrade(level);
        }
        return null;
    }

    private boolean hasUpgrades() {
        if (upgrades == null) return false;
        return upgrades.hasUpgrades(level);
    }

    public void generate() {
        if (!hasUpgrades()) {
            return;
        }
        float speed = getSpeed();
        if (speed == -1) {
            return;
        }
        if (lastGeneratedDate == null) {
            lastGeneratedDate = LocalDateTime.now();
            return;
        }
        Duration duration = Duration.between(lastGeneratedDate, LocalDateTime.now());
        int secondsPassed = (int) duration.getSeconds();
        if (secondsPassed < speed) {
            return;
        }
        setTotalResources((int) (totalResources + (secondsPassed / speed)), true);
    }

    public void setLastGeneratedDate(LocalDateTime lastGeneratedDate) {
        this.lastGeneratedDate = lastGeneratedDate;
    }

    public boolean isBroken() {
        return level < 1;
    }

    @Override
    public void upgrade() {
        if (!isMaxLevel()) {
            setLevel(getLevel() + 1);
        }
    }

    @Override
    public Upgrades.Upgrade getUpgrade(int level) {
        if (hasUpgrades()) {
            return upgrades.getUpgrade(level);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Generator)) return false;
        Generator generator = (Generator) o;
        return id == generator.id &&
                Objects.equals(generatedItemStack, generator.generatedItemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, generatedItemStack);
    }
}
