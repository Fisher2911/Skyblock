package me.herobrinegoat.betterskyblock.utils;

public class CommandUtil {

    public boolean isLengthAndArg(int length, String check, String... args) {
        return (args.length == length && args[0].equalsIgnoreCase(check));
    }

    public boolean isArgAndGreaterThan(int length, String check, String... args) {
        return (args.length > length && args[0].equalsIgnoreCase(check));
    }
}
