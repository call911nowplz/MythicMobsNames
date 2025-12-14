package me.call911.mobs.hologram;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PacketArmorStandNameManager implements NameManager {

    private final JavaPlugin plugin;
    private final String symbol;

    private final ProtocolManager protocol = ProtocolLibrary.getProtocolManager();

    private final Map<UUID, Integer> activeHolograms = new HashMap<>();
    private final Map<UUID, Integer> activeNpcs = new HashMap<>();

    public PacketArmorStandNameManager(JavaPlugin plugin, String symbol) {
        this.plugin = plugin;
        this.symbol = symbol;
    }

    @Override
    public void show(Player player, Entity npc) {
        if (npc == null || !npc.isValid()) {
            return;
        }

        hide(player);

        int hologramEntityId = ThreadLocalRandom.current().nextInt(1_000_000, 2_000_000);
        UUID hologramUUID = UUID.randomUUID();

        Location loc = npc.getLocation().add(0, npc.getHeight() + 0.3, 0);

        PacketContainer spawn = protocol.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
        spawn.getIntegers().write(0, hologramEntityId);
        spawn.getUUIDs().write(0, hologramUUID);
        spawn.getEntityTypeModifier().write(0, org.bukkit.entity.EntityType.ARMOR_STAND);
        spawn.getDoubles()
                .write(0, loc.getX())
                .write(1, loc.getY())
                .write(2, loc.getZ());

        protocol.sendServerPacket(player, spawn);

        activeHolograms.put(player.getUniqueId(), hologramEntityId);
        activeNpcs.put(player.getUniqueId(), npc.getEntityId());

        PacketContainer meta = protocol.createPacket(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, hologramEntityId);

        List<WrappedDataValue> data = new ArrayList<>();

        data.add(new WrappedDataValue(
                0,
                WrappedDataWatcher.Registry.get(Byte.class),
                (byte) 0x20
        ));

        data.add(new WrappedDataValue(
                3,
                WrappedDataWatcher.Registry.get(Boolean.class),
                true
        ));

        data.add(new WrappedDataValue(
                15,
                WrappedDataWatcher.Registry.get(Byte.class),
                (byte) 0x10
        ));

        Component text = resolveText();
        String json = GsonComponentSerializer.gson().serialize(text);
        Object componentHandle = WrappedChatComponent.fromJson(json).getHandle();

        data.add(new WrappedDataValue(
                2,
                WrappedDataWatcher.Registry.getChatComponentSerializer(true),
                Optional.of(componentHandle)
        ));

        meta.getDataValueCollectionModifier().write(0, data);
        protocol.sendServerPacket(player, meta);

        Bukkit.getScheduler().runTaskLater(
                plugin,
                () -> sendMountPacket(player, npc.getEntityId(), hologramEntityId),
                1L
        );
    }

    @Override
    public void hide(Player player) {
        UUID uuid = player.getUniqueId();

        Integer hologramId = activeHolograms.remove(uuid);
        Integer npcId = activeNpcs.remove(uuid);

        if (hologramId == null) {
            return;
        }

        if (npcId != null) {
            sendUnmountPacket(player, npcId);
        }

        PacketContainer destroy = protocol.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
        destroy.getIntLists().write(0, List.of(hologramId));
        protocol.sendServerPacket(player, destroy);
    }

    @Override
    public void cleanup() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            hide(player);
        }
        activeHolograms.clear();
        activeNpcs.clear();
    }

    private void sendMountPacket(Player player, int npcEntityId, int hologramEntityId) {
        PacketContainer mount = protocol.createPacket(PacketType.Play.Server.MOUNT);
        mount.getIntegers().write(0, npcEntityId);
        mount.getIntegerArrays().write(0, new int[]{hologramEntityId});
        protocol.sendServerPacket(player, mount);
    }

    private void sendUnmountPacket(Player player, int npcEntityId) {
        PacketContainer unmount = protocol.createPacket(PacketType.Play.Server.MOUNT);
        unmount.getIntegers().write(0, npcEntityId);
        unmount.getIntegerArrays().write(0, new int[0]);
        protocol.sendServerPacket(player, unmount);
    }

    private Component resolveText() {
        return Component.text(symbol);
    }

}
