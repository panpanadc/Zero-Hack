package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeConcurrentListener
import cum.xiaomao.zerohack.manager.managers.CombatManager
import cum.xiaomao.zerohack.manager.managers.HoleManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module

import cum.xiaomao.zerohack.module.modules.player.PacketMine
import cum.xiaomao.zerohack.util.EntityUtils.betterPosition
import cum.xiaomao.zerohack.util.TickTimer
import net.minecraft.util.math.BlockPos

@CombatManager.CombatModule
internal object BurrowMiner : Module(
    name = "BurrowMiner",
    category = Category.COMBAT,
    description = "Mines your opponent's burrow"
) {
    private val timeout by setting("Timeout", 5000, 0..10000, 100)

    private val timeoutTimer = TickTimer()
    private var lastPos: BlockPos? = null

    init {
        onEnable {
            PacketMine.enable()
        }

        onDisable {
            lastPos = null
            timeoutTimer.reset(-69420L)
            reset()
        }

        safeConcurrentListener<TickEvent.Post> {
            CombatManager.target?.let { target ->
                val pos = target.betterPosition
                if (pos == player.betterPosition) return@safeConcurrentListener
                val burrow = Burrow.isBurrowed(target)
                val isHole = HoleManager.getHoleInfo(pos).isHole

                if (burrow || isHole) {
                    val priority = if (burrow || pos == lastPos) 80 else -100
                    PacketMine.mineBlock(BurrowMiner, pos, priority)
                    timeoutTimer.reset()
                    lastPos = pos
                } else if (pos != lastPos || timeoutTimer.tick(timeout)) {
                    reset()
                }
            } ?: reset()
        }
    }

    private fun reset() {
        PacketMine.reset(this)
    }
}