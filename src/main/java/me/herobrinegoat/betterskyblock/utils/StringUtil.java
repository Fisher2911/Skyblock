package me.herobrinegoat.betterskyblock.utils;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringUtil {


    public static String mapToString(Map<String, Object> map) {
        if (map == null) return null;
        StringBuilder builder = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            i++;
            if (i == 0) {
                builder.append("{");
            }
            String key = entry.getKey();
            Object value = entry.getValue();
            builder.append(key).append("=").append(value);
            if (i == map.size()) {
                builder.append("}");
            } else {
                builder.append(",");
            }
        }
        return builder.toString();
    }

    public static Map<String, Object> mapFromString(String convert) {
        if (convert == null) return null;
        HashMap<String, Object> map = new HashMap<>();
        int firstIndex = convert.indexOf("{");
        int secondIndex = convert.indexOf("}");
        convert = convert.substring(firstIndex +1, secondIndex);
        for (String s : convert.split(",")) {
            map.put(s.split("=")[0], s.split("=")[1]);
        }
        return map;
    }

    public static String collectionToString(Collection<String> collection, String splitter) {
        if (splitter == null) return null;
        StringBuilder stringList = new StringBuilder();
        int size = 0;
        for (String s : collection) {
            size++;
            if (size == 0) {
                stringList.append("{");
            }
            if (s.equalsIgnoreCase("")) s = " ";
            stringList.append(s);
            if (size != collection.size()) {
                stringList.append(splitter);
            } else {
                stringList.append("}");
            }
        }
        return stringList.toString();
    }

    public static Collection<String> collectionFromString(String string, Collection<String> type, String splitter) {
        if (string == null || type == null || splitter == null) return null;
        for (String s : string.split(splitter)) {
            if (s.charAt(0) == '{') s = s.substring(1, s.length());
            if (s.charAt(s.length()-1) == '}') s = s.substring(0, s.length() -1);
            type.add(ChatColor.translateAlternateColorCodes('&', s));
        }
        return type;
    }

    public static String dateTimeToString(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        if (dateTime == null) {
            return "NULL";
        }
        return dateTime.format(formatter);
    }

    public static LocalDateTime dateTimeFromString(String dateTimeString) {
        if (dateTimeString == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        if (!stringNotNull(dateTimeString)) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, formatter);
    }

    public static String locationToString(Location loc) {
        if (loc == null) return null;
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String world = "NULL";
        if (loc.getWorld() != null) {
            world = loc.getWorld().getName();
        }
        return "{-xStart{" + x + "}xEnd-" +
                "yStart{" + y + "}yEnd-" +
                "zStart{" + z + "}zEnd-" +
                "worldStart{" + world + "}worldEnd-}";
    }

    public static Location locationFromString(String loc) {
        if (loc == null) return null;

        loc = loc.substring(0, loc.length() - 1);

        String world = objFromObjString(loc, "world");
        String xStr = objFromObjString(loc, "x");
        String yStr = objFromObjString(loc, "y");
        String zStr = objFromObjString(loc, "z");

        int x = 0;
        int y = 0;
        int z = 0;

        if (xStr != null) x = Integer.parseInt(xStr);
        if (yStr != null) y = Integer.parseInt(yStr);
        if (zStr != null) z = Integer.parseInt(zStr);

        if (world == null) {
            return null;
        }

        return new Location(Bukkit.getWorld(world), x, y, z);
    }

    public static String objFromObjString(String string, String test) {
        if (string == null || test == null) return null;
        if (!string.contains("-" + test + "Start") || !string.contains(test + "End" + "-")) {
            return null;
        }
        String sub = StringUtils.substringBetween(string, "-" + test + "Start", test + "End" + "-");
        return sub.substring(1, sub.length() -1);
    }

    public static boolean stringNotNull(String s) {
        return  (s != null && !s.equalsIgnoreCase("NULL"));
    }

    public static void addItemWithInfoToList(List<String> list, String key, String value, String separatorColor) {
        if (list == null || key == null || value == null || separatorColor == null) return;
        list.add(key + separatorColor + " - " + ChatColor.RESET + value);
    }
}
