package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.block.state.IBlockState
import net.minecraft.util.math.BlockPos
import net.minecraft.entity.Entity as McEntity

sealed class WorldEvent : Event {
    object Unload : WorldEvent(), EventPosting by EventBus()
    object Load : WorldEvent(), EventPosting by EventBus()

    sealed class Entity(val entity: McEntity) : WorldEvent() {
        class Add(entity: McEntity) : Entity(entity), EventPosting by Companion {
            companion object : EventBus()
        }

        class Remove(entity: McEntity) : Entity(entity), EventPosting by Companion {
            companion object : EventBus()
        }
    }

    class BlockUpdate(
        val pos: BlockPos,
        val oldState: IBlockState,
        val newState: IBlockState
    ) : WorldEvent(), EventPosting by Companion {
        companion object : EventBus()
    }

    class RenderUpdate(
        val x1: Int,
        val y1: Int,
        val z1: Int,
        val x2: Int,
        val y2: Int,
        val z2: Int
    ) : WorldEvent(), EventPosting by Companion {
        companion object : EventBus()
    }
}
