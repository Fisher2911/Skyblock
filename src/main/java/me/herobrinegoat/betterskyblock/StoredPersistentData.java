package me.herobrinegoat.betterskyblock;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.util.Objects;

public class StoredPersistentData {

    private NamespacedKey key;
    private PersistentDataType persistentDataType;
    private Object value;

    public StoredPersistentData(NamespacedKey key, PersistentDataType persistentDataType, Object value) {
        this.key = key;
        this.persistentDataType = persistentDataType;
        this.value = value;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public void setKey(NamespacedKey key) {
        this.key = key;
    }

    public PersistentDataType getPersistentDataType() {
        return persistentDataType;
    }

    public void setPersistentDataType(PersistentDataType persistentDataType) {
        this.persistentDataType = persistentDataType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredPersistentData)) return false;
        StoredPersistentData that = (StoredPersistentData) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(persistentDataType, that.persistentDataType) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, persistentDataType, value);
    }

    public boolean isSimilar(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredPersistentData)) return false;
        StoredPersistentData that = (StoredPersistentData) o;
        return Objects.equals(key, that.key) &&
                Objects.equals(persistentDataType, that.persistentDataType);
    }
}
