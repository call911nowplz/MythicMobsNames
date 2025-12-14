package me.call911.mobs.listener;

import me.call911.mobs.hologram.NameManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerProximityListener implements Listener {

    private final NameManager nameManager;

    public PlayerProximityListener(NameManager nameManager) {
        this.nameManager = nameManager;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        nameManager.hide(event.getPlayer());
    }
}
