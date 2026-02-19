package com.viperlevels.messaging;

import com.viperlevels.ViperLevels;
import com.viperlevels.config.MessagesConfig;
import com.viperlevels.config.PlaceholderReplacer;
import com.viperlevels.rule.ValidationResult;
import com.viperlevels.util.CooldownManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public class MessageSender {

    private final ViperLevels plugin;
    private final MessagesConfig messages;
    private final CooldownManager cooldownManager;

    public MessageSender() {
        this.plugin = ViperLevels.getInstance();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.cooldownManager = new CooldownManager(2000);
    }

    public void sendRestrictionMessage(Player player, String messageKey, Map<String, String> placeholders) {
        if (cooldownManager.hasCooldown(player.getUniqueId())) {
            return;
        }

        String message = messages.getMessage("restriction." + messageKey);
        message = PlaceholderReplacer.replace(message, placeholders);
        message = messages.getPrefix() + message;

        player.sendMessage(MessageFormatter.format(message));
        cooldownManager.setCooldown(player.getUniqueId());
    }

    public void sendRestrictionFromResult(Player player, ValidationResult result, String messageKey) {
        if (!result.isPassed() && result.hasMissingRequirements()) {
            Map<String, String> placeholders = PlaceholderReplacer.withRequirements(result.getMissingRequirements());

            if (result.getAppliedRule() != null) {
                placeholders.put("material", result.getAppliedRule().getIdentifier());
            }

            sendRestrictionMessage(player, messageKey, placeholders);
        }
    }

    public void sendMessage(CommandSender sender, String messageKey) {
        String message = messages.getMessageWithPrefix(messageKey);
        sender.sendMessage(MessageFormatter.format(message));
    }

    public void sendMessage(CommandSender sender, String messageKey, Map<String, String> placeholders) {
        String message = messages.getMessage(messageKey);
        message = PlaceholderReplacer.replace(message, placeholders);
        message = messages.getPrefix() + message;
        sender.sendMessage(MessageFormatter.format(message));
    }

    public void sendRawMessage(CommandSender sender, String message) {
        sender.sendMessage(MessageFormatter.format(message));
    }

    public void sendActionBar(Player player, String message) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, 
            new TextComponent(MessageFormatter.format(message)));
    }

    public void sendNoPermission(CommandSender sender) {
        sendMessage(sender, "no-permission");
    }

    public void sendPlayerNotFound(CommandSender sender) {
        sendMessage(sender, "player-not-found");
    }

    public void sendReloadSuccess(CommandSender sender) {
        sendMessage(sender, "reload-success");
    }

    public void broadcast(String message) {
        String formatted = messages.getPrefix() + message;
        plugin.getServer().broadcastMessage(MessageFormatter.format(formatted));
    }

    public void broadcastToPermission(String message, String permission) {
        String formatted = messages.getPrefix() + message;
        plugin.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(permission))
                .forEach(p -> p.sendMessage(MessageFormatter.format(formatted)));
    }
}