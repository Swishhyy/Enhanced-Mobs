package me.swishhyy.enhancedMobs.MobHandling;

import me.swishhyy.enhancedMobs.EnhancedMobs;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.util.Set;

public class MobSpawning {

    private final EnhancedMobs plugin; // Use EnhancedMobs instead of JavaPlugin
    private final FileConfiguration mobsConfig;

    public MobSpawning(EnhancedMobs plugin) {
        this.plugin = plugin;
        this.mobsConfig = plugin.getMobsConfig(); // Dynamically fetch the config
    }

    /**
     * Spawns a custom mob at a given location, replacing all vanilla spawns.
     *
     * @param world   The world where the mob will spawn.
     * @param mobType The type of mob to spawn (based on the configuration key).
     * @param location The location where the mob should spawn.
     */
    public void spawnCustomMob(World world, String mobType, Location location) {
        if (!mobsConfig.contains(mobType)) {
            plugin.getLogger().warning("No configuration found for mob type: " + mobType);
            return;
        }

        ConfigurationSection mobSection = mobsConfig.getConfigurationSection(mobType);
        if (mobSection == null) {
            plugin.getLogger().warning("Failed to load configuration for mob: " + mobType);
            return;
        }

        // Create entity based on mob type
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(mobType.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid mob type in configuration: " + mobType);
            return;
        }

        LivingEntity entity = (LivingEntity) world.spawnEntity(location, entityType);

        // Set health
        double health = mobSection.getDouble("health", 20.0);
        AttributeInstance healthAttribute = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(health);
            entity.setHealth(health); // Ensure current health matches max health
        } else {
            plugin.getLogger().warning("Unable to set health for mob: " + mobType);
        }

        // Set speed
        double speed = mobSection.getDouble("speed", 0.2);
        AttributeInstance speedAttribute = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.setBaseValue(speed);
        } else {
            plugin.getLogger().warning("Unable to set speed for mob: " + mobType);
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

        // Log the spawn
        plugin.getLogger().info("Spawned custom mob: " + mobType + " at " + location);
    }

    /**
     * Replaces all vanilla mobs with custom mobs during spawn logic.
     *
     * @param world The world where the replacement occurs.
     */
    public void replaceVanillaSpawns(World world) {
        world.getEntities().stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player))
                .forEach(entity -> {
                    String mobType = entity.getType().name().toLowerCase();

                    // If the mob type exists in configuration, replace it
                    if (mobsConfig.contains(mobType)) {
                        Location location = entity.getLocation();
                        entity.remove(); // Remove the vanilla mob
                        spawnCustomMob(world, mobType, location); // Spawn custom mob
                    }
                });
    }

    /**
     * Schedules regular replacement of vanilla spawns with custom mobs.
     */
    public void scheduleSpawnReplacement() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                replaceVanillaSpawns(world);
            }
        }, 0L, 20L * 10); // Runs every 10 seconds
    }
}
