package cum.xiaomao.zerohack.manager.managers

import cum.xiaomao.zerohack.event.events.RunGameLoopEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.util.accessor.tickLength
import cum.xiaomao.zerohack.util.accessor.timer
import cum.xiaomao.zerohack.util.extension.lastValueOrNull
import cum.xiaomao.zerohack.util.extension.synchronized
import cum.xiaomao.zerohack.util.graphics.RenderUtils3D
import cum.xiaomao.zerohack.util.threads.runSafe
import java.util.*
import kotlin.math.roundToInt

object TimerManager : Manager() {
    private val modifiers = TreeMap<AbstractModule, Modifier>().synchronized()
    private var modified = false

    var totalTicks = Int.MIN_VALUE
    var tickLength = 50.0f; private set

    init {
        listener<RunGameLoopEvent.Start>(Int.MAX_VALUE, true) {
            runSafe {
                synchronized(modifiers) {
                    modifiers.values.removeIf { it.endTick < totalTicks }
                    modifiers.lastValueOrNull()?.let {
                        mc.timer.tickLength = it.tickLength
                    } ?: return@runSafe null
                }

                modified = true
            } ?: run {
                modifiers.clear()
                if (modified) {
                    mc.timer.tickLength = 50.0f
                    modified = false
                }
            }

            tickLength = mc.timer.tickLength
        }

        listener<TickEvent.Pre>(Int.MAX_VALUE, true) {
            totalTicks++
        }
    }

    fun AbstractModule.resetTimer() {
        modifiers.remove(this)
    }

    fun AbstractModule.modifyTimer(tickLength: Float, timeoutTicks: Int = 1) {
        runSafe {
            modifiers[this@modifyTimer] = Modifier(tickLength, totalTicks + RenderUtils3D.partialTicks.roundToInt() + timeoutTicks)
        }
    }

    private class Modifier(
        val tickLength: Float,
        val endTick: Int
    )
}