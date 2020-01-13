package me.ishift.epicguard.bukkit.object;

import org.bukkit.entity.Player;

import java.util.List;

public class User {
    private List<String> adresses;
    private boolean notifications;
    private String brand;
    private String ip;
    private Player player;

    public User(Player player) {
        this.player = player;
        this.notifications = false;
        this.brand = "none";
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public boolean isNotifications() {
        return notifications;
    }

    public void setNotifications(boolean notifications) {
        this.notifications = notifications;
    }

    public List<String> getAdresses() {
        return adresses;
    }

    public void setAdresses(List<String> adresses) {
        this.adresses = adresses;
    }
}