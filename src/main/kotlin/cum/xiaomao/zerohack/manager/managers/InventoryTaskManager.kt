package cum.xiaomao.zerohack.manager.managers

import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.RunGameLoopEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TpsCalculator
import cum.xiaomao.zerohack.util.inventory.ClickFuture
import cum.xiaomao.zerohack.util.inventory.InventoryTask
import cum.xiaomao.zerohack.util.inventory.StepFuture
import cum.xiaomao.zerohack.util.inventory.removeHoldingItem
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.network.play.server.SPacketConfirmTransaction
import java.util.*

object InventoryTaskManager : Manager() {
    private val confirmMap = Short2ObjectOpenHashMap<ClickFuture>()
    private val taskQueue = PriorityQueue<InventoryTask>()
    private val timer = TickTimer()
    private var lastTask: InventoryTask? = null

    init {
        listener<PacketEvent.Receive> {
            if (it.packet !is SPacketConfirmTransaction) return@listener
            synchronized(InventoryTaskManager) {
                confirmMap.remove(it.packet.actionNumber)?.confirm()
            }
        }

        safeListener<RunGameLoopEvent.Render> {
            if (lastTask == null && taskQueue.isEmpty()) return@safeListener
            if (!timer.tick(0L)) return@safeListener

            lastTaskOrNext()?.let {
                runTask(it)
            }
        }

        listener<ConnectionEvent.Disconnect> {
            reset()
        }
    }

    fun addTask(task: InventoryTask) {
        synchronized(InventoryTaskManager) {
            taskQueue.add(task)
        }
    }

    fun runNow(event: SafeClientEvent, task: InventoryTask) {
        event {
            if (!player.inventory.itemStack.isEmpty) {
                removeHoldingItem()
            }

            while (!task.finished) {
                task.runTask(event)?.let {
                    handleFuture(it)
                }
            }

            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
        }
    }

    private fun SafeClientEvent.lastTaskOrNext(): InventoryTask? {
        return lastTask ?: run {
            val newTask = synchronized(InventoryTaskManager) {
                taskQueue.poll()?.also { lastTask = it }
            } ?: return null

            if (!player.inventory.itemStack.isEmpty) {
                removeHoldingItem()
                return null
            }

            newTask
        }
    }

    private fun SafeClientEvent.runTask(task: InventoryTask) {
        if (mc.currentScreen is GuiContainer && !task.runInGui && !player.inventory.itemStack.isEmpty) {
            timer.reset(500L)
            return
        }

        if (task.delay == 0L) {
            runNow(this, task)
        } else {
            task.runTask(this)?.let {
                handleFuture(it)
                timer.reset((task.delay * TpsCalculator.multiplier).toLong())
            }
        }

        if (task.finished) {
            timer.reset((task.postDelay * TpsCalculator.multiplier).toLong())
            lastTask = null
            return
        }
    }

    private fun handleFuture(future: StepFuture) {
        if (future is ClickFuture) {
            synchronized(InventoryTaskManager) {
                confirmMap[future.id] = future
            }
        }
    }

    private fun reset() {
        synchronized(InventoryTaskManager) {
            confirmMap.clear()
            lastTask?.cancel()
            lastTask = null
            taskQueue.clear()
        }
    }

}