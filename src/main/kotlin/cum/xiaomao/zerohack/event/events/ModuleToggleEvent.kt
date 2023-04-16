package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.module.AbstractModule

class ModuleToggleEvent internal constructor(val module: AbstractModule) : Event, EventPosting by Companion {
    companion object : EventBus()
}