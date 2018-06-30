package me.rayzr522.tpclevel.command;

import me.rayzr522.tpclevel.TpcLevel;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {
    private final TpcLevel plugin;

    public CommandHandler(TpcLevel plugin) {
        this.plugin = plugin;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        String resolved = String.format("%s.%s", plugin.getName(), permission);
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(plugin.tr("error.no-permission", resolved));
            return false;
        }
        return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!checkPermission(sender, "command.use")) {
            return true;
        }

        if (args.length < 1) {
            showUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (!checkPermission(sender, "command.reload")) {
                    return true;
                }

                plugin.reload();
                sender.sendMessage(plugin.tr("command.reload.reloaded"));

                return true;
            case "global":
                if (!checkPermission(sender, "command.global")) {
                    return true;
                }

                sender.sendMessage(plugin.tr("command.global.recalculating"));

                Bukkit.getOnlinePlayers().forEach(plugin::recalculateLevel);

                sender.sendMessage(plugin.tr("command.global.complete", Bukkit.getOnlinePlayers().size()));

                return true;
            case "self":
                if (!checkPermission(sender, "command.self")) {
                    return true;
                }

                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.tr("error.only-players"));
                    return true;
                }

                Player player = (Player) sender;
                plugin.recalculateLevel(player);

                sender.sendMessage(plugin.tr("command.self.calculated", plugin.getPlayerLevel(player.getUniqueId())));

                return true;
            default:
                showUsage(sender);

                return true;
        }
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage(plugin.trRaw("command.help"));
    }
}