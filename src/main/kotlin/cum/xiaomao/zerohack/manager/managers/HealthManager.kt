package cum.xiaomao.zerohack.manager.managers

import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import cum.xiaomao.zerohack.event.events.EntityEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.WorldEvent
import cum.xiaomao.zerohack.event.events.combat.TotemPopEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.util.accessor.entityID
import cum.xiaomao.zerohack.mixins.accessor.entity.AccessorEntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketAnimation
import net.minecraft.network.play.server.SPacketEntityMetadata
import net.minecraft.network.play.server.SPacketEntityStatus

object HealthManager : Manager() {
    private val trackerMap = Int2ObjectMaps.synchronize(Int2ObjectOpenHashMap<Tracker>())

    init {
        listener<WorldEvent.Entity.Remove>(true) {
            trackerMap.remove(it.entity.entityId)
        }

        listener<TotemPopEvent.Pop>(true) {
            val tracker = getTracker(it.entity)
            tracker.lastDamage += tracker.health
        }

        safeListener<PacketEvent.Receive>(true) { event ->
            when (event.packet) {
                is SPacketAnimation -> {
                    if (event.packet.animationType == 1) {
                        trackerMap[event.packet.entityID]?.hurtTime = System.currentTimeMillis()
                    }
                }
                is SPacketEntityStatus -> {
                    when (event.packet.opCode.toInt()) {
                        2, 33, 36, 37 -> {
                            trackerMap[event.packet.entityID]?.hurtTime = System.currentTimeMillis()
                        }
                    }
                }
                is SPacketEntityMetadata -> {
                    val dataManagerEntries = event.packet.dataManagerEntries ?: return@safeListener
                    val entity = world.getEntityByID(event.packet.entityId) as? EntityPlayer? ?: return@safeListener
                    val tracker = trackerMap.computeIfAbsent(entity.entityId) {
                        Tracker(entity)
                    }

                    val entry = dataManagerEntries.find {
                        it.isDirty && it.key == runCatching { AccessorEntityLivingBase.trollGetHealthDataKey() }.getOrNull()
                    } ?: return@safeListener

                    (entry.value as? Float)?.let { health ->
                        val prevHealth = tracker.health
                        val diff = prevHealth - health
                        if (diff > 0.0f
                            && (diff > 2.0f || TotemPopManager.getTracker(entity)?.let { System.currentTimeMillis() - it.popTime > 10L } == true)) {
                            tracker.lastDamage += diff
                        }

                        EntityEvent.UpdateHealth(entity, prevHealth, health).post()
                        tracker.health = health
                    }
                }
            }
        }
    }

    fun getTracker(entity: EntityPlayer): Tracker {
        return trackerMap.computeIfAbsent(entity.entityId) {
            Tracker(entity)
        }
    }

    class Tracker(val entity: EntityPlayer) {
        var health = entity.health
        var lastDamage = 0.0f
        var hurtTime = 0L
            set(value) {
                lastDamage = 0.0f
                field = value
            }

        init {
            val hurtTimeTicks = entity.hurtTime
            if (hurtTimeTicks != 0) {
                hurtTime = System.currentTimeMillis() - hurtTimeTicks * 50L
            }
        }
    }
}