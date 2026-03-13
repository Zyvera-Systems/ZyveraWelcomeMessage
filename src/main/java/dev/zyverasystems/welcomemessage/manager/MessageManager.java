package dev.zyverasystems.welcomemessage.manager;

import dev.zyverasystems.welcomemessage.ZyveraWelcomeMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MessageManager {

    private final ZyveraWelcomeMessage plugin;
    private final MiniMessage miniMessage;
    private final FireworkManager fireworkManager;

    // Speichert UUIDs von Spielern die noch nie gejoint sind
    private final Set<UUID> firstJoinCache = new HashSet<>();

    public MessageManager(ZyveraWelcomeMessage plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.fireworkManager = new FireworkManager(plugin);
    }

    public void reload() {
        firstJoinCache.clear();
    }

    /**
     * Konvertiert {placeholder} -> <placeholder>.
     * Die Config nutzt {player}, MiniMessage erwartet aber <player>.
     * Diese Methode sorgt dafuer, dass beide Formate funktionieren.
     */
    private String preprocess(String text) {
        return text
                .replace("{player}", "<player>")
                .replace("{displayname}", "<displayname>")
                .replace("{uuid}", "<uuid>")
                .replace("{world}", "<world>");
    }

    /**
     * Verarbeitet einen Spieler-Join vollstaendig.
     */
    public void handleJoin(Player player) {
        ConfigManager cfg = plugin.getConfigManager();
        String playerName = player.getName();
        String uuid = player.getUniqueId().toString();

        // Bypass-Permission check
        if (player.hasPermission(cfg.getBypassPermission())) return;

        boolean isFirstJoin = !player.hasPlayedBefore() || firstJoinCache.contains(player.getUniqueId());
        firstJoinCache.remove(player.getUniqueId());

        // Prioritaet: 1. Individuelle Nachricht, 2. First-Join, 3. Global
        ConfigurationSection activeSection;
        String messageType;

        if (cfg.isPlayerMessagesEnabled()) {
            ConfigurationSection playerSection = cfg.getPlayerSection(playerName, uuid);
            if (playerSection != null) {
                activeSection = playerSection;
                messageType = "player";
            } else if (isFirstJoin && cfg.isFirstJoinEnabled()) {
                activeSection = cfg.getFirstJoinSection();
                messageType = "first-join";
            } else if (cfg.isGlobalEnabled()) {
                activeSection = cfg.getGlobalSection();
                messageType = "global";
            } else {
                return;
            }
        } else if (isFirstJoin && cfg.isFirstJoinEnabled()) {
            activeSection = cfg.getFirstJoinSection();
            messageType = "first-join";
        } else if (cfg.isGlobalEnabled()) {
            activeSection = cfg.getGlobalSection();
            messageType = "global";
        } else {
            return;
        }

        if (cfg.isDebug()) {
            plugin.getLogger().info("[DEBUG] " + playerName + " -> Typ: " + messageType);
        }

        sendMessages(player, activeSection);
        fireworkManager.spawnFireworks(player, activeSection);
        playSound(player, activeSection);
    }

    /**
     * Sendet Join-Message (privat) und Broadcast-Message.
     */
    private void sendMessages(Player player, ConfigurationSection section) {
        if (section == null) return;

        TagResolver resolver = buildResolver(player);

        // Private Join-Message (nur der Spieler selbst)
        ConfigurationSection joinMsg = section.getConfigurationSection("join-message");
        if (joinMsg != null && joinMsg.getBoolean("enabled", true)) {
            String text = joinMsg.getString("text", "");
            if (!text.isEmpty()) {
                Component component = miniMessage.deserialize(preprocess(text), resolver);
                player.sendMessage(component);
            }
        }

        // Broadcast-Message (alle Spieler)
        ConfigurationSection broadcastMsg = section.getConfigurationSection("broadcast-message");
        if (broadcastMsg != null && broadcastMsg.getBoolean("enabled", true)) {
            String text = broadcastMsg.getString("text", "");
            if (!text.isEmpty()) {
                Component component = miniMessage.deserialize(preprocess(text), resolver);
                Bukkit.getServer().broadcast(component);
            }
        }
    }

    /**
     * Spielt einen Sound ab basierend auf der Config-Section.
     */
    private void playSound(Player player, ConfigurationSection section) {
        if (section == null) return;

        ConfigurationSection soundSection = section.getConfigurationSection("sound");
        if (soundSection == null || !soundSection.getBoolean("enabled", false)) return;

        String soundName = soundSection.getString("sound", "ENTITY_PLAYER_LEVELUP");
        float volume = (float) soundSection.getDouble("volume", 1.0);
        float pitch = (float) soundSection.getDouble("pitch", 1.0);
        boolean onlyForPlayer = soundSection.getBoolean("only-for-player", true);

        Sound sound;
        try {
            sound = Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Ungueltiger Sound in Config: " + soundName);
            return;
        }

        if (onlyForPlayer) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        } else {
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(player.getLocation(), sound, volume, pitch);
            }
        }
    }

    /**
     * Sendet eine Preview-Nachricht an einen Admin.
     */
    public void sendPreview(Player admin, ConfigurationSection section, String label) {
        if (section == null) {
            admin.sendMessage(miniMessage.deserialize(
                plugin.getConfigManager().getPrefix() + " <red>Keine Section gefunden: " + label));
            return;
        }
        TagResolver resolver = buildResolver(admin);
        admin.sendMessage(miniMessage.deserialize(
            plugin.getConfigManager().getPrefix() + " <gray>Preview fuer: <white>" + label));

        ConfigurationSection joinMsg = section.getConfigurationSection("join-message");
        if (joinMsg != null && joinMsg.getBoolean("enabled", true)) {
            String text = joinMsg.getString("text", "");
            if (!text.isEmpty()) {
                admin.sendMessage(miniMessage.deserialize(preprocess(text), resolver));
            }
        }
    }

    /**
     * Baut einen TagResolver mit Spieler-Platzhaltern.
     * Registriert <player>, <displayname>, <uuid>, <world>.
     */
    private TagResolver buildResolver(Player player) {
        return TagResolver.builder()
                .resolver(Placeholder.unparsed("player", player.getName()))
                .resolver(Placeholder.component("displayname", player.displayName()))
                .resolver(Placeholder.unparsed("uuid", player.getUniqueId().toString()))
                .resolver(Placeholder.unparsed("world", player.getWorld().getName()))
                .build();
    }

    /**
     * Direkte MiniMessage-Auflosung (z.B. fuer externe Nutzung).
     */
    public Component parse(String text, Player player) {
        return miniMessage.deserialize(preprocess(text), buildResolver(player));
    }

    public MiniMessage getMiniMessage() { return miniMessage; }
}
