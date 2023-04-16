package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.RunGameLoopEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.SwingMode
import net.minecraft.network.play.client.CPacketAnimation

internal object NoSwing : Module(
    name = "NoSwing",
    category = Category.PLAYER,
    description = "Cancels server or client swing animation"
) {
    private val mode by setting("Mode", SwingMode.CLIENT)

    init {
        listener<PacketEvent.Send> {
            if (mode == SwingMode.PACKET && it.packet is CPacketAnimation) it.cancel()
        }

        safeListener<RunGameLoopEvent.Render> {
            player.isSwingInProgress = false
            player.swingProgressInt = 0
            player.swingProgress = 0.0f
            player.prevSwingProgress = 0.0f
        }
    }
}