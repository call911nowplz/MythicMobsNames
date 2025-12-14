package me.call911.mobs.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.call911.mobs.MythicMobsNames;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {

    private final MythicMobsNames plugin;
    private HikariDataSource dataSource;


    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private String tableName;

    public DatabaseManager(MythicMobsNames plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        this.host = config.getString("database.host", "localhost");
        this.port = config.getInt("database.port", 3306);
        this.database = config.getString("database.database", "minecraft_db");
        this.username = config.getString("database.username", "user");
        this.password = config.getString("database.password", "password123");
        this.tableName = config.getString("database.table_name", "mm_interactions");
    }

    public void connect() throws SQLException {
        plugin.getLogger().info("Attempting to connect to MySQL via HikariCP...");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?autoReconnect=true&useSSL=false");
        config.setUsername(username);
        config.setPassword(password);

        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        config.setPoolName("MythicMobsNames-Pool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(5);
        config.setMaxLifetime(1800000); // 30 minutes
        config.setConnectionTimeout(5000);

        try {
            this.dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Successfully initialized HikariCP connection pool!");
            createTable();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not initialize HikariCP connection pool.", e);
            throw new SQLException("Failed to connect via HikariCP.", e);
        }
    }

    public void disconnect() {
        if (dataSource != null) {
            dataSource.close();
            plugin.getLogger().info("Disconnected HikariCP connection pool.");
        }
    }

    private void createTable() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS " + tableName + " (" + "player_uuid VARCHAR(36) NOT NULL," + "mob_name VARCHAR(255) NOT NULL," + "PRIMARY KEY (player_uuid, mob_name)" + ");";
        try (Connection conn = dataSource.getConnection(); PreparedStatement ps = conn.prepareStatement(query)) {
            ps.executeUpdate();
            plugin.getLogger().info("MySQL table '" + tableName + "' ensured.");
        }
    }

    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("Database connection pool is not initialized.");
        }
        return dataSource.getConnection();
    }

    public String getTableName() {
        return tableName;
    }
}