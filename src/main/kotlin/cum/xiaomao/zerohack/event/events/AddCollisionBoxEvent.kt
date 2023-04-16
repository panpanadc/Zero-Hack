package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.block.Block
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

class AddCollisionBoxEvent(
    val entity: Entity?,
    val entityBox: AxisAlignedBB,
    val pos: BlockPos,
    val block: Block,
    val collidingBoxes: MutableList<AxisAlignedBB>
) : Event, EventPosting by Companion {
    companion object : EventBus()
}