package cum.xiaomao.zerohack.event.events.baritone

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting

class BaritoneCommandEvent(val command: String) : Event, EventPosting by Companion {
    companion object : EventBus()
}