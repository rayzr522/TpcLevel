package me.rayzr522.tpclevel.event;

import me.rayzr522.tpclevel.TpcLevel;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener {
    private final TpcLevel plugin;

    public PlayerListener(TpcLevel plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("chat-tag.enabled")) {
            return;
        }

        if (e.getPlayer() == null) {
            return;
        }

        Player player = e.getPlayer();
        String format = plugin.getConfig().getString("chat-tag.format");
        int level = plugin.getPlayerLevel(player.getUniqueId());

        String prefix = ChatColor.translateAlternateColorCodes('&', format)
                .replaceAll("\\{LEVEL}", String.valueOf(level));

        e.setFormat(prefix + e.getFormat());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (!plugin.getConfig().getBoolean("events.player-join")) {
            return;
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.recalculateLevel(e.getPlayer()), 60L);
    }

    @EventHandler
    public void onPickupItem(EntityPickupItemEvent e) {
        if (!plugin.getConfig().getBoolean("events.item-pickup")) {
            return;
        }

        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        if (plugin.calculateItemLevel(e.getItem().getItemStack()) > 0) {
            plugin.queueForUpdate((Player) e.getEntity());
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        if (!plugin.getConfig().getBoolean("events.item-drop")) {
            return;
        }

        if (plugin.calculateItemLevel(e.getItemDrop().getItemStack()) > 0) {
            plugin.queueForUpdate(e.getPlayer());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getSlotType() != InventoryType.SlotType.ARMOR) {
            return;
        }

        if (plugin.calculateItemLevel(e.getCurrentItem()) > 0 || plugin.calculateItemLevel(e.getCursor()) > 0) {
            plugin.queueForUpdate((Player) e.getWhoClicked());
        }
    }
}