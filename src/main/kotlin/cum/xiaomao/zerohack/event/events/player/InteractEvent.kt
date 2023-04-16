package cum.xiaomao.zerohack.event.events.player

import cum.xiaomao.zerohack.event.Cancellable
import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

sealed class InteractEvent : Cancellable(), Event {
    sealed class Block(
        val pos: BlockPos,
        val side: EnumFacing,
        val hand: EnumHand
    ) : InteractEvent() {
        class LeftClick(pos: BlockPos, side: EnumFacing) : Block(pos, side, EnumHand.MAIN_HAND), EventPosting by Companion {
            companion object : EventBus()
        }

        class RightClick(pos: BlockPos, side: EnumFacing) : Block(pos, side, EnumHand.MAIN_HAND), EventPosting by Companion {
            companion object : EventBus()
        }

        class Damage(pos: BlockPos, side: EnumFacing) : Block(pos, side, EnumHand.MAIN_HAND), EventPosting by Companion {
            companion object : EventBus()
        }
    }

    sealed class Item(
        val hand: EnumHand
    ) : InteractEvent() {
        class RightClick(hand: EnumHand) : Item(hand), EventPosting by Companion {
            companion object : EventBus()
        }
    }
}