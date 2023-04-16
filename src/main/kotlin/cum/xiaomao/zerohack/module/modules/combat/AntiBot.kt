package cum.xiaomao.zerohack.module.modules.combat

import com.google.common.collect.MapMaker
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.events.WorldEvent
import cum.xiaomao.zerohack.event.events.player.PlayerAttackEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeConcurrentListener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.managers.EntityManager
import cum.xiaomao.zerohack.manager.managers.UUIDManager
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.isFakeOrSelf
import cum.xiaomao.zerohack.util.math.vector.Vec2d
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.abs

internal object AntiBot : Module(
    name = "AntiBot",
    description = "Avoid attacking fake players",
    category = Category.COMBAT,
    alwaysListening = true
) {
    private val tabList by setting("Tab List", true)
    private val ping by setting("Ping", true)
    private val hp by setting("HP", true)
    private val sleeping by setting("Sleeping", false)
    private val hoverOnTop by setting("Hover On Top", true)
    private val ticksExists by setting("Ticks Exists", 200, 0..500, 10)
    private val mojangApi by setting("Mojang API", true)

    private val botMap = MapMaker().weakKeys().makeMap<EntityPlayer, Boolean>()
    private var count = 0

    override fun getHudInfo(): String {
        return count.toString()
    }

    init {
        onDisable {
            botMap.clear()
            count = 0
        }

        listener<ConnectionEvent.Disconnect> {
            botMap.clear()
        }

        listener<PlayerAttackEvent> {
            if (isBot(it.entity)) it.cancel()
        }

        safeListener<WorldEvent.Entity.Add> {
            if (it.entity is EntityPlayer) {
                val isBot = checkBot(it.entity)
                botMap[it.entity] = isBot
            }
        }

        safeConcurrentListener<TickEvent.Pre> {
            var newCount = 0

            for (entity in EntityManager.players) {
                val isBot = checkBot(entity)
                botMap[entity] = isBot
                if (isBot) newCount++
            }

            count = newCount
        }
    }

    fun isBot(entity: Entity): Boolean {
        return isEnabled
            && entity is EntityPlayer
            && !entity.isFakeOrSelf
            && botMap[entity] ?: true
    }

    private fun SafeClientEvent.checkBot(entity: EntityPlayer): Boolean {
        return (entity.name == player.name
            || tabList && connection.getPlayerInfo(entity.name) == null
            || ping && (connection.getPlayerInfo(entity.name)?.responseTime ?: -1) <= 0
            || hp && entity.health !in 0.0f..20.0f
            || sleeping && entity.isPlayerSleeping && !entity.onGround
            || hoverOnTop && hoverCheck(entity)
            || entity.ticksExisted < ticksExists
            || mojangApi && UUIDManager.getByName(entity.name) == null)
    }

    private fun SafeClientEvent.hoverCheck(entity: EntityPlayer): Boolean {
        val distXZ = Vec2d(entity.posX, entity.posZ).minus(player.posX, player.posZ).lengthSquared()
        return distXZ < 16 && entity.posY - player.posY > 2.0 && abs(entity.posY - entity.prevPosY) < 0.1
    }
}
