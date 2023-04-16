package cum.xiaomao.zerohack.module.modules.movement

import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.AddCollisionBoxEvent
import cum.xiaomao.zerohack.event.events.player.PlayerMoveEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.isFlying
import cum.xiaomao.zerohack.util.accessor.isInWeb
import cum.xiaomao.zerohack.util.extension.fastFloor
import cum.xiaomao.zerohack.util.world.getBlock
import net.minecraft.init.Blocks
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

internal object AntiWeb : Module(
    name = "AntiWeb",
    description = "Prevents walking into web",
    category = Category.MOVEMENT
) {
    private val speedMultiplier by setting("Speed Multiplier", 0.8f, 0.1f..1.0f, 0.1f)

    init {
        safeListener<AddCollisionBoxEvent> {
            if (it.entity == player && it.block == Blocks.WEB) {
                it.collidingBoxes.add(
                    AxisAlignedBB(
                        it.pos.x.toDouble(), it.pos.y.toDouble(), it.pos.z.toDouble(),
                        it.pos.x + 1.0, it.pos.y + 1.0, it.pos.z + 1.0
                    )
                )
            }
        }

        safeListener<PlayerMoveEvent.Pre>(-2000) {
            if (!player.isFlying && player.onGround && player.motionY <= 0.0 && player.motionY >= -0.08 && !player.isInWeb && isAboveWeb()) {
                it.x = player.motionX * speedMultiplier
                it.z = player.motionZ * speedMultiplier
            }
        }
    }

    private fun SafeClientEvent.isAboveWeb(): Boolean {
        val box = player.entityBoundingBox
        val pos = BlockPos.PooledMutableBlockPos.retain()
        val y = (player.posY - 0.08).fastFloor()

        for (x in box.minX.fastFloor()..box.maxX.fastFloor()) {
            for (z in box.minZ.fastFloor()..box.maxZ.fastFloor()) {
                if (world.getBlock(pos.setPos(x, y, z)) != Blocks.WEB) {
                    pos.release()
                    return false
                }
            }
        }

        pos.release()
        return true
    }
}