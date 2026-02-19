package com.viperlevels.command.impl;

import com.viperlevels.ViperLevels;
import com.viperlevels.command.ViperCommand;
import com.viperlevels.util.MetricsCollector;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class StatsCommand extends ViperCommand {

    private final MetricsCollector metrics;

    public StatsCommand(ViperLevels plugin) {
        super(plugin, "viperlevels.admin.stats");
        this.metrics = MetricsCollector.getInstance();
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("§8§m----------§r §eViperLevels Stats §8§m----------");
        sender.sendMessage("");
        
        sender.sendMessage("§6§lValidazioni:");
        sender.sendMessage("  §7Totali: §e" + metrics.getMetric("validations.total"));
        sender.sendMessage("  §7Passate: §a" + metrics.getMetric("validations.passed"));
        sender.sendMessage("  §7Fallite: §c" + metrics.getMetric("validations.failed"));
        sender.sendMessage("  §7Tasso successo: §e" + String.format("%.1f", metrics.getValidationPassRate()) + "%");
        sender.sendMessage("");
        
        sender.sendMessage("§6§lCache:");
        sender.sendMessage("  §7Dimensione: §e" + plugin.getCacheManager().getCacheSize());
        sender.sendMessage("  §7Hit: §a" + metrics.getMetric("cache.hits"));
        sender.sendMessage("  §7Miss: §c" + metrics.getMetric("cache.misses"));
        sender.sendMessage("  §7Hit Rate: §e" + String.format("%.1f", metrics.getCacheHitRate()) + "%");
        sender.sendMessage("");
        
        sender.sendMessage("§6§lRegole:");
        sender.sendMessage("  §7Caricate: §e" + plugin.getRuleManager().getTotalRulesCount());
        sender.sendMessage("");
        
        sender.sendMessage("§6§lBypass:");
        sender.sendMessage("  §7Attivi: §e" + plugin.getBypassManager().getAllBypasses().size());
        sender.sendMessage("");
        
        sender.sendMessage("§8§m--------------------------------");
    }
}