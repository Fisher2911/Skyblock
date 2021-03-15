package me.herobrinegoat.betterskyblock.api;

import me.herobrinegoat.betterskyblock.User;
import me.herobrinegoat.betterskyblock.generators.MobGenerator;

public class GeneratorMobKillEvent extends GeneratorEvent {

    private final MobGenerator mobGenerator;
    private final User user;

    public GeneratorMobKillEvent(MobGenerator mobGenerator, User user) {
        this.mobGenerator = mobGenerator;
        this.user = user;
    }

    @Override
    public MobGenerator getGenerator() {
        return mobGenerator;
    }

    @Override
    public User getUser() {
        return user;
    }

}
