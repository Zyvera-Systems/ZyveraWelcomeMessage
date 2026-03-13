package dev.zyverasystems.welcomemessage.manager;

import dev.zyverasystems.welcomemessage.ZyveraWelcomeMessage;
import dev.zyverasystems.welcomemessage.util.FoliaUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.ArrayList;
import java.util.List;

public class FireworkManager {

    private final ZyveraWelcomeMessage plugin;

    public FireworkManager(ZyveraWelcomeMessage plugin) {
        this.plugin = plugin;
    }

    /**
     * Spawnt Feuerwerk basierend auf einer Config-Section.
     * Erwartet: fireworks.enabled, fireworks.count, fireworks.type, fireworks.colors, etc.
     */
    public void spawnFireworks(Player player, ConfigurationSection section) {
        if (section == null) return;

        ConfigurationSection fw = section.getConfigurationSection("fireworks");
        if (fw == null || !fw.getBoolean("enabled", false)) return;

        int count = fw.getInt("count", 1);
        long delayTicks = fw.getLong("delay-ticks", 10L);
        String typeStr = fw.getString("type", "BALL");
        boolean flicker = fw.getBoolean("flicker", false);
        boolean trail = fw.getBoolean("trail", false);

        FireworkEffect.Type type;
        try {
            type = FireworkEffect.Type.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            type = FireworkEffect.Type.BALL;
        }

        List<Color> colors = parseColors(fw.getStringList("colors"));
        List<Color> fadeColors = parseColors(fw.getStringList("fade-colors"));

        if (colors.isEmpty()) colors.add(Color.fromRGB(168, 85, 247));

        FireworkEffect.Type finalType = type;
        List<Color> finalColors = colors;
        List<Color> finalFadeColors = fadeColors;

        for (int i = 0; i < count; i++) {
            final long delay = i * delayTicks;
            FoliaUtil.runForPlayer(plugin, player, (p) -> {
                spawnSingleFirework(p.getLocation(), finalType, finalColors, finalFadeColors, flicker, trail);
            }, delay == 0 ? 1 : delay);
        }
    }

    private void spawnSingleFirework(Location location, FireworkEffect.Type type,
                                     List<Color> colors, List<Color> fadeColors,
                                     boolean flicker, boolean trail) {
        Firework fw = location.getWorld().spawn(location, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();

        FireworkEffect.Builder builder = FireworkEffect.builder()
                .with(type)
                .withColor(colors)
                .flicker(flicker)
                .trail(trail);

        if (!fadeColors.isEmpty()) {
            builder.withFade(fadeColors);
        }

        meta.addEffect(builder.build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    private List<Color> parseColors(List<String> hexList) {
        List<Color> colors = new ArrayList<>();
        for (String hex : hexList) {
            try {
                String clean = hex.replace("#", "").trim();
                int r = Integer.parseInt(clean.substring(0, 2), 16);
                int g = Integer.parseInt(clean.substring(2, 4), 16);
                int b = Integer.parseInt(clean.substring(4, 6), 16);
                colors.add(Color.fromRGB(r, g, b));
            } catch (Exception ignored) {
                // Ungültige Farbe überspringen
            }
        }
        return colors;
    }
}
