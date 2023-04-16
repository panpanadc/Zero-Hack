package cum.xiaomao.zerohack.event.events.player

import cum.xiaomao.zerohack.event.*

class PlayerTravelEvent : Event, ICancellable by Cancellable(), EventPosting by Companion {
    companion object : NamedProfilerEventBus("trollPlayerTravel")
}