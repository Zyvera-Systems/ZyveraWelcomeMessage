package dev.zyverasystems.welcomemessage.command;

import dev.zyverasystems.welcomemessage.ZyveraWelcomeMessage;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WelcomeCommand implements CommandExecutor, TabCompleter {

    private final ZyveraWelcomeMessage plugin;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public WelcomeCommand(ZyveraWelcomeMessage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        String prefix = plugin.getConfigManager().getPrefix();

        if (!sender.hasPermission("zyvera.welcomemessage.admin")) {
            sender.sendMessage(mm.deserialize(prefix + " <red>Keine Berechtigung!"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, prefix, label);
            return true;
        }

        switch (args[0].toLowerCase()) {

            case "reload" -> {
                plugin.reload();
                sender.sendMessage(mm.deserialize(prefix + " <green>Konfiguration erfolgreich neu geladen!"));
            }

            case "preview" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(mm.deserialize(prefix + " <red>Dieser Befehl ist nur für Spieler!"));
                    return true;
                }

                String type = args.length > 1 ? args[1].toLowerCase() : "global";
                switch (type) {
                    case "global" -> plugin.getMessageManager().sendPreview(
                            player, plugin.getConfigManager().getGlobalSection(), "Global");
                    case "firstjoin", "first-join" -> plugin.getMessageManager().sendPreview(
                            player, plugin.getConfigManager().getFirstJoinSection(), "First-Join");
                    default -> {
                        // Individuelle Spieler-Preview
                        var section = plugin.getConfigManager().getPlayerSection(type, type);
                        if (section != null) {
                            plugin.getMessageManager().sendPreview(player, section, "Player: " + type);
                        } else {
                            player.sendMessage(mm.deserialize(prefix + " <red>Kein Spieler-Eintrag gefunden für: <white>" + type));
                        }
                    }
                }
            }

            case "version", "info" -> {
                sender.sendMessage(mm.deserialize(
                    "<newline>" +
                    "<gradient:#a855f7:#6366f1>  ZyveraWelcomeMessage</gradient> <gray>v" + plugin.getDescription().getVersion() + "</gray>" +
                    "<newline><gray>  Gradient-Support: <green>✓ Paper 1.16+</green>" +
                    "<newline><gray>  Folia-Support: <green>✓</green>" +
                    "<newline>"));
            }

            default -> sendHelp(sender, prefix, label);
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String prefix, String label) {
        sender.sendMessage(mm.deserialize(
            "<newline>" + prefix +
            " <gray>Befehle:" +
            "<newline>  <gradient:#a855f7:#6366f1>/" + label + " reload</gradient> <dark_gray>-</dark_gray> <gray>Config neu laden" +
            "<newline>  <gradient:#a855f7:#6366f1>/" + label + " preview [global|first-join|spieler]</gradient> <dark_gray>-</dark_gray> <gray>Nachricht vorschauen" +
            "<newline>  <gradient:#a855f7:#6366f1>/" + label + " version</gradient> <dark_gray>-</dark_gray> <gray>Plugin-Info" +
            "<newline>"));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("zyvera.welcomemessage.admin")) return Collections.emptyList();

        if (args.length == 1) {
            return Arrays.asList("reload", "preview", "version");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("preview")) {
            return Arrays.asList("global", "first-join");
        }
        return Collections.emptyList();
    }
}
