package com.viperlevels.listener;

import com.viperlevels.ViperLevels;
import com.viperlevels.config.MessagesConfig;
import com.viperlevels.config.PenaltiesConfig;
import com.viperlevels.config.PlaceholderReplacer;
import com.viperlevels.rule.RuleValidator;
import com.viperlevels.rule.ValidationResult;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.Map;

public class DimensionListener implements Listener {

    private final ViperLevels plugin;
    private final RuleValidator validator;
    private final MessagesConfig messages;
    private final PenaltiesConfig penalties;

    public DimensionListener(ViperLevels plugin) {
        this.plugin = plugin;
        this.validator = new RuleValidator();
        this.messages = plugin.getConfigManager().getMessagesConfig();
        this.penalties = plugin.getConfigManager().getPenaltiesConfig();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        World newWorld = player.getWorld();
        String dimensionName = getDimensionName(newWorld);

        ValidationResult result = validator.validateDimension(player, dimensionName);

        if (!result.isPassed()) {
            World fromWorld = event.getFrom();
            Location safeLocation = fromWorld.getSpawnLocation();

            player.teleport(safeLocation);

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(dimensionName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.dimension"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission("viperlevels.bypass.all")) {
            return;
        }

        Location to = event.getTo();
        if (to == null) {
            return;
        }

        World toWorld = to.getWorld();
        World fromWorld = event.getFrom().getWorld();

        if (toWorld == null || toWorld.equals(fromWorld)) {
            return;
        }

        String dimensionName = getDimensionName(toWorld);

        ValidationResult result = validator.validateDimension(player, dimensionName);

        if (!result.isPassed()) {
            PenaltiesConfig.PenaltySettings penalty = penalties.getPenalty("dimension");

            if (penalty.isCancelEvent()) {
                event.setCancelled(true);
            }

            Map<String, String> placeholders = PlaceholderReplacer.combine(
                    PlaceholderReplacer.withPlayer(player),
                    PlaceholderReplacer.withMaterial(dimensionName),
                    PlaceholderReplacer.withRequirements(result.getMissingRequirements())
            );

            String message = PlaceholderReplacer.replace(
                    messages.getMessageWithPrefix("restriction.dimension"),
                    placeholders
            );

            player.sendMessage(message);
        }
    }

    private String getDimensionName(World world) {
        switch (world.getEnvironment()) {
            case NETHER:
                return "NETHER";
            case THE_END:
                return "THE_END";
            default:
                return "OVERWORLD";
        }
    }
}