package cum.xiaomao.zerohack.event.events.combat

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import net.minecraft.entity.player.EntityPlayer

sealed class TotemPopEvent(val name: String, val count: Int) : Event {
    class Pop(val entity: EntityPlayer, count: Int) : TotemPopEvent(entity.name, count), EventPosting by Companion {
        companion object : EventBus()
    }

    class Death(val entity: EntityPlayer, count: Int) : TotemPopEvent(entity.name, count), EventPosting by Companion {
        companion object : EventBus()
    }

    class Clear(name: String, count: Int) : TotemPopEvent(name, count), EventPosting by Companion {
        companion object : EventBus()
    }
}
