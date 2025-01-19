package me.swishhyy.enhancedMobs.MobHandling;

import me.swishhyy.enhancedMobs.EnhancedMobs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;

public class MobSpawning {

    private final FileConfiguration mobsConfig;

    public MobSpawning(EnhancedMobs plugin) {
        this.mobsConfig = plugin.getMobsConfig();
    }

    /**
     * Prepares the configuration for custom mob attributes to directly affect their natural spawns.
     */
    public void initializeCustomMobs() {
        for (String mobType : mobsConfig.getKeys(false)) {
            try {
                EntityType entityType = EntityType.valueOf(mobType.toUpperCase());

                // Validate the configuration
                ConfigurationSection mobSection = mobsConfig.getConfigurationSection(mobType);
                if (mobSection == null) continue;

                // Log applied attributes (as actual application needs NMS or other API integrations)
                applyDefaultAttributes(entityType, mobSection);
                applyDefaultEquipment(entityType, mobSection);
            } catch (IllegalArgumentException e) {
                Bukkit.getLogger().warning("[EnhancedMobs] Invalid mob type in configuration: " + mobType);
            }
        }
    }

    private void applyDefaultAttributes(EntityType entityType, ConfigurationSection mobSection) {
        double health = mobSection.getDouble("health", 20.0);
        double speed = mobSection.getDouble("speed", 0.2);

        // Log attributes for debugging
        Bukkit.getLogger().info("[EnhancedMobs] Default attributes for " + entityType.name() + ":");
        Bukkit.getLogger().info("  - Health: " + health);
        Bukkit.getLogger().info("  - Speed: " + speed);

        // Note: You cannot globally set attributes for EntityTypes directly in Bukkit.
        // This is a placeholder for potential integration with NMS or an advanced API.
    }

    private void applyDefaultEquipment(EntityType entityType, ConfigurationSection mobSection) {
        ConfigurationSection equipmentSection = mobSection.getConfigurationSection("equipment");
        if (equipmentSection != null) {
            Bukkit.getLogger().info("[EnhancedMobs] Default equipment for " + entityType.name() + ":");
            for (String item : equipmentSection.getKeys(false)) {
                Material material = Material.matchMaterial(item.toUpperCase());
                double chance = equipmentSection.getDouble(item + ".chance", 0.0);

                if (material != null) {
                    Bukkit.getLogger().info("  - " + material.name() + " with chance: " + (chance * 100) + "%");
                }
            }

            // Note: Setting default equipment for EntityTypes globally requires advanced API integrations
            // like NMS. This logic is for reference/debugging purposes.
        }
    }
}
