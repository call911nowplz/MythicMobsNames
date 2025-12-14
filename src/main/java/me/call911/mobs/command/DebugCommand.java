package me.call911.mobs.command;

import me.call911.mobs.MythicUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {

    private static final String PERMISSION = "mythicmobsnames.admin";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Must be a player.");
            return true;
        }

        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        player.sendMessage("§7Running §b/mmdebug§7...");

        int count = 0;

        for (Entity e : player.getNearbyEntities(16, 16, 16)) {
            count++;

            boolean isMythic = MythicUtil.getActiveMob(e) != null;

            player.sendMessage(
                    "§8- §7" + e.getType()
                            + " §8| §7" + e.getUniqueId()
                            + " §8| §7mythic=" + isMythic
            );

            if (isMythic) {
                String name = e.getName();
                player.sendMessage(
                        "   §aHologram SHOULD spawn saying: §f\"" + name + "\""
                );
            }
        }

        player.sendMessage("§7Entities found: §f" + count);
        return true;
    }
}
