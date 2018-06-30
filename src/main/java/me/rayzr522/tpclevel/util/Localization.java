package me.rayzr522.tpclevel.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Localization {
    private Map<String, String> messages = new HashMap<>();

    private static String baseKeyOf(String key) {
        return key.indexOf('.') < 0 ? "" : key.substring(0, key.lastIndexOf('.'));
    }

    public void load(ConfigurationSection config) {
        messages = config.getKeys(true).stream()
                .collect(Collectors.toMap(key -> key, key -> Objects.toString(config.get(key))));
    }

    private String getPrefixFor(String key) {
        String base = baseKeyOf(key);
        String prefix = messages.getOrDefault(base + ".prefix", messages.getOrDefault("prefix", ""));
        String prefixAddon = messages.getOrDefault(base + ".prefix-addon", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + prefixAddon);
    }

    public String trRaw(String key, Object... formattingArgs) {
        String output = messages.getOrDefault(key, key);

        for (int i = 0; i < formattingArgs.length; i++) {
            output = output.replace("{" + i + "}", Objects.toString(formattingArgs[i]));
        }

        return ChatColor.translateAlternateColorCodes('&', output);
    }

    public String tr(String key, Object... formattingArgs) {
        return getPrefixFor(key) + trRaw(key, formattingArgs);
    }
}
