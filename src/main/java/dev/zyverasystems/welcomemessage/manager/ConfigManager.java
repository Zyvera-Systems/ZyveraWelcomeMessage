package dev.zyverasystems.welcomemessage.manager;

import dev.zyverasystems.welcomemessage.ZyveraWelcomeMessage;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ConfigManager {

    private final ZyveraWelcomeMessage plugin;
    private FileConfiguration config;

    private final Map<String, ConfigurationSection> playerSections = new HashMap<>();

    public ConfigManager(ZyveraWelcomeMessage plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        this.config = plugin.getConfig();
        playerSections.clear();

        ConfigurationSection players = config.getConfigurationSection("player-messages.players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                ConfigurationSection section = players.getConfigurationSection(key);
                if (section != null) {
                    playerSections.put(key.toLowerCase(), section);
                }
            }
        }
    }

    public void reload() {
        load();
    }

    public String getPrefix() {
        return config.getString("prefix", "<dark_gray>[ZWM]</dark_gray>");
    }
    public boolean isDebug() {
        return config.getBoolean("settings.debug", false);
    }
    public long getMessageDelayTicks() {
        return config.getLong("settings.message-delay-ticks", 20L);
    }
    public String getBypassPermission() {
        return config.getString("settings.bypass-permission", "zyvera.welcomemessage.bypass");
    }

    public boolean isGlobalEnabled() {
        return config.getBoolean("global.enabled", true);
    }
    public ConfigurationSection getGlobalSection() {
        return config.getConfigurationSection("global");
    }
    public boolean isFirstJoinEnabled() {
        return config.getBoolean("first-join.enabled", true);
    }

    public ConfigurationSection getFirstJoinSection() {
        return config.getConfigurationSection("first-join");
    }

    public boolean isPlayerMessagesEnabled() {
        return config.getBoolean("player-messages.enabled", true);
    }

    /**
     * Gibt die individuelle Config-Section eines Spielers zurück.
     * Sucht erst nach Name, dann nach UUID.
     */
    public ConfigurationSection getPlayerSection(String playerName, String uuid) {
        ConfigurationSection byName = playerSections.get(playerName.toLowerCase());
        if (byName != null) return byName;
        return playerSections.get(uuid.toLowerCase());
    }

    public Set<String> getPlayerKeys() {
        return playerSections.keySet();
    }
}
