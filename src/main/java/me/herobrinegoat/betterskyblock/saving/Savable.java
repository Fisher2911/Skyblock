package me.herobrinegoat.betterskyblock.saving;

public interface Savable {

    void save(ConnectionPool pool);
    void delete(ConnectionPool pool);

}
