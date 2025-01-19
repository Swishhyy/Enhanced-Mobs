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
import java.util.List;
import java.util.Set;

public class MobHandler implements Listener {

    private final EnhancedMobs plugin;
    private final FileConfiguration mobsConfig; // Added mobsConfig field

    public MobHandler(EnhancedMobs plugin) {
        this.plugin = plugin;
        this.mobsConfig = plugin.getMobsConfig(); // Initialize mobsConfig here
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        LivingEntity entity = event.getEntity();

        String mobName = entity.getType().name().toLowerCase();

        if (!mobsConfig.contains(mobName)) return;

        ConfigurationSection mobSection = mobsConfig.getConfigurationSection(mobName);
        if (mobSection == null) return;

        // Enforce spawn conditions
        if (!enforceSpawnConditions(entity, mobSection)) {
            event.setCancelled(true);
            return;
        }

        // Set health
        double health = mobSection.getDouble("health", 20.0);
        AttributeInstance healthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(health);
            entity.setHealth(health); // Ensure current health matches max health
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
                Set<String> equipmentKeys = equipmentSection.getKeys(false);
                for (String item : equipmentKeys) {
                    Material material = Material.matchMaterial(item.toUpperCase());
                    double chance = equipmentSection.getDouble(item + ".chance", 0.0) / 100;
                    if (material != null && Math.random() <= chance) {
                        mob.getEquipment().setItemInMainHand(new ItemStack(material));
                    }
                }
            }
        }

        // Log changes
        plugin.getLogger().info("Applied configuration to mob: " + mobName);
    }

    /**
     * Enforces spawn conditions for a mob.
     *
     * @param entity     The entity being spawned.
     * @param mobSection The configuration section for the mob.
     * @return True if spawn conditions are met, false otherwise.
     */
    private boolean enforceSpawnConditions(LivingEntity entity, ConfigurationSection mobSection) {
        List<String> spawnConditions = mobSection.getStringList("spawn_conditions");

        for (String condition : spawnConditions) {
            condition = condition.toLowerCase();

            // Check for night condition
            if (condition.equals("night")) {
                long time = entity.getWorld().getTime();
                boolean isNight = time >= 13000 && time <= 23000;
                if (!isNight) {
                    plugin.getLogger().info("Cancelled spawn of " + entity.getType().name() + " due to time condition: night");
                    return false;
                }
            }

            // Check for land condition
            if (condition.equals("land")) {
                if (!entity.getLocation().getBlock().isPassable()) {
                    plugin.getLogger().info("Cancelled spawn of " + entity.getType().name() + " due to condition: land");
                    return false;
                }
            }

            // Check for water condition
            if (condition.equals("water")) {
                if (!entity.getLocation().getBlock().isLiquid()) {
                    plugin.getLogger().info("Cancelled spawn of " + entity.getType().name() + " due to condition: water");
                    return false;
                }
            }

            // Check for specific biomes
            if (condition.startsWith("biome:")) {
                String biome = condition.replace("biome:", "").toUpperCase();
                if (!entity.getLocation().getBlock().getBiome().name().equals(biome)) {
                    plugin.getLogger().info("Cancelled spawn of " + entity.getType().name() + " due to biome condition: " + biome);
                    return false;
                }
            }

            // Additional conditions can be added here
        }

        return true;
    }
}
