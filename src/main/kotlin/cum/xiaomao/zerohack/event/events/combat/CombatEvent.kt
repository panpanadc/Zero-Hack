package cum.xiaomao.zerohack.event.events.combat

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.entity.EntityLivingBase

sealed class CombatEvent : Event {
    abstract val entity: EntityLivingBase?

    class UpdateTarget(val prevEntity: EntityLivingBase?, override val entity: EntityLivingBase?) : CombatEvent(), EventPosting by Companion {
        companion object : EventBus()
    }
}
