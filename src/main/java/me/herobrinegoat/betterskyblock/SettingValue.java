package me.herobrinegoat.betterskyblock;

import java.util.Objects;

import static me.herobrinegoat.betterskyblock.Island.*;

public class SettingValue {
    private Island.Setting setting;
    private SettingType settingType;
    private boolean value;

    public SettingValue(Island.Setting setting, SettingType settingType, boolean value) {
        this.setting = setting;
        this.settingType = settingType;
        this.value = value;
    }

    public SettingType getSettingType() {
        return settingType;
    }

    public void setSettingType(SettingType settingType) {
        this.settingType = settingType;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public Island.Setting getSetting() {
        return setting;
    }

    public void setSetting(Island.Setting setting) {
        this.setting = setting;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingValue)) return false;
        SettingValue that = (SettingValue) o;
        return setting == that.setting &&
                settingType == that.settingType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(setting, settingType);
    }
}
