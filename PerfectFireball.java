package fun.ceroxe;

import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PerfectFireball extends JavaPlugin implements Listener, CommandExecutor {

    // 权限节点
    public static final String FIREBALL_PERMISSION = "pfb.use";
    public static final String RELOAD_PERMISSION = "pfb.reload";

    @Override
    public void onEnable() {
        // 确保配置文件存在（带注释）
        createConfigWithComments();

        getServer().getPluginManager().registerEvents(this, this);

        // 注册重载命令
        Command pfbCommand = getCommand("pfb");
        if (pfbCommand != null) {
            ((PluginCommand) pfbCommand).setExecutor(this);
            ((PluginCommand) pfbCommand).setTabCompleter(this);
            getLogger().info("§a成功注册 /pfb 命令");
        } else {
            getLogger().warning("§c无法注册 /pfb 命令，请检查 plugin.yml 文件");
        }

        getLogger().log(Level.INFO, "§aPerfectFireball 已启用! 权限节点: {0}", FIREBALL_PERMISSION);
    }

    /**
     * 创建带注释的配置文件
     */
    private void createConfigWithComments() {
        File configFile = new File(getDataFolder(), "config.yml");

        // 如果配置文件不存在，从资源文件复制
        if (!configFile.exists()) {
            // 确保插件目录存在
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }

            try (InputStream input = getResource("config.yml")) {
                if (input != null) {
                    Files.copy(input, configFile.toPath());
                    getLogger().info("§a已创建带注释的默认配置文件");
                } else {
                    // 如果资源文件不存在，创建基本配置
                    getConfig().addDefault("fireball-speed-multiplier", 1.0); // 火球速度倍率
                    getConfig().addDefault("fireball-timeout", 20); // 火球超时时间（秒）
                    getConfig().addDefault("cooldown-ticks", 16); // 冷却时间（tick），20 ticks = 1秒
                    getConfig().addDefault("disable-fall-damage", false); // 全局禁用摔落伤害
                    getConfig().addDefault("override-other-plugins", false); // 覆盖其他插件行为
                    getConfig().addDefault("destroy-blocks", false);
                    getConfig().addDefault("damage-entities", false);
                    getConfig().addDefault("knockback-power", 5.0);
                    getConfig().addDefault("knockback-radius", 5.0);
                    getConfig().addDefault("explosion-power", 0.0);
                    getConfig().options().copyDefaults(true);
                    saveConfig();
                    getLogger().warning("§e未找到资源文件，创建了基本配置文件");
                }
            } catch (IOException e) {
                getLogger().log(Level.SEVERE, "§c创建配置文件失败", e);
            }
        }

        // 加载配置
        reloadConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission(RELOAD_PERMISSION)) {
                sender.sendMessage(ChatColor.RED + "你没有权限执行此命令!");
                return true;
            }

            try {
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "PerfectFireball 配置已重载!");

                // 显示所有配置参数（按配置文件顺序）
                sender.sendMessage(ChatColor.GOLD + "火球速度倍率: " + getConfig().getDouble("fireball-speed-multiplier"));
                sender.sendMessage(ChatColor.GOLD + "火球超时时间(秒): " + getConfig().getInt("fireball-timeout"));
                sender.sendMessage(ChatColor.GOLD + "冷却时间(tick): " + getConfig().getInt("cooldown-ticks") +
                        " (≈" + String.format("%.1f", getConfig().getDouble("cooldown-ticks") / 20.0) + "秒)");
                sender.sendMessage(ChatColor.GOLD + "禁用摔落伤害: " + getConfig().getBoolean("disable-fall-damage"));
                sender.sendMessage(ChatColor.GOLD + "覆盖其他插件行为: " + getConfig().getBoolean("override-other-plugins"));
                sender.sendMessage(ChatColor.GOLD + "破坏方块: " + getConfig().getBoolean("destroy-blocks"));
                sender.sendMessage(ChatColor.GOLD + "伤害实体: " + getConfig().getBoolean("damage-entities"));
                sender.sendMessage(ChatColor.GOLD + "击飞强度: " + getConfig().getDouble("knockback-power"));
                sender.sendMessage(ChatColor.GOLD + "击飞范围: " + getConfig().getDouble("knockback-radius"));
                sender.sendMessage(ChatColor.GOLD + "爆炸视觉效果强度: " + getConfig().getDouble("explosion-power"));

                sender.sendMessage(ChatColor.YELLOW + "使用权限: " + FIREBALL_PERMISSION);
                sender.sendMessage(ChatColor.YELLOW + "重载权限: " + RELOAD_PERMISSION);
                return true;
            } catch (Exception e) {
                sender.sendMessage(ChatColor.RED + "重载配置失败: " + e.getMessage());
                getLogger().log(Level.SEVERE, "重载配置失败", e);
                return true;
            }
        }

        // 显示帮助信息
        sender.sendMessage(ChatColor.GOLD + "PerfectFireball 命令帮助:");
        sender.sendMessage(ChatColor.YELLOW + "/pfb reload - 重载配置文件");
        sender.sendMessage(ChatColor.YELLOW + "配置文件位置: plugins/PerfectFireball/config.yml");
        sender.sendMessage(ChatColor.YELLOW + "使用权限: " + FIREBALL_PERMISSION);
        sender.sendMessage(ChatColor.YELLOW + "重载权限: " + RELOAD_PERMISSION);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        boolean overrideMode = getConfig().getBoolean("override-other-plugins", false);

        // 覆盖模式：强制处理所有火焰弹交互事件
        if (overrideMode) {
            // 重置事件取消状态，确保我们能处理
            if (event.isCancelled()) {
                event.setCancelled(false);
            }
        }
        // 非覆盖模式：如果事件已被取消，则不处理
        else if (event.isCancelled()) {
            return;
        }

        if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) &&
                event.getItem() != null &&
                event.getItem().getType() == Material.FIRE_CHARGE) {

            Player player = event.getPlayer();

            // 从配置获取冷却时间（ticks） - 使用一致的默认值16
            int cooldownTicks = getConfig().getInt("cooldown-ticks", 16);

            // 检查冷却时间 - 无论是否有权限，冷却时间内都阻止所有火球使用
            if (player.getCooldown(Material.FIRE_CHARGE) > 0) {
                event.setCancelled(true); // 关键修复：冷却时间内完全阻止火球使用
                return;
            }

            // 检查玩家是否有权限使用自定义火球
            if (player.hasPermission(FIREBALL_PERMISSION)) {
                // 立即取消事件，阻止原版行为
                event.setCancelled(true);

                // 使用自定义火球（应用配置的冷却时间）
                player.setCooldown(Material.FIRE_CHARGE, cooldownTicks);

                Location eyeLoc = player.getEyeLocation();
                Fireball fireball = player.getWorld().spawn(eyeLoc, Fireball.class);
                fireball.setDirection(eyeLoc.getDirection());
                fireball.setShooter(player);
                fireball.setYield(0); // 禁用原版爆炸
                fireball.setIsIncendiary(false); // 禁用点火功能

                // 设置火焰弹速度（使用配置的速度倍率）
                double speedMultiplier = getConfig().getDouble("fireball-speed-multiplier", 1.0);
                fireball.setVelocity(eyeLoc.getDirection().multiply(1.5 * speedMultiplier));

                // 火焰弹元数据标记（用于覆盖模式）
                fireball.setMetadata("PerfectFireball", new org.bukkit.metadata.FixedMetadataValue(this, true));

                // 消耗物品
                if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                    event.getItem().setAmount(event.getItem().getAmount() - 1);
                }

                // 设置火球超时（如果配置值大于0）
                int timeoutSeconds = getConfig().getInt("fireball-timeout", 20); // 使用一致的默认值20
                if (timeoutSeconds > 0) {
                    Bukkit.getScheduler().runTaskLater(this, () -> {
                        if (fireball.isValid()) {
                            fireball.remove();
                            // 修正：使用正确的粒子常量 SMOKE_LARGE 代替 LARGE_SMOKE
                            Location loc = fireball.getLocation();
                            loc.getWorld().spawnParticle(Particle.SMOKE_LARGE, loc, 10, 0.2, 0.2, 0.2, 0.05);
                        }
                    }, timeoutSeconds * 20L); // 转换为ticks
                }
            } else {
                // 无权限玩家：保留原版行为
                // 但设置冷却时间，防止连续使用（使用配置的冷却时间）
                player.setCooldown(Material.FIRE_CHARGE, cooldownTicks);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onProjectileHit(ProjectileHitEvent event) {
        boolean overrideMode = getConfig().getBoolean("override-other-plugins", false);

        if (!(event.getEntity() instanceof Fireball fireball)) {
            return;
        }

        // 覆盖模式：强制处理所有火焰弹命中事件
        if (overrideMode) {
            // 重置事件取消状态，确保我们能处理
            if (event.isCancelled()) {
                event.setCancelled(false);
            }
        }
        // 非覆盖模式：如果事件已被取消，则不处理
        else if (event.isCancelled()) {
            return;
        }

        // 只处理玩家发射的火焰弹
        if (!(fireball.getShooter() instanceof Player shooter)) return;

        // 检查玩家是否有权限使用自定义火球
        if (!shooter.hasPermission(FIREBALL_PERMISSION)) {
            return;
        }

        // 覆盖模式下的额外检查：确保是我们生成的火焰弹
        if (overrideMode && !fireball.hasMetadata("PerfectFireball")) {
            return;
        }

        // 自定义火球行为
        Location explodeLoc = fireball.getLocation();
        World world = fireball.getWorld();

        // 读取配置
        boolean destroyBlocks = getConfig().getBoolean("destroy-blocks", false);
        boolean damageEntities = getConfig().getBoolean("damage-entities", false);
        double knockbackPower = getConfig().getDouble("knockback-power", 5.0); // 使用一致的默认值5.0
        double knockbackRadius = getConfig().getDouble("knockback-radius", 5.0);
        double explosionPower = getConfig().getDouble("explosion-power", 0.0);

        // 取消原版爆炸
        event.setCancelled(true);
        fireball.remove();

        // ==== 完全重做爆炸系统 ====
        // 1. 方块破坏处理
        if (destroyBlocks) {
            // 应用游戏规则确保方块破坏生效
            boolean previousMobGriefing = world.getGameRuleValue(GameRule.MOB_GRIEFING);
            world.setGameRule(GameRule.MOB_GRIEFING, true);

            // 创建爆炸（仅用于方块破坏）
            world.createExplosion(explodeLoc, (float) Math.min(explosionPower, 4.0), false, true, null);

            // 恢复原始游戏规则
            world.setGameRule(GameRule.MOB_GRIEFING, previousMobGriefing);
        }

        // 2. 爆炸视觉效果（完全独立于伤害系统）
        if (explosionPower > 0) {
            // 根据威力调整效果强度
            float scaledPower = (float) Math.min(explosionPower, 4.0);

            // 播放爆炸声音
            world.playSound(explodeLoc, Sound.ENTITY_GENERIC_EXPLODE, scaledPower, 1.0f);

            // 粒子效果
            int particleCount = (int) (scaledPower * 15);
            world.spawnParticle(Particle.EXPLOSION_LARGE, explodeLoc, 1, 0, 0, 0, 0);
            world.spawnParticle(Particle.SMOKE_LARGE, explodeLoc, particleCount, 0.5, 0.5, 0.5, 0.05);
            world.spawnParticle(Particle.FLAME, explodeLoc, particleCount / 2, 0.5, 0.5, 0.5, 0.05);
        }

        // 3. 应用击飞效果（独立配置伤害）
        applyKnockback(world, explodeLoc, knockbackRadius, damageEntities, knockbackPower, shooter);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        // 检查是否启用全局禁用摔落伤害
        boolean disableFallDamage = getConfig().getBoolean("disable-fall-damage", false);

        if (!disableFallDamage) {
            return; // 如果未启用，不做处理
        }

        // 检查伤害原因是否为摔落伤害
        if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            // 取消所有实体的摔落伤害
            event.setCancelled(true);
        }
    }

    private void applyKnockback(World world, Location center, double radius,
                                boolean damageEntities,
                                double knockbackMultiplier, Player shooter) {

        // 确保最小击飞范围
        double effectiveRadius = Math.max(1.0, radius);

        for (Entity entity : world.getNearbyEntities(center, effectiveRadius, effectiveRadius, effectiveRadius)) {
            // 只处理活体实体
            if (entity instanceof LivingEntity livingEntity && !(entity instanceof ArmorStand)) {

                boolean isShooter = entity.equals(shooter);

                // 计算击飞方向
                Vector direction = entity.getLocation().toVector().subtract(center.toVector());

                // 避免零向量
                if (direction.lengthSquared() < 0.0001) {
                    direction = new Vector(0, 1, 0);
                } else {
                    direction.normalize();
                }

                double distance = entity.getLocation().distance(center);
                double attenuation = Math.max(0, 1 - (distance / effectiveRadius));

                // 计算击飞力度
                Vector knockback = direction.multiply(knockbackMultiplier * attenuation);

                // 应用击飞（保留实体原有速度）
                Vector currentVelocity = entity.getVelocity();
                entity.setVelocity(new Vector(
                        currentVelocity.getX() + knockback.getX(),
                        currentVelocity.getY() + knockback.getY() * 0.8,
                        currentVelocity.getZ() + knockback.getZ()
                ));

                // 伤害处理（跳过发射者）- 严格遵循damageEntities配置
                if (damageEntities && !isShooter && livingEntity.isValid()) {
                    double damage = 6.0 * attenuation;
                    // 确保至少造成0.5点伤害
                    damage = Math.max(0.5, damage);
                    livingEntity.damage(damage);

                    // 受伤粒子效果
                    world.spawnParticle(Particle.DAMAGE_INDICATOR,
                            livingEntity.getLocation().add(0, 1, 0),
                            5, 0.5, 0.5, 0.5);
                }
            }
        }
    }
}