package com.example.mobgather;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GatherManager {

    private final MobGatherPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GatherManager(MobGatherPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean gather(Player player, Material material) {
        ConfigManager config = plugin.getConfigManager();

        String permission = getPermission(material);
        if (permission != null && !player.hasPermission(permission)) {
            sendMessage(player, config.getNoPermissionMessage());
            return false;
        }

        if (!player.hasPermission("mobgather.bypass.cooldown")) {
            double remainingCooldown = plugin.getCooldownManager().getRemainingCooldown(player, material);
            if (remainingCooldown > 0) {
                String msg = config.getCooldownMessage().replace("%time%", String.format("%.1f", remainingCooldown));
                sendMessage(player, msg);
                return false;
            }
        } else if (config.isDebugEnabled()) {
            plugin.getLogger().info(player.getName() + " 绕过冷却 (权限: mobgather.bypass.cooldown)");
        }

        Location playerLoc = player.getLocation();
        int range = config.getRange(material);
        int count = 0;

        switch (material) {
            case ZOMBIE_HEAD:
                count = gatherZombies(player, playerLoc, range);
                break;
            case SPIDER_EYE:
                count = gatherSpiders(player, playerLoc, range);
                break;
            case SKELETON_SKULL:
                count = gatherSkeletons(player, playerLoc, range);
                break;
            case CREEPER_HEAD:
                count = gatherCreepers(player, playerLoc, range);
                break;
            case SLIME_BALL:
                count = gatherSlimes(player, playerLoc, range);
                break;
            case WITHER_SKELETON_SKULL:
                count = gatherAllHostile(player, playerLoc, range, false);
                break;
            case PLAYER_HEAD:
                count = gatherAllHostile(player, playerLoc, range, true);
                break;
            case DRAGON_HEAD:
                count = killAllHostile(player, playerLoc, range);
                break;
            default:
                return false;
        }

        if (!player.hasPermission("mobgather.bypass.cooldown")) {
            plugin.getCooldownManager().setCooldown(player, material);
        } else if (config.isDebugEnabled()) {
            plugin.getLogger().info(player.getName() + " 绕过冷却设置 (权限: mobgather.bypass.cooldown)");
        }

        if (config.isConsumeEnabled() && config.shouldConsume(material) && !player.hasPermission("mobgather.bypass.consume")) {
            consumeItem(player, material);
        } else if (config.isDebugEnabled() && player.hasPermission("mobgather.bypass.consume")) {
            plugin.getLogger().info(player.getName() + " 绕过消耗 (权限: mobgather.bypass.consume)");
        }

        sendCompleteMessage(player, material, count);
        return true;
    }

    private int gatherZombies(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.ZOMBIE_HEAD));
        playEffects(player, loc, Material.ZOMBIE_HEAD);

        List<Zombie> zombies = loc.getWorld().getEntitiesByClass(Zombie.class).stream()
            .filter(z -> z.getLocation().distance(loc) <= range).toList();
        for (Zombie zombie : zombies) {
            zombie.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                zombie.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        return zombies.size();
    }

    private int gatherSpiders(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.SPIDER_EYE));
        playEffects(player, loc, Material.SPIDER_EYE);

        List<Spider> spiders = loc.getWorld().getEntitiesByClass(Spider.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        List<CaveSpider> caveSpiders = loc.getWorld().getEntitiesByClass(CaveSpider.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        for (Spider spider : spiders) {
            spider.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                spider.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        for (CaveSpider caveSpider : caveSpiders) {
            caveSpider.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                caveSpider.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        return spiders.size() + caveSpiders.size();
    }

    private int gatherSkeletons(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.SKELETON_SKULL));
        playEffects(player, loc, Material.SKELETON_SKULL);

        List<Skeleton> skeletons = loc.getWorld().getEntitiesByClass(Skeleton.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        List<Stray> strays = loc.getWorld().getEntitiesByClass(Stray.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        List<WitherSkeleton> witherSkeletons = loc.getWorld().getEntitiesByClass(WitherSkeleton.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        for (Skeleton skeleton : skeletons) {
            skeleton.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                skeleton.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        for (Stray stray : strays) {
            stray.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                stray.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        for (WitherSkeleton ws : witherSkeletons) {
            ws.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                ws.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        return skeletons.size() + strays.size() + witherSkeletons.size();
    }

    private int gatherCreepers(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.CREEPER_HEAD));
        playEffects(player, loc, Material.CREEPER_HEAD);

        List<Creeper> creepers = loc.getWorld().getEntitiesByClass(Creeper.class).stream()
            .filter(c -> c.getLocation().distance(loc) <= range).toList();
        for (Creeper creeper : creepers) {
            creeper.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                creeper.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        return creepers.size();
    }

    private int gatherSlimes(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.SLIME_BALL));
        playEffects(player, loc, Material.SLIME_BALL);

        List<Slime> slimes = loc.getWorld().getEntitiesByClass(Slime.class).stream()
            .filter(s -> s.getLocation().distance(loc) <= range).toList();
        List<MagmaCube> magmaCubes = loc.getWorld().getEntitiesByClass(MagmaCube.class).stream()
            .filter(m -> m.getLocation().distance(loc) <= range).toList();
        for (Slime slime : slimes) {
            slime.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                slime.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        for (MagmaCube magmaCube : magmaCubes) {
            magmaCube.teleport(loc.clone().add(0, 2, 0));
            if (config.isGlowingEnabled()) {
                magmaCube.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, config.getGlowingDuration() * 20, 0));
            }
        }
        return slimes.size() + magmaCubes.size();
    }

    private int gatherAllHostile(Player player, Location loc, int range, boolean free) {
        ConfigManager config = plugin.getConfigManager();
        Material material = free ? Material.PLAYER_HEAD : Material.WITHER_SKELETON_SKULL;
        sendActionBar(player, config.getMessage(material));
        playEffects(player, loc, material);

        Location target = loc.clone().add(0, 3, 0);
        int total = 0;

        // 基础敌对生物
        List<Zombie> zombies = getEntities(loc, range, Zombie.class);
        List<Spider> spiders = getEntities(loc, range, Spider.class);
        List<CaveSpider> caveSpiders = getEntities(loc, range, CaveSpider.class);
        List<Skeleton> skeletons = getEntities(loc, range, Skeleton.class);
        List<Stray> strays = getEntities(loc, range, Stray.class);
        List<WitherSkeleton> witherSkeletons = getEntities(loc, range, WitherSkeleton.class);
        List<Creeper> creepers = getEntities(loc, range, Creeper.class);
        List<Slime> slimes = getEntities(loc, range, Slime.class);
        List<MagmaCube> magmaCubes = getEntities(loc, range, MagmaCube.class);

        // 扩展敌对生物
        List<Witch> witches = getEntities(loc, range, Witch.class);
        List<Enderman> endermen = getEntities(loc, range, Enderman.class);
        List<Phantom> phantoms = getEntities(loc, range, Phantom.class);
        List<Drowned> drowneds = getEntities(loc, range, Drowned.class);
        List<Husk> husks = getEntities(loc, range, Husk.class);
        List<ZombieVillager> zombieVillagers = getEntities(loc, range, ZombieVillager.class);
        List<Pillager> pillagers = getEntities(loc, range, Pillager.class);
        List<Vindicator> vindicators = getEntities(loc, range, Vindicator.class);
        List<Evoker> evokers = getEntities(loc, range, Evoker.class);
        List<Ravager> ravagers = getEntities(loc, range, Ravager.class);
        List<Vex> vexes = getEntities(loc, range, Vex.class);
        List<Guardian> guardians = getEntities(loc, range, Guardian.class);
        List<ElderGuardian> elderGuardians = getEntities(loc, range, ElderGuardian.class);
        List<Blaze> blazes = getEntities(loc, range, Blaze.class);
        List<Ghast> ghasts = getEntities(loc, range, Ghast.class);
        List<Hoglin> hoglins = getEntities(loc, range, Hoglin.class);
        List<Zoglin> zoglins = getEntities(loc, range, Zoglin.class);
        List<PiglinBrute> piglinBrutes = getEntities(loc, range, PiglinBrute.class);
        List<Silverfish> silverfishes = getEntities(loc, range, Silverfish.class);
        List<Endermite> endermites = getEntities(loc, range, Endermite.class);
        List<Shulker> shulkers = getEntities(loc, range, Shulker.class);
        List<Warden> wardens = getEntities(loc, range, Warden.class);
        List<Breeze> breezes = getEntities(loc, range, Breeze.class);

        total = zombies.size() + spiders.size() + caveSpiders.size() + skeletons.size() + 
                strays.size() + witherSkeletons.size() + creepers.size() + slimes.size() + 
                magmaCubes.size() + witches.size() + endermen.size() + phantoms.size() + 
                drowneds.size() + husks.size() + zombieVillagers.size() + pillagers.size() + 
                vindicators.size() + evokers.size() + ravagers.size() + vexes.size() + 
                guardians.size() + elderGuardians.size() + blazes.size() + ghasts.size() + 
                hoglins.size() + zoglins.size() + piglinBrutes.size() + silverfishes.size() + 
                endermites.size() + shulkers.size() + wardens.size() + breezes.size();

        // 传送所有生物
        for (Zombie z : zombies) { z.teleport(target); applyGlowing(z); }
        for (Spider s : spiders) { s.teleport(target); applyGlowing(s); }
        for (CaveSpider cs : caveSpiders) { cs.teleport(target); applyGlowing(cs); }
        for (Skeleton sk : skeletons) { sk.teleport(target); applyGlowing(sk); }
        for (Stray st : strays) { st.teleport(target); applyGlowing(st); }
        for (WitherSkeleton ws : witherSkeletons) { ws.teleport(target); applyGlowing(ws); }
        for (Creeper c : creepers) { c.teleport(target); applyGlowing(c); }
        for (Slime sl : slimes) { sl.teleport(target); applyGlowing(sl); }
        for (MagmaCube mc : magmaCubes) { mc.teleport(target); applyGlowing(mc); }
        for (Witch w : witches) { w.teleport(target); applyGlowing(w); }
        for (Enderman e : endermen) { e.teleport(target); applyGlowing(e); }
        for (Phantom p : phantoms) { p.teleport(target); applyGlowing(p); }
        for (Drowned d : drowneds) { d.teleport(target); applyGlowing(d); }
        for (Husk h : husks) { h.teleport(target); applyGlowing(h); }
        for (ZombieVillager zv : zombieVillagers) { zv.teleport(target); applyGlowing(zv); }
        for (Pillager pl : pillagers) { pl.teleport(target); applyGlowing(pl); }
        for (Vindicator v : vindicators) { v.teleport(target); applyGlowing(v); }
        for (Evoker ev : evokers) { ev.teleport(target); applyGlowing(ev); }
        for (Ravager r : ravagers) { r.teleport(target); applyGlowing(r); }
        for (Vex vx : vexes) { vx.teleport(target); applyGlowing(vx); }
        for (Guardian g : guardians) { g.teleport(target); applyGlowing(g); }
        for (ElderGuardian eg : elderGuardians) { eg.teleport(target); applyGlowing(eg); }
        for (Blaze b : blazes) { b.teleport(target); applyGlowing(b); }
        for (Ghast gh : ghasts) { gh.teleport(target); applyGlowing(gh); }
        for (Hoglin hg : hoglins) { hg.teleport(target); applyGlowing(hg); }
        for (Zoglin zg : zoglins) { zg.teleport(target); applyGlowing(zg); }
        for (PiglinBrute pb : piglinBrutes) { pb.teleport(target); applyGlowing(pb); }
        for (Silverfish sf : silverfishes) { sf.teleport(target); applyGlowing(sf); }
        for (Endermite em : endermites) { em.teleport(target); applyGlowing(em); }
        for (Shulker sh : shulkers) { sh.teleport(target); applyGlowing(sh); }
        for (Warden wa : wardens) { wa.teleport(target); applyGlowing(wa); }
        for (Breeze br : breezes) { br.teleport(target); applyGlowing(br); }

        if (config.isDebugEnabled()) {
            plugin.getLogger().info(player.getName() + " 聚集了 " + total + " 只敌对生物" + (free ? " [无消耗]" : ""));
        }
        return total;
    }

    private int killAllHostile(Player player, Location loc, int range) {
        ConfigManager config = plugin.getConfigManager();
        sendActionBar(player, config.getMessage(Material.DRAGON_HEAD));
        playEffects(player, loc, Material.DRAGON_HEAD);

        int total = 0;

        // 基础敌对生物
        List<Zombie> zombies = getEntities(loc, range, Zombie.class);
        List<Spider> spiders = getEntities(loc, range, Spider.class);
        List<CaveSpider> caveSpiders = getEntities(loc, range, CaveSpider.class);
        List<Skeleton> skeletons = getEntities(loc, range, Skeleton.class);
        List<Stray> strays = getEntities(loc, range, Stray.class);
        List<WitherSkeleton> witherSkeletons = getEntities(loc, range, WitherSkeleton.class);
        List<Creeper> creepers = getEntities(loc, range, Creeper.class);
        List<Slime> slimes = getEntities(loc, range, Slime.class);
        List<MagmaCube> magmaCubes = getEntities(loc, range, MagmaCube.class);

        // 扩展敌对生物
        List<Witch> witches = getEntities(loc, range, Witch.class);
        List<Enderman> endermen = getEntities(loc, range, Enderman.class);
        List<Phantom> phantoms = getEntities(loc, range, Phantom.class);
        List<Drowned> drowneds = getEntities(loc, range, Drowned.class);
        List<Husk> husks = getEntities(loc, range, Husk.class);
        List<ZombieVillager> zombieVillagers = getEntities(loc, range, ZombieVillager.class);
        List<Pillager> pillagers = getEntities(loc, range, Pillager.class);
        List<Vindicator> vindicators = getEntities(loc, range, Vindicator.class);
        List<Evoker> evokers = getEntities(loc, range, Evoker.class);
        List<Ravager> ravagers = getEntities(loc, range, Ravager.class);
        List<Vex> vexes = getEntities(loc, range, Vex.class);
        List<Guardian> guardians = getEntities(loc, range, Guardian.class);
        List<ElderGuardian> elderGuardians = getEntities(loc, range, ElderGuardian.class);
        List<Blaze> blazes = getEntities(loc, range, Blaze.class);
        List<Ghast> ghasts = getEntities(loc, range, Ghast.class);
        List<Hoglin> hoglins = getEntities(loc, range, Hoglin.class);
        List<Zoglin> zoglins = getEntities(loc, range, Zoglin.class);
        List<PiglinBrute> piglinBrutes = getEntities(loc, range, PiglinBrute.class);
        List<Silverfish> silverfishes = getEntities(loc, range, Silverfish.class);
        List<Endermite> endermites = getEntities(loc, range, Endermite.class);
        List<Shulker> shulkers = getEntities(loc, range, Shulker.class);
        List<Warden> wardens = getEntities(loc, range, Warden.class);
        List<Breeze> breezes = getEntities(loc, range, Breeze.class);

        total = zombies.size() + spiders.size() + caveSpiders.size() + skeletons.size() + 
                strays.size() + witherSkeletons.size() + creepers.size() + slimes.size() + 
                magmaCubes.size() + witches.size() + endermen.size() + phantoms.size() + 
                drowneds.size() + husks.size() + zombieVillagers.size() + pillagers.size() + 
                vindicators.size() + evokers.size() + ravagers.size() + vexes.size() + 
                guardians.size() + elderGuardians.size() + blazes.size() + ghasts.size() + 
                hoglins.size() + zoglins.size() + piglinBrutes.size() + silverfishes.size() + 
                endermites.size() + shulkers.size() + wardens.size() + breezes.size();

        // 移除所有生物
        zombies.forEach(Zombie::remove);
        spiders.forEach(Spider::remove);
        caveSpiders.forEach(CaveSpider::remove);
        skeletons.forEach(Skeleton::remove);
        strays.forEach(Stray::remove);
        witherSkeletons.forEach(WitherSkeleton::remove);
        creepers.forEach(Creeper::remove);
        slimes.forEach(Slime::remove);
        magmaCubes.forEach(MagmaCube::remove);
        witches.forEach(Witch::remove);
        endermen.forEach(Enderman::remove);
        phantoms.forEach(Phantom::remove);
        drowneds.forEach(Drowned::remove);
        husks.forEach(Husk::remove);
        zombieVillagers.forEach(ZombieVillager::remove);
        pillagers.forEach(Pillager::remove);
        vindicators.forEach(Vindicator::remove);
        evokers.forEach(Evoker::remove);
        ravagers.forEach(Ravager::remove);
        vexes.forEach(Vex::remove);
        guardians.forEach(Guardian::remove);
        elderGuardians.forEach(ElderGuardian::remove);
        blazes.forEach(Blaze::remove);
        ghasts.forEach(Ghast::remove);
        hoglins.forEach(Hoglin::remove);
        zoglins.forEach(Zoglin::remove);
        piglinBrutes.forEach(PiglinBrute::remove);
        silverfishes.forEach(Silverfish::remove);
        endermites.forEach(Endermite::remove);
        shulkers.forEach(Shulker::remove);
        wardens.forEach(Warden::remove);
        breezes.forEach(Breeze::remove);

        loc.getWorld().spawnParticle(Particle.EXPLOSION, loc, 20, 5, 5, 5);
        loc.getWorld().spawnParticle(Particle.LAVA, loc, 50, 5, 5, 5);

        if (config.isDebugEnabled()) {
            plugin.getLogger().info(player.getName() + " 抹杀了 " + total + " 只敌对生物");
        }
        return total;
    }

    private <T extends Entity> List<T> getEntities(Location loc, int range, Class<T> clazz) {
        return loc.getWorld().getEntitiesByClass(clazz).stream()
            .filter(e -> e.getLocation().distance(loc) <= range)
            .toList();
    }

    private void applyGlowing(LivingEntity entity) {
        if (plugin.getConfigManager().isGlowingEnabled()) {
            entity.addPotionEffect(new PotionEffect(
                PotionEffectType.GLOWING,
                plugin.getConfigManager().getGlowingDuration() * 20,
                0
            ));
        }
    }

    private void playEffects(Player player, Location loc, Material material) {
        ConfigManager config = plugin.getConfigManager();
        if (config.isParticlesEnabled()) {
            Particle particle = config.getParticleType(material);
            int count = config.getParticleCount();
            loc.getWorld().spawnParticle(particle, loc.clone().add(0, 1, 0), count, 1, 1, 1, 0.1);
        }
        if (config.isSoundsEnabled()) {
            Sound sound = config.getSoundEffect(material);
            player.playSound(loc, sound, 1.0f, 1.0f);
        }
    }

    private void consumeItem(Player player, Material material) {
        PlayerInventory inv = player.getInventory();
        ItemStack mainHand = inv.getItemInMainHand();
        ItemStack offHand = inv.getItemInOffHand();

        if (mainHand.getType() == material && mainHand.getAmount() > 0) {
            mainHand.setAmount(mainHand.getAmount() - 1);
            inv.setItemInMainHand(mainHand);
            return;
        }
        if (offHand.getType() == material && offHand.getAmount() > 0) {
            offHand.setAmount(offHand.getAmount() - 1);
            inv.setItemInOffHand(offHand);
            return;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null && item.getType() == material && item.getAmount() > 0) {
                item.setAmount(item.getAmount() - 1);
                inv.setItem(i, item);
                return;
            }
        }
    }

    private String getPermission(Material material) {
        return switch (material) {
            case ZOMBIE_HEAD -> "mobgather.use.zombie";
            case SPIDER_EYE -> "mobgather.use.spider";
            case SKELETON_SKULL -> "mobgather.use.skeleton";
            case CREEPER_HEAD -> "mobgather.use.creeper";
            case SLIME_BALL -> "mobgather.use.slime";
            case WITHER_SKELETON_SKULL -> "mobgather.use.wither";
            case PLAYER_HEAD -> "mobgather.use.player";
            case DRAGON_HEAD -> "mobgather.use.dragon";
            default -> null;
        };
    }

    private void sendActionBar(Player player, String message) {
        if (plugin.getConfigManager().isActionbarEnabled() && !message.isEmpty()) {
            Component component = miniMessage.deserialize(message);
            player.sendActionBar(component);
        }
    }

    private void sendMessage(Player player, String message) {
        if (plugin.getConfigManager().isChatMessagesEnabled() && !message.isEmpty()) {
            String prefix = plugin.getConfigManager().getPrefix();
            Component component = miniMessage.deserialize(prefix + message);
            player.sendMessage(component);
        }
    }

    private void sendCompleteMessage(Player player, Material material, int count) {
        if (count <= 0) {
            sendMessage(player, "<gray>范围内没有目标生物。");
            return;
        }
        String msg = switch (material) {
            case ZOMBIE_HEAD -> "<green>已聚集 <yellow>" + count + " <green>只僵尸！";
            case SPIDER_EYE -> "<green>已聚集 <yellow>" + count + " <green>只蜘蛛！";
            case SKELETON_SKULL -> "<green>已聚集 <yellow>" + count + " <green>只骷髅！";
            case CREEPER_HEAD -> "<green>已聚集 <yellow>" + count + " <green>只苦力怕！";
            case SLIME_BALL -> "<green>已聚集 <yellow>" + count + " <green>只史莱姆！";
            case WITHER_SKELETON_SKULL -> "<dark_red>已聚集 <yellow>" + count + " <dark_red>只敌对生物！";
            case PLAYER_HEAD -> "<gold>已聚集 <yellow>" + count + " <gold>只敌对生物！ <gray>[无消耗]";
            case DRAGON_HEAD -> "<dark_red>已抹杀 <yellow>" + count + " <dark_red>只敌对生物！";
            default -> "<green>操作完成！";
        };
        sendMessage(player, msg);
        if (plugin.getConfigManager().isSoundsEnabled()) {
            if (material == Material.DRAGON_HEAD) {
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.5f);
            } else {
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            }
        }
    }
}
