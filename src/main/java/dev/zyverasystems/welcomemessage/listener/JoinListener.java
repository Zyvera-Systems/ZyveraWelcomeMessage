package dev.zyverasystems.welcomemessage.listener;

import dev.zyverasystems.welcomemessage.ZyveraWelcomeMessage;
import dev.zyverasystems.welcomemessage.util.FoliaUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    private final ZyveraWelcomeMessage plugin;

    public JoinListener(ZyveraWelcomeMessage plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        long delay = plugin.getConfigManager().getMessageDelayTicks();

        FoliaUtil.runForEntity(plugin, event.getPlayer(), (player) -> {
            if (!player.isOnline()) return;
            plugin.getMessageManager().handleJoin((Player) player);
        }, Math.max(1, delay));
    }
}
