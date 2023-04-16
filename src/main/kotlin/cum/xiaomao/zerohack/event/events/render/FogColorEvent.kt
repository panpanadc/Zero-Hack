package cum.xiaomao.zerohack.event.events.render

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.WrappedForgeEvent
import net.minecraftforge.client.event.EntityViewRenderEvent

class FogColorEvent(override val event: EntityViewRenderEvent.FogColors) : Event, WrappedForgeEvent, EventPosting by Companion {
    var red by event::red
    var green by event::green
    var blue by event::blue

    companion object : EventBus()
}