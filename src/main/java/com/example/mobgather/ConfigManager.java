package com.example.mobgather;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private final MobGatherPlugin plugin;
    private FileConfiguration config;

    private boolean particlesEnabled;
    private boolean soundsEnabled;
    private boolean actionbarEnabled;
    private boolean chatMessagesEnabled;
    private boolean debugEnabled;
    private boolean consumeEnabled;
    private boolean glowingEnabled;
    private int glowingDuration;
    private int particleCount;

    private final Map<Material, Integer> ranges = new HashMap<>();
    private final Map<Material, Integer> cooldowns = new HashMap<>();
    private final Map<Material, Boolean> consumeItems = new HashMap<>();
    private final Map<Material, Particle> particleTypes = new HashMap<>();
    private final Map<Material, Sound> soundEffects = new HashMap<>();
    private final Map<Material, String> messages = new HashMap<>();

    private String prefix;
    private String cooldownMessage;
    private String noPermissionMessage;
    private String reloadSuccessMessage;
    private String reloadFailMessage;
    private String killSuccessMessage;

    public ConfigManager(MobGatherPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        this.config = plugin.getConfig();

        this.particlesEnabled = config.getBoolean("global.particles", true);
        this.soundsEnabled = config.getBoolean("global.sounds", true);
        this.actionbarEnabled = config.getBoolean("global.actionbar", true);
        this.chatMessagesEnabled = config.getBoolean("global.chat-messages", true);
        this.debugEnabled = config.getBoolean("debug", false);

        this.consumeEnabled = config.getBoolean("consume.enabled", true);

        this.glowingEnabled = config.getBoolean("effects.glowing", true);
        this.glowingDuration = config.getInt("effects.glowing-duration", 5);
        this.particleCount = config.getInt("effects.particle-count", 50);

        this.prefix = config.getString("messages.prefix", "<dark_gray>[<green>MobGather<dark_gray>] <reset>");
        this.cooldownMessage = config.getString("messages.cooldown", "<red>冷却中，请等待 <yellow>%time% <red>秒");
        this.noPermissionMessage = config.getString("messages.no-permission", "<red>你没有权限使用此功能！");
        this.reloadSuccessMessage = config.getString("messages.reload-success", "<green>配置重载成功！");
        this.reloadFailMessage = config.getString("messages.reload-fail", "<red>配置重载失败！");
        this.killSuccessMessage = config.getString("messages.kill-success", "<red>已抹杀 <yellow>%count% <red>只敌对生物！");

        loadItemConfig(Material.ZOMBIE_HEAD, "zombie-head", "gather-zombie", 100, 5, true,
            Particle.HAPPY_VILLAGER, Sound.ENTITY_ZOMBIE_AMBIENT);
        loadItemConfig(Material.SPIDER_EYE, "spider-eye", "gather-spider", 100, 5, true,
            Particle.WITCH, Sound.ENTITY_SPIDER_AMBIENT);
        loadItemConfig(Material.SKELETON_SKULL, "skeleton-skull", "gather-skeleton", 100, 5, true,
            Particle.ASH, Sound.ENTITY_SKELETON_AMBIENT);
        loadItemConfig(Material.CREEPER_HEAD, "creeper-head", "gather-creeper", 100, 5, true,
            Particle.SMOKE, Sound.ENTITY_CREEPER_PRIMED);
        loadItemConfig(Material.SLIME_BALL, "slime-ball", "gather-slime", 100, 5, true,
            Particle.ITEM_SLIME, Sound.ENTITY_SLIME_SQUISH);
        loadItemConfig(Material.WITHER_SKELETON_SKULL, "wither-skeleton-skull", "gather-all", 100, 15, true,
            Particle.SOUL_FIRE_FLAME, Sound.ENTITY_WITHER_AMBIENT);
        loadItemConfig(Material.PLAYER_HEAD, "player-head", "gather-all-free", 100, 0, false,
            Particle.ENCHANT, Sound.BLOCK_BEACON_POWER_SELECT);
        loadItemConfig(Material.DRAGON_HEAD, "dragon-head", "kill-all", 100, 20, true,
            Particle.DRAGON_BREATH, Sound.ENTITY_ENDER_DRAGON_GROWL);
    }

    private void loadItemConfig(Material material, String configKey, String messageKey,
                                int defaultRange, int defaultCooldown, boolean defaultConsume,
                                Particle defaultParticle, Sound defaultSound) {
        int range = config.getInt("ranges." + configKey, -1);
        if (range == -1) range = config.getInt("ranges.default", defaultRange);
        ranges.put(material, range);

        int cooldown = config.getInt("cooldowns." + configKey, -1);
        if (cooldown == -1) cooldown = config.getInt("cooldowns.default", defaultCooldown);
        cooldowns.put(material, cooldown);

        boolean consume = config.getBoolean("consume." + configKey, defaultConsume);
        consumeItems.put(material, consume);

        String particleStr = config.getString("effects.particle-types." + configKey, defaultParticle.toString());
        Particle particle = parseParticle(particleStr, defaultParticle);
        particleTypes.put(material, particle);

        String soundStr = config.getString("sound-effects." + configKey, defaultSound.toString());
        Sound sound = parseSound(soundStr, defaultSound);
        soundEffects.put(material, sound);

        String message = config.getString("messages." + messageKey, "");
        messages.put(material, message);
    }

    private Particle parseParticle(String name, Particle defaultValue) {
        try {
            return Particle.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的粒子类型: " + name + "，使用默认值");
            return defaultValue;
        }
    }

    private Sound parseSound(String name, Sound defaultValue) {
        try {
            return Sound.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的音效: " + name + "，使用默认值");
            return defaultValue;
        }
    }

    public boolean isParticlesEnabled() { return particlesEnabled; }
    public boolean isSoundsEnabled() { return soundsEnabled; }
    public boolean isActionbarEnabled() { return actionbarEnabled; }
    public boolean isChatMessagesEnabled() { return chatMessagesEnabled; }
    public boolean isDebugEnabled() { return debugEnabled; }
    public boolean isConsumeEnabled() { return consumeEnabled; }
    public boolean isGlowingEnabled() { return glowingEnabled; }
    public int getGlowingDuration() { return glowingDuration; }
    public int getParticleCount() { return particleCount; }

    public int getRange(Material material) { return ranges.getOrDefault(material, 100); }
    public int getCooldown(Material material) { return cooldowns.getOrDefault(material, 5); }
    public boolean shouldConsume(Material material) { return consumeItems.getOrDefault(material, true); }
    public Particle getParticleType(Material material) { return particleTypes.getOrDefault(material, Particle.HAPPY_VILLAGER); }
    public Sound getSoundEffect(Material material) { return soundEffects.getOrDefault(material, Sound.ENTITY_ZOMBIE_AMBIENT); }
    public String getMessage(Material material) { return messages.getOrDefault(material, ""); }

    public String getPrefix() { return prefix; }
    public String getCooldownMessage() { return cooldownMessage; }
    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getReloadSuccessMessage() { return reloadSuccessMessage; }
    public String getReloadFailMessage() { return reloadFailMessage; }
    public String getKillSuccessMessage() { return killSuccessMessage; }
}
