package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting

sealed class ConnectionEvent : Event {
    object Connect : ConnectionEvent(), EventPosting by EventBus()
    object Disconnect : ConnectionEvent(), EventPosting by EventBus()
}