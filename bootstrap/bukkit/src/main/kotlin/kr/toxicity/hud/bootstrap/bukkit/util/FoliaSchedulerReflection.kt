package kr.toxicity.hud.bootstrap.bukkit.util

import kr.toxicity.hud.api.scheduler.HudTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.function.Consumer

internal object FoliaSchedulerReflection {
    private val entitySchedulerClass by lazy {
        Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler")
    }
    private val getSchedulerMethod by lazy {
        Player::class.java.getMethod("getScheduler")
    }
    private val executeMethod by lazy {
        entitySchedulerClass.getMethod(
            "execute",
            Plugin::class.java,
            Runnable::class.java,
            Runnable::class.java,
            Long::class.javaPrimitiveType
        )
    }
    private val runDelayedMethod by lazy {
        entitySchedulerClass.getMethod(
            "runDelayed",
            Plugin::class.java,
            Consumer::class.java,
            Runnable::class.java,
            Long::class.javaPrimitiveType
        )
    }
    private val runAtFixedRateMethod by lazy {
        entitySchedulerClass.getMethod(
            "runAtFixedRate",
            Plugin::class.java,
            Consumer::class.java,
            Runnable::class.java,
            Long::class.javaPrimitiveType,
            Long::class.javaPrimitiveType
        )
    }
    private val taskIsCancelledMethod by lazy {
        Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask")
            .getMethod("isCancelled")
    }
    private val taskCancelMethod by lazy {
        Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask")
            .getMethod("cancel")
    }

    fun execute(plugin: Plugin, player: Player, delay: Long, block: () -> Unit): Boolean {
        return runCatching {
            executeMethod.invoke(
                getSchedulerMethod.invoke(player),
                plugin,
                Runnable {
                    block()
                },
                null,
                delay
            ) as Boolean
        }.getOrDefault(false)
    }

    fun runDelayed(plugin: Plugin, player: Player, delay: Long, block: () -> Unit): HudTask? {
        return runCatching {
            val task = runDelayedMethod.invoke(
                getSchedulerMethod.invoke(player),
                plugin,
                Consumer<Any> {
                    block()
                },
                null,
                delay
            ) ?: return@runCatching null
            ReflectionHudTask(task)
        }.getOrNull()
    }

    fun runAtFixedRate(plugin: Plugin, player: Player, delay: Long, period: Long, block: () -> Unit): HudTask? {
        return runCatching {
            val task = runAtFixedRateMethod.invoke(
                getSchedulerMethod.invoke(player),
                plugin,
                Consumer<Any> {
                    block()
                },
                null,
                delay,
                period
            ) ?: return@runCatching null
            ReflectionHudTask(task)
        }.getOrNull()
    }

    fun cancelGlobalAndAsyncTasks(plugin: Plugin) {
        cancelTasks("getGlobalRegionScheduler", "io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler", plugin)
        cancelTasks("getAsyncScheduler", "io.papermc.paper.threadedregions.scheduler.AsyncScheduler", plugin)
    }

    private fun cancelTasks(accessor: String, schedulerType: String, plugin: Plugin) {
        runCatching {
            val scheduler = Bukkit::class.java.getMethod(accessor).invoke(null)
            Class.forName(schedulerType).getMethod("cancelTasks", Plugin::class.java).invoke(scheduler, plugin)
        }
    }

    private class ReflectionHudTask(
        private val task: Any
    ) : HudTask {
        override fun isCancelled(): Boolean {
            return taskIsCancelledMethod.invoke(task) as? Boolean ?: false
        }

        override fun cancel() {
            taskCancelMethod.invoke(task)
        }
    }
}
