package me.call911.mobs.listener;

import me.call911.mobs.hologram.NameManager;
import me.call911.mobs.player.InteractionManager;
import io.lumine.mythic.bukkit.events.MythicMobInteractEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerInteractionListener implements Listener {

    private final InteractionManager interactionManager;
    private final NameManager nameManager;

    public PlayerInteractionListener(InteractionManager interactionManager, NameManager nameManager) {
        this.interactionManager = interactionManager;
        this.nameManager = nameManager;
    }

    @EventHandler
    public void onMythicMobInteract(MythicMobInteractEvent event) {
        Player player = event.getPlayer();

        String displayName = event.getActiveMob().getDisplayName();
        if (displayName == null || displayName.isEmpty()) {
            return;
        }

        interactionManager.recordInteraction(player.getUniqueId(), displayName);

        nameManager.hide(player);
    }
}
