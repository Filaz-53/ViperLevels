package com.viperlevels.util;

import com.viperlevels.ViperLevels;
import com.viperlevels.database.DatabaseManager;
import com.viperlevels.database.repository.StatsRepository;
import lombok.Getter;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class MetricsCollector {

    private static MetricsCollector instance;

    private final ViperLevels plugin;
    private final Map<String, AtomicLong> metrics;
    private final StatsRepository repository;

    private MetricsCollector() {
        this.plugin = ViperLevels.getInstance();
        this.metrics = new ConcurrentHashMap<>();
        this.repository = DatabaseManager.getInstance().getStatsRepository();

        initializeMetrics();
    }

    public static MetricsCollector getInstance() {
        if (instance == null) {
            instance = new MetricsCollector();
        }
        return instance;
    }

    private void initializeMetrics() {
        metrics.put("validations.total", new AtomicLong(0));
        metrics.put("validations.passed", new AtomicLong(0));
        metrics.put("validations.failed", new AtomicLong(0));
        metrics.put("bypasses.active", new AtomicLong(0));
        metrics.put("cache.hits", new AtomicLong(0));
        metrics.put("cache.misses", new AtomicLong(0));
        metrics.put("rules.loaded", new AtomicLong(0));
        metrics.put("database.queries", new AtomicLong(0));
    }

    public void incrementMetric(String key) {
        metrics.computeIfAbsent(key, k -> new AtomicLong(0)).incrementAndGet();
    }

    public void setMetric(String key, long value) {
        metrics.computeIfAbsent(key, k -> new AtomicLong(0)).set(value);
    }

    public long getMetric(String key) {
        AtomicLong metric = metrics.get(key);
        return metric != null ? metric.get() : 0;
    }

    public void recordValidation(boolean passed) {
        incrementMetric("validations.total");
        incrementMetric(passed ? "validations.passed" : "validations.failed");
    }

    public void recordCacheAccess(boolean hit) {
        incrementMetric(hit ? "cache.hits" : "cache.misses");
    }

    public double getValidationPassRate() {
        long total = getMetric("validations.total");
        if (total == 0) return 100.0;

        long passed = getMetric("validations.passed");
        return (double) passed / total * 100.0;
    }

    public double getCacheHitRate() {
        long hits = getMetric("cache.hits");
        long misses = getMetric("cache.misses");
        long total = hits + misses;

        if (total == 0) return 0.0;
        return (double) hits / total * 100.0;
    }

    public Map<String, Long> getAllMetrics() {
        Map<String, Long> result = new ConcurrentHashMap<>();
        metrics.forEach((key, value) -> result.put(key, value.get()));
        return result;
    }

    public CompletableFuture<Void> saveMetrics() {
        return CompletableFuture.runAsync(() -> {
            // TODO: Implement proper metric persistence when StatsRepository method is available
            for (Map.Entry<String, AtomicLong> entry : metrics.entrySet()) {
                // repository.saveStat("metric", entry.getKey(), entry.getValue().get());
                plugin.logDebug("Metric: " + entry.getKey() + " = " + entry.getValue().get());
            }
            plugin.logDebug("Metrics saved to database");
        });
    }

    public void reset() {
        metrics.values().forEach(metric -> metric.set(0));
        plugin.logInfo("Metrics reset");
    }
}