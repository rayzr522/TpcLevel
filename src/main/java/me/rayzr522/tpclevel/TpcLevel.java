package me.rayzr522.tpclevel;

import com.nametagedit.plugin.NametagEdit;
import me.rayzr522.tpclevel.command.CommandHandler;
import me.rayzr522.tpclevel.event.PlayerListener;
import me.rayzr522.tpclevel.util.Localization;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TpcLevel extends JavaPlugin {
    private final Localization localization = new Localization();
    private final Map<UUID, Integer> levels = new HashMap<>();
    private final List<UUID> playersToUpdate = new ArrayList<>();

    private String levelLorePrefix;

    @Override
    public void onEnable() {
        reload();

        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        getCommand("level").setExecutor(new CommandHandler(this));

        recalculateGlobal();
    }

    public void reload() {
        saveDefaultConfig();
        reloadConfig();

        localization.load(getConfig("messages.yml"));
        levelLorePrefix = getConfig().getString("level-lore-prefix");
    }

    private YamlConfiguration getConfig(String path) {
        File file = getFile(path);
        if (!file.exists() && getResource(path) != null) {
            saveResource(path, false);
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private File getFile(String path) {
        return new File(getDataFolder(), path);
    }

    public void recalculateGlobal() {
        Bukkit.getOnlinePlayers().forEach(this::recalculateLevel);
    }

    public void recalculateLevel(Player player) {
        int total = 0;
        int count = 0;

        int highestLevelItem = Arrays.stream(player.getInventory().getContents())
                .filter(Objects::nonNull)
                .filter(item -> item.getType() == Material.DIAMOND_SWORD)
                .mapToInt(this::calculateItemLevel)
                .filter(level -> level >= 0)
                .max()
                .orElse(0);

        total += highestLevelItem;
        if (highestLevelItem > 0) {
            count++;
        }

        List<Integer> armorLevels = Arrays.stream(player.getEquipment().getArmorContents())
                .map(this::calculateItemLevel)
                .filter(level -> level > 0)
                .collect(Collectors.toList());

        count += armorLevels.size();
        total += armorLevels.stream().mapToInt(level -> level).sum();

        int average = count > 0 ? total / count : 0;

        levels.put(player.getUniqueId(), average);

        ConfigurationSection levelsSection = getConfig().getConfigurationSection("levels");

        String levelSuffix = levelsSection.getKeys(false).stream()
                .filter(levelsSection::isConfigurationSection)
                .map(levelsSection::getConfigurationSection)
                .filter(section -> average >= section.getInt("min") && average <= section.getInt("max"))
                .findFirst()
                .map(section -> ChatColor.translateAlternateColorCodes('&', section.getString("suffix")))
                .orElse("");

        String nametagFormat = ChatColor.translateAlternateColorCodes('&', getConfig().getString("format"))
                .replaceAll("\\{LEVEL}", String.valueOf(average));

        if (count >= 5) {
            nametagFormat += levelSuffix;
        }

        NametagEdit.getApi().setNametag(player, NametagEdit.getApi().getNametag(player).getPrefix(), nametagFormat);
    }

    public void queueForUpdate(Player p) {
        if (playersToUpdate.size() < 1) {
            Bukkit.getScheduler().runTaskLater(this, () -> {
                playersToUpdate.stream()
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .forEach(this::recalculateLevel);

                playersToUpdate.clear();
            }, 120L);
        }

        if (!playersToUpdate.contains(p.getUniqueId())) {
            playersToUpdate.add(p.getUniqueId());
        }
    }

    public int calculateItemLevel(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.getLore().size() < 1) {
            return 0;
        }

        String firstLine = meta.getLore().get(0);

        if (!firstLine.startsWith(levelLorePrefix) || firstLine.contains(ChatColor.RED.toString())) {
            return 0;
        }

        return Integer.parseInt(firstLine.substring(levelLorePrefix.length()));
    }

    public int getPlayerLevel(UUID id) {
        return levels.getOrDefault(id, 0);
    }

    public String tr(String key, Object... formattingArgs) {
        return localization.tr(key, formattingArgs);
    }

    public String trRaw(String key, Object... formattingArgs) {
        return localization.trRaw(key, formattingArgs);
    }
}
