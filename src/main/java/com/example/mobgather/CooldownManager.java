package com.example.mobgather;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {

    private final MobGatherPlugin plugin;

    // 存储玩家UUID -> 物品类型 -> 冷却结束时间(毫秒)
    private final Map<UUID, Map<Material, Long>> cooldowns = new HashMap<>();

    public CooldownManager(MobGatherPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * 检查玩家是否处于冷却中（纯检查，不涉及权限）
     * @param player 玩家
     * @param material 物品类型
     * @return 如果处于冷却返回剩余秒数，否则返回0
     */
    public double getRemainingCooldown(Player player, Material material) {
        UUID playerId = player.getUniqueId();

        Map<Material, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return 0;
        }

        Long endTime = playerCooldowns.get(material);
        if (endTime == null) {
            return 0;
        }

        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            playerCooldowns.remove(material);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(playerId);
            }
            return 0;
        }

        double seconds = remaining / 1000.0;
        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info(player.getName() + " 冷却中: " + String.format("%.1f", seconds) + "秒");
        }
        return seconds;
    }

    /**
     * 设置玩家冷却（纯设置，不涉及权限）
     * @param player 玩家
     * @param material 物品类型
     */
    public void setCooldown(Player player, Material material) {
        int cooldownSeconds = plugin.getConfigManager().getCooldown(material);
        if (cooldownSeconds <= 0) {
            if (plugin.getConfigManager().isDebugEnabled()) {
                plugin.getLogger().info(player.getName() + " " + material.name() + " 无冷却设置");
            }
            return;
        }

        UUID playerId = player.getUniqueId();
        long endTime = System.currentTimeMillis() + (cooldownSeconds * 1000L);
        cooldowns.computeIfAbsent(playerId, k -> new HashMap<>()).put(material, endTime);

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info("设置冷却: " + player.getName() + " - " + material.name() + " (" + cooldownSeconds + "秒)");
        }
    }

    /**
     * 清除玩家的所有冷却
     * @param player 玩家
     */
    public void clearCooldowns(Player player) {
        cooldowns.remove(player.getUniqueId());
    }

    /**
     * 清除所有冷却
     */
    public void clearAllCooldowns() {
        cooldowns.clear();
    }
}
