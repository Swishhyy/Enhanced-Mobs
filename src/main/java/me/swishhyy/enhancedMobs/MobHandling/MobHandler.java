package me.swishhyy.enhancedMobs.MobHandling;

import me.swishhyy.enhancedMobs.EnhancedMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MobHandler implements Listener {

    private final EnhancedMobs plugin;
    private final FileConfiguration mobsConfig;
    private final Set<String> loggedMobs = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private double cachedTPS = 20.0;
    private long lastTPSFetch = 0;

    public MobHandler(EnhancedMobs plugin) {
        this.plugin = plugin;
        this.mobsConfig = plugin.getMobsConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        // Skip if TPS is low
        if (getServerTPS() < plugin.getConfig().getDouble("performance.min_tps_threshold", 18.0)) {
            return;
        }

        if (!(event.getEntity() instanceof Mob mob)) {
            return;
        }

        String mobName = mob.getType().name().toLowerCase();

        // Skip if mob type is not configured
        if (!mobsConfig.contains(mobName)) {
            return;
        }

        ConfigurationSection mobSection = mobsConfig.getConfigurationSection(mobName);
        if (mobSection == null) {
            return;
        }

        // Skip if spawn conditions are not met
        if (!enforceSpawnConditions(mob, mobSection)) {
            return;
        }

        // Apply customizations
        customizeMob(mob, mobSection);
    }

    private void customizeMob(LivingEntity entity, ConfigurationSection mobSection) {
        // Set health
        double health = mobSection.getDouble("health", 20.0);
        AttributeInstance healthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(health);
            entity.setHealth(health);
        }

        // Set speed
        double speed = mobSection.getDouble("speed", 0.2);
        AttributeInstance speedAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(speed);
        }

        // Set equipment
        if (entity instanceof Mob mob) {
            ConfigurationSection equipmentSection = mobSection.getConfigurationSection("equipment");
            if (equipmentSection != null) {
                for (String item : equipmentSection.getKeys(false)) {
                    Material material = Material.matchMaterial(item.toUpperCase());
                    double chance = equipmentSection.getDouble(item + ".chance", 0.0) / 100;
                    if (material != null && Math.random() <= chance) {
                        mob.getEquipment().setItemInMainHand(new ItemStack(material));
                    }
                }
            }
        }

        // Log customization without spamming console
        if (plugin.getConfig().getBoolean("log.mob_customizations", false)) {
            logOnce("Customized mob: " + entity.getType().name());
        }
    }

    private void logOnce(String message) {
        if (loggedMobs.add(message)) {
            plugin.getLogger().info(message);
            Bukkit.getScheduler().runTaskLater(plugin, () -> loggedMobs.remove(message), 200L); // Remove after 10 seconds
        }
    }

    private boolean enforceSpawnConditions(LivingEntity entity, ConfigurationSection mobSection) {
        List<String> spawnConditions = mobSection.getStringList("spawn_conditions");

        for (String condition : spawnConditions) {
            condition = condition.toLowerCase();

            // Check for night condition
            if (condition.equals("night")) {
                long time = entity.getWorld().getTime();
                if (time < 13000 || time > 23000) {
                    return false;
                }
            }

            // Check for land condition
            if (condition.equals("land")) {
                if (!entity.getLocation().getBlock().isPassable()) {
                    return false;
                }
            }

            // Check for water condition
            if (condition.equals("water")) {
                if (!entity.getLocation().getBlock().isLiquid()) {
                    return false;
                }
            }

            // Check for specific biomes
            if (condition.startsWith("biome:")) {
                String biome = condition.replace("biome:", "").toUpperCase();
                if (!entity.getLocation().getBlock().getBiome().name().equals(biome)) {
                    return false;
                }
            }
        }

        return true;
    }

    private double getServerTPS() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTPSFetch > 5000) { // Fetch TPS every 5 seconds
            lastTPSFetch = currentTime;
            try {
                Object minecraftServer = Bukkit.getServer().getClass().getMethod("getServer").invoke(Bukkit.getServer());
                double[] recentTps = (double[]) minecraftServer.getClass().getField("recentTps").get(minecraftServer);
                cachedTPS = recentTps[0];
            } catch (ReflectiveOperationException e) {
                plugin.getLogger().severe("Failed to fetch server TPS.");
            }
        }
        return cachedTPS;
    }
}
