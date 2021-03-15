package me.herobrinegoat.betterskyblock.api;

import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.Generator;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class GeneratorEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public abstract Generator getGenerator();
    public abstract User getUser();
}
