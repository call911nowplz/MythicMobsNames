package me.call911.mobs.command;

import me.call911.mobs.player.InteractionManager;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResetCommand implements CommandExecutor {

    private static final String PERMISSION = "mythicmobsnames.admin";

    private final InteractionManager interactionManager;

    public ResetCommand(InteractionManager interactionManager) {
        this.interactionManager = interactionManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage("§cUsage: /mmresetdata <player>");
            return true;
        }

        String targetName = args[0];

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        if (target == null || target.getUniqueId() == null) {
            sender.sendMessage("§cPlayer '" + targetName + "' not found.");
            return true;
        }

        UUID targetUUID = target.getUniqueId();

        interactionManager.resetInteractionsAsync(targetUUID);

        sender.sendMessage("§aInteraction data reset initiated for §f" + targetName + "§a.");
        sender.sendMessage("§7(Database cleanup runs asynchronously)");

        if (target.isOnline() && target.getPlayer() != null) {
            sender.sendMessage("§e" + targetName + " is online. Holograms will reappear automatically.");
        }

        return true;
    }
}
