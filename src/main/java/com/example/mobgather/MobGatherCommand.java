package com.example.mobgather;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MobGatherCommand implements CommandExecutor {

    private final MobGatherPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MobGatherCommand(MobGatherPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "cooldown":
                handleCooldown(sender, args);
                break;
            case "give":
                handleGive(sender, args);
                break;
            case "toggle":
                handleToggle(sender, args);
                break;
            case "help":
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mobgather.admin")) {
            sendMessage(sender, "<red>你没有权限使用此命令！");
            return;
        }

        try {
            plugin.reloadPlugin();
            sendMessage(sender, "<green>配置重载成功！");
        } catch (Exception e) {
            plugin.getLogger().severe("重载配置失败: " + e.getMessage());
            sendMessage(sender, "<red>配置重载失败！");
        }
    }

    private void handleCooldown(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobgather.admin")) {
            sendMessage(sender, "<red>你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            sendMessage(sender, "<yellow>用法: /mobgather cooldown <玩家名>");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "<red>玩家不在线！");
            return;
        }

        plugin.getCooldownManager().clearCooldowns(target);
        sendMessage(sender, "<green>已清除玩家 <yellow>" + target.getName() + " <green>的所有冷却！");
    }

    private void handleGive(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobgather.admin")) {
            sendMessage(sender, "<red>你没有权限使用此命令！");
            return;
        }

        if (args.length < 3) {
            sendMessage(sender, "<yellow>用法: /mobgather give <玩家> <类型> [数量]");
            sendMessage(sender, "<gray>类型: zombie, spider, skeleton, creeper, slime, wither, player, dragon");
            return;
        }

        Player target = plugin.getServer().getPlayer(args[1]);
        if (target == null) {
            sendMessage(sender, "<red>玩家不在线！");
            return;
        }

        String type = args[2].toLowerCase();
        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "<red>无效的数量！");
                return;
            }
        }

        org.bukkit.Material material = switch (type) {
            case "zombie" -> org.bukkit.Material.ZOMBIE_HEAD;
            case "spider" -> org.bukkit.Material.SPIDER_EYE;
            case "skeleton" -> org.bukkit.Material.SKELETON_SKULL;
            case "creeper" -> org.bukkit.Material.CREEPER_HEAD;
            case "slime" -> org.bukkit.Material.SLIME_BALL;
            case "wither" -> org.bukkit.Material.WITHER_SKELETON_SKULL;
            case "player" -> org.bukkit.Material.PLAYER_HEAD;
            case "dragon" -> org.bukkit.Material.DRAGON_HEAD;
            default -> null;
        };

        if (material == null) {
            sendMessage(sender, "<red>无效的物品类型！可用类型: zombie, spider, skeleton, creeper, slime, wither, player, dragon");
            return;
        }

        org.bukkit.inventory.ItemStack item = new org.bukkit.inventory.ItemStack(material, amount);
        target.getInventory().addItem(item);
        sendMessage(sender, "<green>已给予 <yellow>" + target.getName() + " <green>" + amount + " 个 <yellow>" + type + "<green>！");
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mobgather.toggle")) {
            sendMessage(sender, "<red>你没有权限使用此命令！");
            return;
        }

        if (args.length < 2) {
            sendToggleHelp(sender);
            return;
        }

        String type = args[1].toLowerCase();
        if (!type.equals("cooldown") && !type.equals("consume")) {
            sendToggleHelp(sender);
            return;
        }

        String permission = type.equals("cooldown") ? "mobgather.bypass.cooldown" : "mobgather.bypass.consume";

        Player target;
        boolean isOther = false;

        if (args.length >= 3) {
            if (!sender.hasPermission("mobgather.toggle.others")) {
                sendMessage(sender, "<red>你没有权限切换其他玩家的设置！");
                return;
            }
            target = plugin.getServer().getPlayer(args[2]);
            if (target == null) {
                sendMessage(sender, "<red>玩家不在线！");
                return;
            }
            isOther = true;
        } else {
            if (!(sender instanceof Player)) {
                sendMessage(sender, "<red>控制台请指定玩家名: /mobgather toggle " + type + " <玩家>");
                return;
            }
            target = (Player) sender;
        }

        togglePermission(target, permission);

        boolean hasPermission = target.hasPermission(permission);
        String status = hasPermission ? "<green>开启" : "<red>关闭";
        String typeName = type.equals("cooldown") ? "冷却绕过" : "消耗绕过";

        if (isOther) {
            sendMessage(sender, "<green>已设置玩家 <yellow>" + target.getName() + " <green>的" + typeName + ": " + status);
            sendMessage(target, "<yellow>" + sender.getName() + " <green>已将你的" + typeName + "设置为: " + status);
        } else {
            sendMessage(sender, "<green>你的" + typeName + "已设置为: " + status);
        }
    }

    private void togglePermission(Player player, String permission) {
        boolean hasPermission = player.hasPermission(permission);
        if (hasPermission) {
            player.addAttachment(plugin, permission, false);
        } else {
            player.addAttachment(plugin, permission, true);
        }
    }

    private void sendHelp(CommandSender sender) {
        sendMessage(sender, "<dark_gray>========== <green>MobGather <yellow>帮助 <dark_gray>==========");
        sendMessage(sender, "<gray>/mobgather help <dark_gray>- <white>显示此帮助");
        if (sender.hasPermission("mobgather.toggle")) {
            sendMessage(sender, "<gray>/mobgather toggle <cooldown|consume> [玩家] <dark_gray>- <white>开关冷却/消耗绕过");
        }
        if (sender.hasPermission("mobgather.admin")) {
            sendMessage(sender, "<gray>/mobgather reload <dark_gray>- <white>重载配置文件");
            sendMessage(sender, "<gray>/mobgather cooldown <玩家> <dark_gray>- <white>清除玩家冷却");
            sendMessage(sender, "<gray>/mobgather give <玩家> <类型> [数量] <dark_gray>- <white>给予聚集物品");
        }
        sendMessage(sender, "<dark_gray>=====================================");
        sendMessage(sender, "<gray>手持对应头颅右键即可使用");
        sendMessage(sender, "<gray>玩家头颅无消耗 | 龙首可抹杀怪物");
    }

    private void sendToggleHelp(CommandSender sender) {
        sendMessage(sender, "<dark_gray>========== <green>MobGather <yellow>Toggle 帮助 <dark_gray>==========");
        sendMessage(sender, "<gray>/mobgather toggle cooldown <dark_gray>- <white>开关自己的冷却绕过");
        sendMessage(sender, "<gray>/mobgather toggle consume <dark_gray>- <white>开关自己的消耗绕过");
        if (sender.hasPermission("mobgather.toggle.others")) {
            sendMessage(sender, "<gray>/mobgather toggle cooldown <玩家> <dark_gray>- <white>开关指定玩家的冷却绕过");
            sendMessage(sender, "<gray>/mobgather toggle consume <玩家> <dark_gray>- <white>开关指定玩家的消耗绕过");
        }
        sendMessage(sender, "<dark_gray>==========================================");
    }

    private void sendMessage(CommandSender sender, String message) {
        if (message.isEmpty()) return;
        Component component = miniMessage.deserialize(message);
        sender.sendMessage(component);
    }
}
