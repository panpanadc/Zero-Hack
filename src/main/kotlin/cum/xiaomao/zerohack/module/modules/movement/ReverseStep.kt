package cum.xiaomao.zerohack.module.modules.movement

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.player.PlayerTravelEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.isInOrAboveLiquid
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.world.getGroundLevel
import net.minecraft.network.play.server.SPacketPlayerPosLook

internal object ReverseStep : Module(
    name = "ReverseStep",
    description = "Walks down edge of block faster",
    category = Category.MOVEMENT
) {
    private val height by setting("Height", 2.0f, 0.25f..8.0f, 0.1f)
    private val speed by setting("Speed", 1.0f, 0.1f..8.0f, 0.1f)

    private val timer = TickTimer()

    init {
        listener<PacketEvent.Receive> {
            if (it.packet is SPacketPlayerPosLook) {
                timer.reset()
            }
        }

        safeListener<PlayerTravelEvent>(100) {
            if (shouldRun()) {
                player.motionY -= speed.toDouble()
                Speed.resetReverseStep()
            }
        }
    }

    private fun SafeClientEvent.shouldRun(): Boolean {
        return !mc.gameSettings.keyBindSneak.isKeyDown
            && !mc.gameSettings.keyBindJump.isKeyDown
            && !player.isElytraFlying
            && !player.capabilities.isFlying
            && !player.isOnLadder
            && !player.isInOrAboveLiquid
            && player.onGround
            && player.motionY in -0.08..0.0
            && timer.tick(3000L)
            && checkGroundLevel()
    }

    private fun SafeClientEvent.checkGroundLevel(): Boolean {
        return player.posY - world.getGroundLevel(player) in 0.25..height.toDouble()
            || player.posY - world.getGroundLevel(player.entityBoundingBox.offset(player.motionX, 0.0, player.motionZ)) in 0.25..height.toDouble()
            || player.posY - world.getGroundLevel(player.entityBoundingBox.offset(player.motionX * 2.0, 0.0, player.motionZ * 2.0)) in 0.25..height.toDouble()
    }
}