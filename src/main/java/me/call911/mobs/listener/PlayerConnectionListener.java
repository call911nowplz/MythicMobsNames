package me.call911.mobs.listener;

import me.call911.mobs.player.InteractionManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    private final InteractionManager interactionManager;

    public PlayerConnectionListener(InteractionManager interactionManager) {
        this.interactionManager = interactionManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        interactionManager.loadInteractionsAsync(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        interactionManager.unloadInteractions(event.getPlayer().getUniqueId());
    }
}