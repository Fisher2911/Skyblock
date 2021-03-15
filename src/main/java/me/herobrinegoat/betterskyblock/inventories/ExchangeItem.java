package me.herobrinegoat.betterskyblock.inventories;

import me.herobrinegoat.menuapi.itemtypes.Button;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ExchangeItem extends Button {


    private ItemStack exchangedItem;
    private ItemStack gainedItem;
    private int reqMoney;
    private int reqItemAmount;
    private Type type;

    public ExchangeItem(ItemStack item, ItemStack exchangedItem, ItemStack gainedItem, int reqMoney, int reqItemAmount, Type type) {
        super(item);
        this.exchangedItem = exchangedItem;
        this.gainedItem = gainedItem;
        this.reqMoney = reqMoney;
        this.reqItemAmount = reqItemAmount;
        this.type = type;
    }


    public ExchangeItem(ItemStack exchangedItem, ItemStack gainedItem, int reqMoney, int reqItems, Type type) {
        super(exchangedItem);
        this.exchangedItem = exchangedItem;
        this.gainedItem = gainedItem;
        this.reqMoney = reqMoney;
        this.reqItemAmount = reqItems;
        this.type = type;
    }

    public ExchangeItem (ExchangeItem exchangeItem) {
        super(exchangeItem.getItemStack());
        this.exchangedItem = exchangeItem.getExchangedItem();
        this.gainedItem = exchangeItem.getGainedItem();
        this.reqMoney = exchangeItem.getReqMoney();
        this.reqItemAmount = exchangeItem.getReqItemAmount();
        this.type = exchangeItem.getType();
    }

    public ItemStack getExchangedItem() {
        return exchangedItem;
    }

    public ItemStack getGainedItem() {
        return gainedItem;
    }

    public int getReqMoney() {
        return reqMoney;
    }

    public int getReqItemAmount() {
        return reqItemAmount;
    }

    public void setReqMoney(int reqMoney) {
        this.reqMoney = reqMoney;
    }

    public void setReqItemAmount(int reqItems) {
        this.reqItemAmount = reqItems;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setExchangedItem(ItemStack exchangedItem) {
        this.exchangedItem = exchangedItem;
    }

    public void setGainedItem(ItemStack gainedItem) {
        this.gainedItem = gainedItem;
    }



    public enum Type {

        MOB_DROP,

        BLOCK,

        BLOCK_GENERATOR,

        MOB_GENERATOR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExchangeItem)) return false;
        ExchangeItem that = (ExchangeItem) o;
        return Objects.equals(exchangedItem, that.exchangedItem) &&
                Objects.equals(gainedItem, that.gainedItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(exchangedItem, gainedItem);
    }
}
