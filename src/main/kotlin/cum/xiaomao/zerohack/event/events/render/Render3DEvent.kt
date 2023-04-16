package cum.xiaomao.zerohack.event.events.render

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.event.NamedProfilerEventBus

object Render3DEvent : Event, EventPosting by NamedProfilerEventBus("trollRender3D")