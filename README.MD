# PerfectFireball 完美火球插件

![Bukkit](https://img.shields.io/badge/Bukkit-1.13%2B-blue)
![Spigot](https://img.shields.io/badge/Spigot-1.13%2B-orange)
![Paper](https://img.shields.io/badge/Paper-1.13%2B-yellow)
![License](https://img.shields.io/badge/License-MIT-green)

PerfectFireball 是一个强大的 Bukkit/Spigot 插件，为玩家发射火球提供了高度可定制的体验。通过简单的配置，您可以调整火球的速度、冷却时间、爆炸效果等参数，让您的服务器拥有与众不同的火球玩法！

原生支持版本：1.21 paper

## 功能特点

✅ 自定义火球速度倍率  
✅ 可配置的火球冷却时间  
✅ 火球超时自动消失机制  
✅ 可选的方块破坏功能  
✅ 可选的实体伤害功能  
✅ 可调节的击飞效果（强度和范围）  
✅ 可调节的爆炸视觉效果  
✅ 完整的权限控制系统  
✅ 实时配置重载命令

## 安装方法

1. 下载最新的 `PerfectFireball.jar` 文件
2. 将文件放入服务器的 `plugins` 文件夹
3. 重启服务器
4. 插件会自动生成配置文件 `plugins/PerfectFireball/config.yml`
5. 根据需求修改配置文件（详情见下文）

## 权限节点

| 权限节点 | 描述 | 默认 |
|----------|------|------|
| `pfb.use` | 允许使用自定义火球 | OP |
| `pfb.reload` | 允许重载插件配置 | OP |

## 命令使用

```bash
/pfb
```
显示插件命令帮助信息

### 重载配置
```bash
/pfb reload
```
重载插件配置，显示当前所有配置值。<br>
需要 pfb.reload 权限
## 配置文件解析
配置文件位于 plugins/PerfectFireball/config.yml，下面是详细的配置说明：

```yaml
# PerfectFireball 配置文件

# 火球速度倍率 (默认: 1.0)
#: 1.0)
# 值越大，火球飞行速度越快
fireball-speed-multiplier: 1.0

# 火球超时时间 (秒) (默认: 20)
# 火球在发射后经过指定时间未命中目标将自动消失
# 设置为0表示永不超时
fireball-timeout: 20

# 冷却时间 (tick) (默认: 16,0.8s)
# 玩家发射火球后的冷却时间，20 ticks = 1秒
# 设置为0表示无冷却
cooldown-ticks: 16

# 是否破坏方块 (默认: false)
# 设置为true时，火球爆炸会破坏方块，爆炸强度由 explosion-power 控制
destroy-blocks: false

# 是否伤害实体 (默认: false)
# 设置为 true 时，火球爆炸会伤害实体（玩家、生物等）伤害由 explosion-power 控制
damage-entities: false

# 击飞强度 (默认: 2.0)
# 值越大，被击飞的实体飞得越远
knockback-power: 4.5

# 击飞范围 (默认: 5.0)
# 火球爆炸后，在此 火球爆炸后，在此范围内的实体会被击飞
knockback-radius: 5.0

# 爆炸视觉效果强度 (默认: 0.0)
# 控制爆炸的视觉效果（声音、粒子），不影响实际破坏
# 设置为0.0时无视觉效果，最大值为4.0（相当于TNT）
explosion-power: 0.0
```


