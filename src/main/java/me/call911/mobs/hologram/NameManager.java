package me.call911.mobs.hologram;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public interface NameManager {

    /**
     * Show the NPC name to a specific player.
     */
    void show(Player player, Entity npc);

    /**
     * Hide the NPC name from a specific player.
     */
    void hide(Player player);

    /**
     * Cleanup all active name entities (called on plugin disable).
     */
    void cleanup();
}
