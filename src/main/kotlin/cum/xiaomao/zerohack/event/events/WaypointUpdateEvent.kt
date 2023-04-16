package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.manager.managers.WaypointManager.Waypoint

class WaypointUpdateEvent(val type: Type, val waypoint: Waypoint?) : Event, EventPosting by Companion {
    enum class Type {
        GET, ADD, REMOVE, CLEAR, RELOAD
    }

    companion object : EventBus()
}