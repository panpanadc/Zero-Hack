package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.NamedProfilerEventBus

sealed class TickEvent : Event {
    object Pre : TickEvent(), EventPosting by NamedProfilerEventBus("trollTickPre")
    object Post : TickEvent(), EventPosting by NamedProfilerEventBus("trollTickPost")
}