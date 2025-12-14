package me.call911.mobs.player;

import me.call911.mobs.MythicMobsNames;
import me.call911.mobs.database.DatabaseManager;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class InteractionManager {

    private final MythicMobsNames plugin;
    private final DatabaseManager databaseManager;

    private final Map<UUID, Set<String>> interactedNpcsCache = new HashMap<>();

    public InteractionManager(MythicMobsNames plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }


    public void recordInteraction(UUID playerUUID, String npcCustomName) {
        interactedNpcsCache.computeIfAbsent(playerUUID, k -> Collections.synchronizedSet(new HashSet<>()))
                .add(npcCustomName);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String table = databaseManager.getTableName();
            String query = "INSERT INTO " + table + " (player_uuid, mob_name) VALUES (?, ?) ON DUPLICATE KEY UPDATE mob_name=mob_name";

            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, playerUUID.toString());
                ps.setString(2, npcCustomName);
                ps.executeUpdate();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error recording interaction for " + playerUUID, e);
            }
        });
    }

    public boolean hasInteracted(UUID playerUUID, String npcCustomName) {
        Set<String> interactions = interactedNpcsCache.get(playerUUID);


        if (interactions == null) {
            return false;
        }

        return interactions.contains(npcCustomName);
    }


    public void loadInteractionsAsync(UUID playerUUID) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Set<String> mobNames = loadInteractionsSync(playerUUID);
                interactedNpcsCache.put(playerUUID, mobNames);
                plugin.getLogger().info("Loaded " + mobNames.size() + " interactions for " + playerUUID);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error loading interactions for " + playerUUID, e);
                // If loading fails, put an empty set to prevent constant reloading attempts
                interactedNpcsCache.put(playerUUID, Collections.synchronizedSet(new HashSet<>()));
            }
        });
    }

    private Set<String> loadInteractionsSync(UUID playerUUID) throws SQLException {
        Set<String> mobNames = Collections.synchronizedSet(new HashSet<>());
        String table = databaseManager.getTableName();
        String query = "SELECT mob_name FROM " + table + " WHERE player_uuid = ?";

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {

            ps.setString(1, playerUUID.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    mobNames.add(rs.getString("mob_name"));
                }
            }
        }
        return mobNames;
    }

    public void unloadInteractions(UUID playerUUID) {
        interactedNpcsCache.remove(playerUUID);
    }

    /**
     * Resets a player's interaction state by clearing the cache and deleting
     * records from the MySQL database asynchronously.
     * * @param playerUUID The UUID of the player whose data should be reset.
     */
    public void resetInteractionsAsync(UUID playerUUID) {
        interactedNpcsCache.remove(playerUUID);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String table = databaseManager.getTableName();
            String query = "DELETE FROM " + table + " WHERE player_uuid = ?";

            try (Connection conn = databaseManager.getConnection();
                 PreparedStatement ps = conn.prepareStatement(query)) {

                ps.setString(1, playerUUID.toString());
                int deletedRows = ps.executeUpdate();

                plugin.getLogger().info("Reset interactions for player " + playerUUID + ". Deleted " + deletedRows + " records from MySQL.");

            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "Error resetting interactions for " + playerUUID, e);
            }
        });
    }
}