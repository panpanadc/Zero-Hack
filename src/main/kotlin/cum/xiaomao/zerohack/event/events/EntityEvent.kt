package cum.xiaomao.zerohack.event.events

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.entity.EntityLivingBase

sealed class EntityEvent(val entity: EntityLivingBase) : Event {
    class UpdateHealth(entity: EntityLivingBase, val prevHealth: Float, val health: Float) : EntityEvent(entity), EventPosting by Companion {
        companion object : EventBus()
    }

    class Death(entity: EntityLivingBase) : EntityEvent(entity), EventPosting by Companion {
        companion object : EventBus()
    }
}