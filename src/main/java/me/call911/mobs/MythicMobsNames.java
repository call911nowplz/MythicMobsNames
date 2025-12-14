package me.call911.mobs;

import me.call911.mobs.command.ReloadCommand;
import me.call911.mobs.database.DatabaseManager;
import me.call911.mobs.command.DebugCommand;
import me.call911.mobs.hologram.NameManager;
import me.call911.mobs.hologram.PacketArmorStandNameManager;
import me.call911.mobs.listener.PlayerConnectionListener;
import me.call911.mobs.listener.PlayerInteractionListener;
import me.call911.mobs.listener.PlayerProximityListener;
import me.call911.mobs.player.InteractionManager;
import me.call911.mobs.task.ProximityCheckTask;

import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class MythicMobsNames extends JavaPlugin {

    private NameManager nameManager;
    private DatabaseManager databaseManager;
    private InteractionManager interactionManager;

    @Override
    public void onEnable() {
        PluginManager pm = getServer().getPluginManager();

        saveDefaultConfig();
        int hologramRange = getConfig().getInt("hologram.range", 16);
        int hologramCheckInterval = getConfig().getInt("hologram.check-interval", 5);
        String hologramSymbol = getConfig().getString("hologram.symbol", "‼");

        if (pm.getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib not found! Disabling MythicMobsNames.");
            pm.disablePlugin(this);
            return;
        }

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to connect to MySQL. Disabling plugin.", e);
            pm.disablePlugin(this);
            return;
        }

        nameManager = new PacketArmorStandNameManager(this, hologramSymbol);
        interactionManager = new InteractionManager(this, databaseManager);

        getCommand("mmreload").setExecutor(new ReloadCommand(this));
        getCommand("mmdebug").setExecutor(new DebugCommand());
        getCommand("mmresetdata")
                .setExecutor(new me.call911.mobs.command.ResetCommand(interactionManager));

        pm.registerEvents(new PlayerProximityListener(nameManager), this);
        pm.registerEvents(new PlayerInteractionListener(interactionManager, nameManager), this);
        pm.registerEvents(new PlayerConnectionListener(interactionManager), this);

        new ProximityCheckTask(
                nameManager,
                interactionManager,
                hologramRange
        ).runTaskTimer(this, 0L, hologramCheckInterval);

        getLogger().info("MythicMobsNames loaded successfully.");
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);

        if (nameManager != null) {
            nameManager.cleanup();
        }

        if (databaseManager != null) {
            databaseManager.disconnect();
        }
    }
}
