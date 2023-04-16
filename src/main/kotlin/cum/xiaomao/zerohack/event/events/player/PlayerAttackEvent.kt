package cum.xiaomao.zerohack.event.events.player

import cum.xiaomao.zerohack.event.Cancellable
import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.entity.Entity

class PlayerAttackEvent(val entity: Entity) : Event, Cancellable(), EventPosting by Companion {
    companion object : EventBus()
}