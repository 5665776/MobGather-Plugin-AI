package com.example.mobgather;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    private final MobGatherPlugin plugin;

    private static final Material[] VALID_ITEMS = {
        Material.ZOMBIE_HEAD,
        Material.SPIDER_EYE,
        Material.SKELETON_SKULL,
        Material.CREEPER_HEAD,
        Material.SLIME_BALL,
        Material.WITHER_SKELETON_SKULL,
        Material.PLAYER_HEAD,
        Material.DRAGON_HEAD
    };

    public PlayerInteractListener(MobGatherPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }

        Material material = item.getType();

        if (!isValidItem(material)) {
            return;
        }

        event.setCancelled(true);
        boolean success = plugin.getGatherManager().gather(event.getPlayer(), material);

        if (plugin.getConfigManager().isDebugEnabled()) {
            plugin.getLogger().info(event.getPlayer().getName() + " 使用 " + material.name() + " - 结果: " + success);
        }
    }

    private boolean isValidItem(Material material) {
        for (Material valid : VALID_ITEMS) {
            if (valid == material) {
                return true;
            }
        }
        return false;
    }
}
