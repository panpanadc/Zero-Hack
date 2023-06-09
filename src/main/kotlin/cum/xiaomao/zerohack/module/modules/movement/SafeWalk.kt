package cum.xiaomao.zerohack.module.modules.movement

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.mixins.core.entity.MixinEntity
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.module.modules.player.Scaffold
import cum.xiaomao.zerohack.util.BaritoneUtils
import cum.xiaomao.zerohack.util.Wrapper
import cum.xiaomao.zerohack.util.extension.fastFloor
import cum.xiaomao.zerohack.util.threads.runSafeOrFalse
import net.minecraft.util.math.BlockPos

/**
 * @see MixinEntity.moveInvokeIsSneakingPre
 * @see MixinEntity.moveInvokeIsSneakingPost
 */
internal object SafeWalk : Module(
    name = "SafeWalk",
    category = Category.MOVEMENT,
    description = "Keeps you from walking off edges"
) {
    private val checkFallDist by setting("Check Fall Distance", true, description = "Check fall distance from edge")

    init {
        onToggle {
            BaritoneUtils.settings?.assumeSafeWalk?.value = it
        }
    }

    @JvmStatic
    fun shouldSafewalk(entityID: Int, motionX: Double, motionZ: Double): Boolean {
        return runSafeOrFalse {
            !player.isSneaking && player.entityId == entityID
                && (isEnabled || isEnabled && Scaffold.safeWalk || isEnabled)
                && (!checkFallDist && !BaritoneUtils.isPathing || !isEdgeSafe(motionX, motionZ))
        }
    }


    @JvmStatic
    fun setSneaking(state: Boolean) {
        Wrapper.player?.movementInput?.sneak = state
    }

    private fun isEdgeSafe(motionX: Double, motionZ: Double): Boolean {
        return runSafeOrFalse {
            checkFallDist(player.posX, player.posZ)
                && checkFallDist(player.posX + motionX, player.posZ + motionZ)
        }
    }

    private fun SafeClientEvent.checkFallDist(posX: Double, posZ: Double): Boolean {
        val startY = (player.posY - 0.5).fastFloor()
        val pos = BlockPos.PooledMutableBlockPos.retain(posX.fastFloor(), startY, posZ.fastFloor())

        for (y in startY downTo startY - 2) {
            pos.y = y
            if (world.getBlockState(pos).getCollisionBoundingBox(world, pos) != null) {
                pos.release()
                return true
            }
        }

        pos.y = startY - 3
        val box = world.getBlockState(pos).getCollisionBoundingBox(world, pos)
        pos.release()

        return box != null && box.maxY >= 1.0
    }
}