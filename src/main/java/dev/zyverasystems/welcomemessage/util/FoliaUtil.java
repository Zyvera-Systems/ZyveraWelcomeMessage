package dev.zyverasystems.welcomemessage.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.function.Consumer;

/**
 * Utility-Klasse für Folia-Kompatibilität.
 * Folia verwendet regionsbasierte Scheduler statt des globalen Bukkit-Schedulers.
 */
public class FoliaUtil {

    private static final boolean IS_FOLIA;

    static {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        IS_FOLIA = folia;
    }

    public static boolean isFolia() {
        return IS_FOLIA;
    }

    /**
     * Führt eine Aufgabe nach einer Verzögerung aus.
     * Kompatibel mit Folia (RegionScheduler) und Bukkit/Paper (BukkitScheduler).
     */
    public static void runLater(Plugin plugin, Location location, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getRegionScheduler().runDelayed(plugin, location, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }

    /**
     * Führt eine Aufgabe für einen Player aus (Folia: EntityScheduler).
     * Nutzt Player statt Entity damit isOnline() und Player-Methoden verfügbar sind.
     */
    public static void runForPlayer(Plugin plugin, Player player, Consumer<Player> task, long delayTicks) {
        if (IS_FOLIA) {
            // Folia: EntityScheduler ist an die Entity gebunden
            player.getScheduler().runDelayed(plugin, scheduledTask -> {
                // Im Folia-Callback ist der Player evtl. nicht mehr online
                if (player.isOnline()) {
                    task.accept(player);
                }
            }, null, Math.max(1, delayTicks));
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    task.accept(player);
                }
            }, Math.max(1, delayTicks));
        }
    }

    /**
     * Führt eine globale (nicht positionsgebundene) Aufgabe aus.
     */
    public static void runGlobal(Plugin plugin, Runnable task, long delayTicks) {
        if (IS_FOLIA) {
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> task.run(), delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delayTicks);
        }
    }
}
