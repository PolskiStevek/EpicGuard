package me.ishift.epicguard.bukkit;

import me.ishift.epicguard.bukkit.command.GuardCommand;
import me.ishift.epicguard.bukkit.gui.GuiMain;
import me.ishift.epicguard.bukkit.gui.GuiPlayers;
import me.ishift.epicguard.bukkit.listener.*;
import me.ishift.epicguard.bukkit.manager.DataFileManager;
import me.ishift.epicguard.bukkit.manager.FileManager;
import me.ishift.epicguard.bukkit.manager.UserManager;
import me.ishift.epicguard.bukkit.object.CustomFile;
import me.ishift.epicguard.bukkit.task.AttackTask;
import me.ishift.epicguard.bukkit.task.HeuristicsTask;
import me.ishift.epicguard.bukkit.task.InventoryTask;
import me.ishift.epicguard.bukkit.task.SaveTask;
import me.ishift.epicguard.bukkit.util.LogFilter;
import me.ishift.epicguard.bukkit.util.MessagesBukkit;
import me.ishift.epicguard.bukkit.util.Metrics;
import me.ishift.epicguard.bukkit.util.MiscUtil;
import me.ishift.epicguard.bukkit.util.nms.NMSUtil;
import me.ishift.epicguard.universal.Config;
import me.ishift.epicguard.universal.util.GeoAPI;
import me.ishift.epicguard.universal.util.Logger;
import me.ishift.epicguard.universal.util.LogoPrinter;
import me.ishift.epicguard.universal.util.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GuardBukkit extends JavaPlugin {
    public static final String PERMISSION = "epicguard.admin";

    public static GuardBukkit getInstance() {
        return JavaPlugin.getPlugin(GuardBukkit.class);
    }

    @Override
    public void onEnable() {
        try {
            final long ms = System.currentTimeMillis();
            this.saveDefaultConfig();
            this.createDirectories();
            new Logger(ServerType.SPIGOT);
            loadConfig();
            LogoPrinter.print();
            new GeoAPI(ServerType.SPIGOT);
            new Metrics(this);

            NMSUtil.init();
            new DataFileManager(this.getDataFolder() + "/data/data_flat.yml").load();
            DataFileManager.save();
            MessagesBukkit.load();

            this.registerListeners();
            this.registerTasks();

            GuiMain.eq = Bukkit.createInventory(null, 45, "EpicGuard Management Menu");
            GuiPlayers.inv = Bukkit.createInventory(null, 36, "EpicGuard Player Manager");

            this.getCommand("epicguard").setExecutor(new GuardCommand());
            this.registerBrand();

            new LogFilter(this.getDataFolder() + "/filter.yml").registerFilter();

            Bukkit.getOnlinePlayers().forEach(UserManager::addUser);
            Logger.info("Succesfully loaded! Took: " + (System.currentTimeMillis() - ms) + "ms");
        } catch (Exception e) {
            Logger.throwException(e);
        }
    }

    @Override
    public void onDisable() {
        try {
            Logger.info("Saving data and disabling plugin.");
            DataFileManager.save();
        } catch (Exception e) {
            Logger.throwException(e);
        }
    }

    private void registerListeners() {
        PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new PreLoginListener(), this);
        pm.registerEvents(new ServerListPingListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new InventoryClickListener(), this);
        pm.registerEvents(new PlayerCommandListener(), this);
        if (pm.isPluginEnabled("ProtocolLib")) {
            MiscUtil.registerProtocolLib(this);
        }
    }

    public static void loadConfig() {
        final FileConfiguration config = getInstance().getConfig();
        Config.FIREWALL = config.getBoolean("firewall");
        Config.FIREWALL_BL = config.getString("firewall.command-blacklist");
        Config.FIREWALL_WL = config.getString("firewall.command-whitelist");
        Config.CONNECT_SPEED = config.getInt("speed.connection");
        Config.PING_SPEED = config.getInt("speed.ping-speed");
        Config.AUTO_WHITELIST = config.getBoolean("auto-whitelist.enabled");
        Config.AUTO_WHITELIST_TIME = config.getInt("auto-whitelist.time");
        Config.ANTIBOT_QUERY_1 = config.getString("antibot.checkers.1.adress");
        Config.ANTIBOT_QUERY_2 = config.getString("antibot.checkers.2.adress");
        Config.ANTIBOT_QUERY_3 = config.getString("antibot.checkers.3.adress");
        Config.ANTIBOT_QUERY_CONTAINS = config.getStringList("antibot.checkers.responses");
        Config.COUNTRIES = config.getStringList("countries.list");
        Config.COUNTRY_MODE = config.getString("countries.mode");
        Config.ANTIBOT = config.getBoolean("antibot.enabled");
        Config.UPDATER = config.getBoolean("updater");
        Config.ATTACK_TIMER = config.getLong("speed.attack-timer-reset");
        Config.JOIN_SPEED = config.getInt("speed.join-speed");
        Config.TAB_COMPLETE_BLOCK = config.getBoolean("fully-block-tab-complete");
        Config.BLOCKED_COMMANDS = config.getStringList("command-protection.list");
        Config.ALLOWED_COMMANDS = config.getStringList("allowed-commands.list");
        Config.OP_PROTECTION_LIST = config.getStringList("op-protection.list");
        Config.OP_PROTECTION_ALERT = config.getString("op-protection.alert");
        Config.OP_PROTECTION_COMMAND = config.getString("op-protection.command");
        Config.ALLOWED_COMMANDS_BYPASS = config.getString("allowed-commands.bypass");
        Config.BLOCKED_COMMANDS_ENABLE = config.getBoolean("command-protection.enabled");
        Config.ALLOWED_COMMANDS_ENABLE = config.getBoolean("allowed-commands.enabled");
        Config.OP_PROTECTION_ENABLE = config.getBoolean("op-protection.enabled");
        Config.IP_HISTORY_ENABLE = config.getBoolean("ip-history.enabled");
        Config.FORCE_REJOIN = config.getBoolean("antibot.force-rejoin");
        Config.PEX_PROTECTION = config.getBoolean("op-protection.pex-protection");
        Config.NAME_CONTAINS = config.getStringList("antibot.name-contains");
    }

    private void registerTasks() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new InventoryTask(), 1L, 40L);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new HeuristicsTask(), 1L, 20L);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AttackTask(), 1L, Config.ATTACK_TIMER);
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new SaveTask(), 1L, 5000L);
    }

    private void createDirectories() {
        File cfg = new File(this.getDataFolder() + "/config.yml");
        File dir1 = new File(this.getDataFolder() + "/logs");
        if (!dir1.exists()) {
            dir1.mkdir();
        }
        File dir2 = new File(this.getDataFolder() + "/deprecated");
        if (!dir2.exists()) {
            cfg.renameTo(new File(dir2 + "/config.yml"));
            dir2.mkdir();
        }
        File dir3 = new File(this.getDataFolder() + "/data");
        if (!dir3.exists()) {
            dir3.mkdir();
        }
    }

    private void registerBrand() {
        FileManager.createFile(this.getDataFolder() + "/brand.yml");
        final CustomFile brandConfig = FileManager.getFile(this.getDataFolder() + "/brand.yml");
        if (!brandConfig.isExisting()) {
            List<String> blockedBrandDefault = new ArrayList<>();
            blockedBrandDefault.add("some_blocked_brand");
            brandConfig.getConfig().set("channel-verification.enabled", true);
            brandConfig.getConfig().set("channel-verification.punish", "kick {PLAYER} &cException occurred in your connection, please rejoin!");
            brandConfig.getConfig().set("blocked-brands.enabled", true);
            brandConfig.getConfig().set("blocked-brands.punish", "kick {PLAYER} &cYour client is not allowed on this server!");
            brandConfig.getConfig().set("blocked-brands.list", blockedBrandDefault);
            brandConfig.save();
        }

        if (NMSUtil.isOldVersion()) {
            final Messenger messenger = Bukkit.getMessenger();
            messenger.registerIncomingPluginChannel(this, "MC|Brand", new BrandPluginMessageListener());
        }
    }
}