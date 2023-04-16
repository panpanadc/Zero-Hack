package cum.xiaomao.zerohack.event.events.render

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting

class ResolutionUpdateEvent(val width: Int, val height: Int) : Event, EventPosting by Companion {
    companion object : EventBus()
}