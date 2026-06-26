package kr.toxicity.hud.bootstrap.bukkit.player

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.api.scheduler.HudTask
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.player.HudPlayerImpl
import kr.toxicity.hud.util.BOOTSTRAP
import kr.toxicity.hud.util.taskLater
import net.kyori.adventure.audience.Audience
import org.bukkit.Bukkit
import org.bukkit.boss.BossBar
import org.bukkit.entity.Player
import java.util.*

class HudPlayerBukkit(
    private val player: Player,
    private val audience: Audience
) : HudPlayerImpl() {
    override fun uuid(): UUID = player.uniqueId
    override fun name(): String = player.name
    override fun handle(): Any = player
    override fun audience(): Audience = audience
    override fun world(): WorldWrapper = WorldWrapper(
        player.world.name
    )

    override fun locale(): Locale = player.locale.let {
        val split = it.split('_')
        if (split.size == 1) Locale.of(split[0].lowercase()) else Locale.of(split[0].lowercase(), split[1].uppercase())
    }

    override fun location(): LocationWrapper {

        val loc = player.location
        return LocationWrapper(
            world(),
            loc.x,
            loc.y,
            loc.z,
            loc.pitch,
            loc.yaw
        )
    }

    override fun updatePlaceholder() {
        (BOOTSTRAP as BukkitBootstrapImpl).update(this)
    }

    override fun hudUpdateTask(speed: Long, block: () -> Unit): HudTask {
        return foliaPlayerTaskTimer(1, speed, block) ?: super.hudUpdateTask(speed, block)
    }

    override fun hudLocationProvideTask(speed: Long, block: () -> Unit): HudTask {
        return foliaPlayerTaskTimer(speed, speed, block) ?: super.hudLocationProvideTask(speed, block)
    }

    override fun reload() {
        initBossBar {
            super.reload()
        }
    }

    init {
        initBossBar {
            inject()
        }
    }

    private fun initBossBar(action: () -> Unit) {
        val bars = ArrayList<BossBar>()
        for (bossBar in Bukkit.getBossBars()) {
            if (bossBar.players.any {
                it.uniqueId == player.uniqueId
            }) {
                bossBar.removePlayer(player)
                bars += bossBar
            }
        }
        action()
        playerTaskLater(20) {
            bars.forEach {
                it.addPlayer(player)
            }
        }
    }

    override fun hasPermission(perm: String): Boolean = player.hasPermission(perm)

    private fun foliaPlayerTaskTimer(delay: Long, period: Long, block: () -> Unit): HudTask? {
        val bootstrap = BOOTSTRAP as BukkitBootstrapImpl
        if (!bootstrap.isFolia()) {
            return null
        }
        return player.scheduler.runAtFixedRate(bootstrap, {
            block()
        }, null, delay, period).wrap()
    }

    private fun playerTaskLater(delay: Long, block: () -> Unit) {
        val bootstrap = BOOTSTRAP as BukkitBootstrapImpl
        if (bootstrap.isFolia()) {
            player.scheduler.runDelayed(bootstrap, {
                block()
            }, null, delay)
            return
        }
        taskLater(delay, block)
    }

    private fun ScheduledTask.wrap() = object : HudTask {
        override fun isCancelled(): Boolean {
            return this@wrap.isCancelled
        }

        override fun cancel() {
            this@wrap.cancel()
        }
    }
}
