package me.call911.mobs;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;

import java.util.Optional;

public final class MythicUtil {

    private MythicUtil() {}

    /**
     * Retrieves the ActiveMob instance associated with a Bukkit Entity's current UUID.
     * Used by the PlayerProximityListener to determine if a scanned entity is a MythicMob.
     */
    public static ActiveMob getActiveMob(Entity entity) {
        Optional<ActiveMob> mob = MythicBukkit.inst()
                .getMobManager()
                .getActiveMob(entity.getUniqueId());

        return mob.orElse(null);
    }
}