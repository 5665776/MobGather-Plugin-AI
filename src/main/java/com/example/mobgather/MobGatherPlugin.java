package com.example.mobgather;

import org.bukkit.plugin.java.JavaPlugin;

public class MobGatherPlugin extends JavaPlugin {

    private static MobGatherPlugin instance;
    private ConfigManager configManager;
    private CooldownManager cooldownManager;
    private GatherManager gatherManager;

    @Override
    public void onEnable() {
        instance = this;

        // 保存默认配置
        saveDefaultConfig();

        // 初始化管理器
        this.configManager = new ConfigManager(this);
        this.cooldownManager = new CooldownManager(this);
        this.gatherManager = new GatherManager(this);

        // 注册事件监听
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);

        // 注册命令
        getCommand("mobgather").setExecutor(new MobGatherCommand(this));
        getCommand("mobgather").setTabCompleter(new MobGatherTabCompleter());

        getLogger().info("=================================");
        getLogger().info("MobGather 怪物聚集系统已加载！");
        getLogger().info("版本: " + getDescription().getVersion());
        getLogger().info("作者: " + getDescription().getAuthors());
        getLogger().info("=================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("MobGather 怪物聚集系统已卸载！");
    }

    public static MobGatherPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public GatherManager getGatherManager() {
        return gatherManager;
    }

    public void reloadPlugin() {
        reloadConfig();
        configManager.loadConfig();
        cooldownManager.clearAllCooldowns();
    }
}
