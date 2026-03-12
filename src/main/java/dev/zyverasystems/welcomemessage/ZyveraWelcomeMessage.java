package dev.zyverasystems.welcomemessage;

import dev.zyverasystems.welcomemessage.command.WelcomeCommand;
import dev.zyverasystems.welcomemessage.listener.JoinListener;
import dev.zyverasystems.welcomemessage.manager.ConfigManager;
import dev.zyverasystems.welcomemessage.manager.MessageManager;
import dev.zyverasystems.welcomemessage.util.FoliaUtil;
import org.bukkit.plugin.java.JavaPlugin;

public class ZyveraWelcomeMessage extends JavaPlugin {

    private static ZyveraWelcomeMessage instance;
    private ConfigManager configManager;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        instance = this;

        // Config laden
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        // Command registrieren
        var cmd = getCommand("zwm");
        if (cmd != null) {
            var handler = new WelcomeCommand(this);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        // Startup Log
        String folia = FoliaUtil.isFolia() ? " §a[Folia-kompatibel]" : "";
        getLogger().info("══════════════════════════════════════");
        getLogger().info("  ZyveraWelcomeMessage v" + getDescription().getVersion() + " gestartet!" + folia);
        getLogger().info("  Gradient-Support: Paper 1.16+ ✓");
        getLogger().info("══════════════════════════════════════");
    }

    @Override
    public void onDisable() {
        getLogger().info("ZyveraWelcomeMessage wurde deaktiviert.");
    }

    public void reload() {
        reloadConfig();
        configManager.reload();
        messageManager.reload();
    }

    public static ZyveraWelcomeMessage getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public MessageManager getMessageManager() { return messageManager; }
}
