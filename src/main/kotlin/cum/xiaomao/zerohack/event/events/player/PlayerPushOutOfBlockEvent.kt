package cum.xiaomao.zerohack.event.events.player

import cum.xiaomao.zerohack.event.*
import net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent

class PlayerPushOutOfBlockEvent(override val event: PlayerSPPushOutOfBlocksEvent) : Event, ICancellable, WrappedForgeEvent, EventPosting by Companion {
    companion object : EventBus()
}