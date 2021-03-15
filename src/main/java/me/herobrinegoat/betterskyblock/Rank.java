package me.herobrinegoat.betterskyblock;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class Rank implements Comparable<Rank>{

    private ItemStack itemStack;

    private int rank;

    private int requirement;

    public Rank(ItemStack itemStack, int rank, int requirement) {
        this.itemStack = itemStack;
        this.rank = rank;
        this.requirement = requirement;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public int getRequirement() {
        return requirement;
    }

    public void setRequirement(int requirement) {
        this.requirement = requirement;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rank)) return false;
        Rank rank = (Rank) o;
        return Objects.equals(itemStack, rank.itemStack);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemStack);
    }

    @Override
    public int compareTo(Rank rank) {
        if (this.rank > rank.getRank()) {
            return 1;
        } else if (this.rank < rank.getRank()) {
            return -1;
        }
        return 0;
    }
}
