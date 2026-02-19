package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.bypass.BypassEntry;
import com.viperlevels.bypass.BypassManager;
import com.viperlevels.bypass.BypassType;
import com.viperlevels.command.ViperCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class BypassCommand extends ViperCommand {

    private final BypassManager bypassManager;

    public BypassCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.admin.bypass");
        this.bypassManager = BypassManager.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUso: /viperlevels bypass <add|remove|list> ...");
            return;
        }

        String action = args[0].toLowerCase();

        switch (action) {
            case "add":
                handleAdd(sender, args);
                break;
            case "remove":
                handleRemove(sender, args);
                break;
            case "list":
                handleList(sender, args);
                break;
            default:
                sender.sendMessage("§cAzione non valida. Usa: add, remove, list");
        }
    }

    private void handleAdd(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUso: /viperlevels bypass add <player> <target> [duration]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("player-not-found"));
            return;
        }

        String bypassTarget = args[2].toUpperCase();
        BypassType type = BypassType.fromString(bypassTarget);
        
        if (type == null) {
            type = BypassType.ITEMS;
        }

        if (args.length >= 4) {
            try {
                long duration = parseDuration(args[3]);
                bypassManager.addTemporaryBypass(
                    target.getUniqueId(),
                    target.getName(),
                    type,
                    bypassTarget,
                    duration,
                    sender instanceof Player ? ((Player) sender).getUniqueId() : null
                );
                sender.sendMessage("§aBypass temporaneo aggiunto per §e" + target.getName() + " §a(§e" + bypassTarget + "§a)");
            } catch (IllegalArgumentException e) {
                sender.sendMessage("§cDurata non valida! Usa: 1h, 30m, 1d, ecc.");
            }
        } else {
            bypassManager.addPermanentBypass(
                target.getUniqueId(),
                target.getName(),
                type,
                bypassTarget,
                sender instanceof Player ? ((Player) sender).getUniqueId() : null
            );
            sender.sendMessage("§aBypass permanente aggiunto per §e" + target.getName() + " §a(§e" + bypassTarget + "§a)");
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /viperlevels bypass remove <player> [target]");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("player-not-found"));
            return;
        }

        if (args.length >= 3) {
            String bypassTarget = args[2].toUpperCase();
            bypassManager.removeSpecificBypass(target.getUniqueId(), bypassTarget);
            sender.sendMessage("§cBypass rimosso per §e" + target.getName() + " §c(§e" + bypassTarget + "§c)");
        } else {
            bypassManager.removeBypass(target.getUniqueId());
            sender.sendMessage("§cTutti i bypass rimossi per §e" + target.getName());
        }
    }

    private void handleList(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("§cUso: /viperlevels bypass list <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(plugin.getConfigManager().getMessageWithPrefix("player-not-found"));
            return;
        }

        List<BypassEntry> bypasses = bypassManager.getPlayerBypasses(target.getUniqueId());

        sender.sendMessage("§8§m----------§r §eBypass Attivi §8§m----------");
        sender.sendMessage("§7Giocatore: §e" + target.getName());
        sender.sendMessage("");

        if (bypasses.isEmpty()) {
            sender.sendMessage("§cNessun bypass attivo");
        } else {
            for (BypassEntry bypass : bypasses) {
                String expiry = bypass.getExpiresAt() == null ? "§aPermanente" : 
                    "§eScade tra " + formatDuration(bypass.getExpiresAt() - System.currentTimeMillis());
                sender.sendMessage("§e• " + bypass.getTarget() + " §7(" + bypass.getBypassType() + ") §7- " + expiry);
            }
        }

        sender.sendMessage("§8§m--------------------------------");
    }

    private long parseDuration(String input) {
        char unit = input.charAt(input.length() - 1);
        int value = Integer.parseInt(input.substring(0, input.length() - 1));

        switch (unit) {
            case 's': return TimeUnit.SECONDS.toMillis(value);
            case 'm': return TimeUnit.MINUTES.toMillis(value);
            case 'h': return TimeUnit.HOURS.toMillis(value);
            case 'd': return TimeUnit.DAYS.toMillis(value);
            default: throw new IllegalArgumentException("Invalid duration unit");
        }
    }

    private String formatDuration(long millis) {
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        long hours = TimeUnit.MILLISECONDS.toHours(millis) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) % 60;

        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        return minutes + "m";
    }
}