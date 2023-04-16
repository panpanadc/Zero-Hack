package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.NamedProfilerEventBus

sealed class ProcessKeyBindEvent : Event {
    object Pre : ProcessKeyBindEvent(), EventPosting by NamedProfilerEventBus("pre")
    object Post : ProcessKeyBindEvent(), EventPosting by NamedProfilerEventBus("post")
}