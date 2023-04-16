package cum.xiaomao.zerohack.event.events.render

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.NamedProfilerEventBus

sealed class Render2DEvent : Event {
    object Mc : Render2DEvent(), EventPosting by NamedProfilerEventBus("mc")
    object Absolute : Render2DEvent(), EventPosting by NamedProfilerEventBus("absolute")
    object Troll : Render2DEvent(), EventPosting by NamedProfilerEventBus("troll")
}