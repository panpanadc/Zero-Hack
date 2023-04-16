package cum.xiaomao.zerohack.event.events.player

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.WrappedForgeEvent
import net.minecraftforge.client.event.InputUpdateEvent

class InputUpdateEvent(override val event: InputUpdateEvent) : Event, WrappedForgeEvent, EventPosting by Companion {
    val movementInput
        get() = event.movementInput

    companion object : EventBus()
}