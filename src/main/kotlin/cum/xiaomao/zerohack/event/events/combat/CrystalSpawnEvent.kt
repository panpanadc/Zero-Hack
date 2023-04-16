package cum.xiaomao.zerohack.event.events.combat

import cum.xiaomao.zerohack.event.Event
import cum.xiaomao.zerohack.event.EventBus
import cum.xiaomao.zerohack.event.EventPosting
import cum.xiaomao.zerohack.util.combat.CrystalDamage

class CrystalSpawnEvent(
    val entityID: Int,
    val crystalDamage: CrystalDamage
) : Event, EventPosting by Companion {
    companion object : EventBus()
}