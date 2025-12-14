package me.call911.mobs.task;

import me.call911.mobs.hologram.NameManager;
import me.call911.mobs.player.InteractionManager;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProximityCheckTask extends BukkitRunnable {

    private final NameManager nameManager;
    private final InteractionManager interactionManager;

    private final int range;
    private final double rangeSquared;

    private final Map<UUID, UUID> currentTarget = new HashMap<>();

    private static final double SWITCH_MARGIN_SQUARED = 2.0 * 2.0;

    public ProximityCheckTask(
            NameManager nameManager,
            InteractionManager interactionManager,
            int range
    ) {
        this.nameManager = nameManager;
        this.interactionManager = interactionManager;
        this.range = range;
        this.rangeSquared = range * range;
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID playerUUID = player.getUniqueId();

            Entity nearestNpc = null;
            double nearestDistSq = rangeSquared;

            for (Entity entity : player.getNearbyEntities(range, range, range)) {

                if (!entity.isValid() || entity instanceof Player) {
                    continue;
                }

                ActiveMob activeMob = MythicBukkit.inst()
                        .getAPIHelper()
                        .getMythicMobInstance(entity);

                if (activeMob == null) {
                    continue;
                }

                String displayName = activeMob.getDisplayName();
                if (displayName == null || displayName.isEmpty()) {
                    continue;
                }

                String interactionKey = ChatColor.stripColor(
                        ChatColor.translateAlternateColorCodes('&', displayName)
                );

                if (interactionManager.hasInteracted(playerUUID, interactionKey)) {
                    continue;
                }

                double distSq = entity.getLocation().distanceSquared(player.getLocation());
                if (distSq <= nearestDistSq) {
                    nearestNpc = entity;
                    nearestDistSq = distSq;
                }
            }

            UUID currentNpcUUID = currentTarget.get(playerUUID);

            if (nearestNpc == null) {
                if (currentNpcUUID != null) {
                    nameManager.hide(player);
                    currentTarget.remove(playerUUID);
                }
                continue;
            }

            UUID newNpcUUID = nearestNpc.getUniqueId();

            if (currentNpcUUID != null && currentNpcUUID.equals(newNpcUUID)) {
                continue;
            }

            if (currentNpcUUID != null) {
                Entity currentEntity = Bukkit.getEntity(currentNpcUUID);
                if (currentEntity != null && currentEntity.isValid()) {
                    double currentDistSq =
                            currentEntity.getLocation().distanceSquared(player.getLocation());

                    if (currentDistSq - nearestDistSq < SWITCH_MARGIN_SQUARED) {
                        continue;
                    }
                }
            }

            nameManager.hide(player);
            nameManager.show(player, nearestNpc);
            currentTarget.put(playerUUID, newNpcUUID);
        }
    }
}
