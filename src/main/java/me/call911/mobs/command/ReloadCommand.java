package me.call911.mobs.command;

import me.call911.mobs.MythicMobsNames;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private static final String PERMISSION = "mythicmobsnames.admin";

    private final MythicMobsNames plugin;

    public ReloadCommand(MythicMobsNames plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        plugin.reloadConfig();

        sender.sendMessage("§aMythicMobsNames configuration reloaded.");

        sender.sendMessage("§7Note: Active holograms will update automatically.");

        return true;
    }
}
