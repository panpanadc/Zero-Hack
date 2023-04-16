package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.NamedProfilerEventBus

sealed class RunGameLoopEvent : Event {
    object Start : RunGameLoopEvent(), EventPosting by NamedProfilerEventBus("start")
    object Tick : RunGameLoopEvent(), EventPosting by NamedProfilerEventBus("tick")
    object Render : RunGameLoopEvent(), EventPosting by NamedProfilerEventBus("render")
    object End : RunGameLoopEvent(), EventPosting by NamedProfilerEventBus("end")
}