package kr.toxicity.hud.bootstrap.bukkit.player

import kr.toxicity.hud.api.adapter.LocationWrapper
import kr.toxicity.hud.api.adapter.WorldWrapper
import kr.toxicity.hud.api.scheduler.HudTask
import kr.toxicity.hud.bootstrap.bukkit.BukkitBootstrapImpl
import kr.toxicity.hud.bootstrap.bukkit.util.FoliaSchedulerReflection
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
    private val audience: Audience,
    private val schedulerPlayer: Player = player
) : HudPlayerImpl() {
    private var bossBarRestoreTask: HudTask? = null

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
        val bootstrap = BOOTSTRAP as BukkitBootstrapImpl
        if (!bootstrap.isFolia()) {
            return super.hudUpdateTask(speed, block)
        }
        return FoliaSchedulerReflection.runAtFixedRate(bootstrap, schedulerPlayer, 1, speed, block) ?: CANCELLED_TASK
    }

    override fun hudLocationProvideTask(speed: Long, block: () -> Unit): HudTask {
        val bootstrap = BOOTSTRAP as BukkitBootstrapImpl
        if (!bootstrap.isFolia()) {
            return super.hudLocationProvideTask(speed, block)
        }
        return FoliaSchedulerReflection.runAtFixedRate(bootstrap, schedulerPlayer, speed, speed, block) ?: CANCELLED_TASK
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
        initializeTasks()
    }

    private fun initBossBar(action: () -> Unit) {
        cancelPlatformTasks()
        val bars = ArrayList<BossBar>()
        for (bossBar in Bukkit.getBossBars()) {
            if (bossBar.players.any {
                it.uniqueId == schedulerPlayer.uniqueId
            }) {
                bossBar.removePlayer(schedulerPlayer)
                bars += bossBar
            }
        }
        action()
        var scheduled: HudTask? = null
        scheduled = playerTaskLater(20) {
            bars.forEach {
                it.addPlayer(schedulerPlayer)
            }
            if (bossBarRestoreTask === scheduled) {
                bossBarRestoreTask = null
            }
        }
        bossBarRestoreTask = scheduled
    }

    override fun hasPermission(perm: String): Boolean = player.hasPermission(perm)

    override fun cancelPlatformTasks() {
        bossBarRestoreTask?.cancel()
        bossBarRestoreTask = null
    }

    private fun playerTaskLater(delay: Long, block: () -> Unit): HudTask {
        val bootstrap = BOOTSTRAP as BukkitBootstrapImpl
        if (bootstrap.isFolia()) {
            return FoliaSchedulerReflection.runDelayed(bootstrap, schedulerPlayer, delay, block) ?: CANCELLED_TASK
        }
        return taskLater(delay, block)
    }

    fun schedulerPlayer(): Player = schedulerPlayer

    companion object {
        private val CANCELLED_TASK = object : HudTask {
            override fun isCancelled(): Boolean = true

            override fun cancel() {
            }
        }
    }
}
