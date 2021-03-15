package me.herobrinegoat.betterskyblock.api;

import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.BlockGenerator;

public class GeneratorBlockBreakEvent extends GeneratorEvent {

    private final BlockGenerator blockGenerator;
    private final User user;

    public GeneratorBlockBreakEvent(BlockGenerator blockGenerator, User user) {
        this.blockGenerator = blockGenerator;
        this.user = user;
    }

    @Override
    public BlockGenerator getGenerator() {
        return blockGenerator;
    }

    @Override
    public User getUser() {
        return user;
    }
}
