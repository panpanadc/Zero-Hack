package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TpsCalculator
import net.minecraft.network.play.client.CPacketAnimation

internal object SwingLimiter : Module(
    name = "SwingLimiter",
    category = Category.PLAYER,
    description = "Limits swing packet",
    modulePriority = 2000
) {
    private val ticks by setting("Ticks", 10, 0..40, 1)
    private val tpsSync by setting("Tps Sync", true)

    private val swingTimer = TickTimer()

    init {
        listener<PacketEvent.Send>(-9999) {
            if (it.packet is CPacketAnimation) {
                if (checkSwingDelay()) {
                    swingTimer.reset()
                } else {
                    it.cancel()
                }
            }
        }
    }

    @JvmStatic
    fun checkSwingDelay(): Boolean {
        return swingTimer.tick(
            if (tpsSync) (ticks * TpsCalculator.multiplier * 50.0f).toLong()
            else ticks * 50L
        )
    }
}