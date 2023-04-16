package cum.xiaomao.zerohack.module.modules.movement

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.player.PlayerMoveEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.TimerManager.modifyTimer
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.BaritoneUtils
import cum.xiaomao.zerohack.util.EntityUtils.betterPosition
import cum.xiaomao.zerohack.util.EntityUtils.isFlying
import cum.xiaomao.zerohack.util.EntityUtils.isInOrAboveLiquid
import cum.xiaomao.zerohack.util.MovementUtils
import cum.xiaomao.zerohack.util.MovementUtils.applySpeedPotionEffects
import cum.xiaomao.zerohack.util.MovementUtils.calcMoveYaw
import cum.xiaomao.zerohack.util.MovementUtils.speed
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

internal object Strafe : Module(
    name = "Strafe",
    category = Category.MOVEMENT,
    description = "Improves control",
    modulePriority = 50
) {
    private val timerBoost by setting("Timer Boost", 1.09f, 1.0f..1.5f, 0.01f)
    private val groundBoost by setting("Ground Boost", false)
    private val airBoost by setting("Air Boost", true)
    private val speedPotionBoost by setting("Speed Potion Boost", true)
    private val speed by setting("Speed", 0.22, 0.1..0.5, 0.0001)
    private val sprintSpeed by setting("Sprint Speed", 0.2873, 0.1..0.5, 0.0001)
    private val autoJump by setting("Auto Jump", false)

    private var running = false
    private var burrowTicks = 0x22
    private var jumpTicks = 0

    init {
        onDisable {
            running = false
            burrowTicks = 0x22
            jumpTicks = 0
        }

        safeListener<PlayerMoveEvent.Pre>(500) {
            jumpTicks++

            val playerPos = player.betterPosition
            val box = world.getBlockState(playerPos).getCollisionBoundingBox(world, playerPos)
            if (box != null && box.maxY + playerPos.y > player.posY) {
                burrowTicks = 0
            } else {
                burrowTicks++
            }

            if (shouldStrafe()) {
                val onGround = player.onGround
                val inputting = MovementUtils.isInputting

                if (onGround && groundBoost || !onGround && airBoost) {
                    if (inputting) {
                        val yaw = calcMoveYaw()
                        var baseSpeed = if (player.isSprinting) sprintSpeed else speed
                        if (player.isSneaking) baseSpeed *= 0.2f
                        if (speedPotionBoost) baseSpeed = player.applySpeedPotionEffects(baseSpeed)

                        val speed = max(player.speed, baseSpeed)

                        it.x = -sin(yaw) * speed
                        it.z = cos(yaw) * speed
                        if (burrowTicks >= 10) modifyTimer(50.0f / timerBoost)

                        running = true
                    } else if (running) {
                        player.motionX = 0.0
                        player.motionZ = 0.0
                        running = false
                    }
                }

                if (autoJump && inputting && onGround && jumpTicks >= 5) {
                    player.jump()
                    it.x *= 1.2
                    it.z *= 1.2
                }
            }
        }
    }

    private fun SafeClientEvent.shouldStrafe(): Boolean {
        return !player.isOnLadder
            && Flight.isDisabled
            && !BaritoneUtils.isPathing
            && !player.isFlying
            && !player.isInOrAboveLiquid
    }
}