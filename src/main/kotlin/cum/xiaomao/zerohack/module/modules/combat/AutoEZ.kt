package cum.xiaomao.zerohack.module.modules.combat

import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.ints.IntSets
import cum.xiaomao.zerohack.event.SafeClientEvent
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.gui.hudgui.elements.client.Notification
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.EntityUtils.isFakeOrSelf
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.accessor.textComponent
import cum.xiaomao.zerohack.util.atValue
import cum.xiaomao.zerohack.util.extension.synchronized
import cum.xiaomao.zerohack.util.interfaces.DisplayEnum
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import cum.xiaomao.zerohack.util.text.formatValue
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.TextFormatting
import java.util.*

internal object AutoEZ : Module(
    name = "AutoEZ",
    category = Category.COMBAT,
    description = "Sends an insult in chat after killing someone"
) {
    private const val UNCHANGED = "Unchanged"
    private const val NAME = "\$NAME"

    private val detectMode by setting("Detect Mode", DetectMode.HEALTH)
    private val messageMode0 = setting("Message Mode", MessageMode.ONTOP)
    private val messageMode by messageMode0
    private val customText by setting("Custom Text", UNCHANGED, messageMode0.atValue(MessageMode.CUSTOM))
    private val notification by setting("Notification", true)

    private enum class DetectMode {
        BROADCAST, HEALTH
    }

    @Suppress("UNUSED")
    private enum class MessageMode(override val displayName: CharSequence, val text: String) : DisplayEnum {
        OWN("Owns me and all", "Good fight $NAME! Zero Hack owns me and all"),
        GG("GG", "gg, $NAME"),
        ONTOP("On top", "Zero Hack on top! ez $NAME"),
        EZD("Ez'd", "You just got ez'd $NAME"),
        NAENAE("Naenae'd", "You just got naenae'd by Zero Hack, $NAME"),
        CUSTOM("Custom", "");
    }

    private val timer = TickTimer(TimeUnit.SECONDS)
    private val attackedPlayers = WeakHashMap<EntityPlayer, Int>().synchronized() // <Player, Last Attack Time>
    private val confirmedKills = IntSets.synchronize(IntOpenHashSet())

    init {
        onDisable {
            reset()
        }

        // Clear the map on disconnect
        listener<ConnectionEvent.Disconnect> {
            reset()
        }

        safeListener<PacketEvent.Receive> { event ->
            if (event.packet !is SPacketChat || detectMode != DetectMode.BROADCAST || !player.isEntityAlive) return@safeListener

            val message = event.packet.textComponent.unformattedText
            if (!message.contains(player.name)) return@safeListener

            attackedPlayers.keys.find {
                message.contains(it.name)
            }?.let {
                confirmedKills.add(it.entityId)
            }
        }

        safeParallelListener<TickEvent.Post> {
            if (!player.isEntityAlive) {
                reset()
                return@safeParallelListener
            }

            updateAttackedPlayer()
            removeInvalidPlayers()
            sendEzMessage()
            sendHelpMessage()
        }
    }

    private fun SafeClientEvent.updateAttackedPlayer() {
        val attacked = player.lastAttackedEntity
        if (attacked is EntityPlayer && attacked.isEntityAlive && !attacked.isFakeOrSelf) {
            attackedPlayers[attacked] = player.lastAttackedEntityTime
        }
    }

    private fun SafeClientEvent.removeInvalidPlayers() {
        val removeTime = player.ticksExisted - 100L

        // Remove players if they are offline or we haven't attack them again in 100 ticks (5 seconds)
        attackedPlayers.entries.removeIf {
            @Suppress("SENSELESS_COMPARISON")
            it.value < removeTime || connection.getPlayerInfo(it.key.uniqueID) == null
        }
    }

    private fun SafeClientEvent.sendEzMessage() {
        // Check death and confirmation
        attackedPlayers.keys.find {
            !it.isEntityAlive
                && player.getDistanceSq(it) <= 256.0
                && (detectMode == DetectMode.HEALTH || confirmedKills.contains(it.entityId))
        }?.let {
            attackedPlayers.remove(it)
            confirmedKills.remove(it.entityId)

            val originalText = if (messageMode == MessageMode.CUSTOM) customText else messageMode.text
            val replaced = originalText.replace(NAME, it.name)

            if (notification) {
                Notification.send("${TextFormatting.RED}${it.name} ${TextFormatting.RESET}was killed by you", 5000L)
            }

            AutoEZ.sendServerMessage(replaced)
        }
    }

    private fun sendHelpMessage() {
        if (messageMode == MessageMode.CUSTOM && customText == UNCHANGED && timer.tickAndReset(5L)) { // 5 seconds delay
            MessageSendUtils.sendNoSpamChatMessage(
                "$chatName In order to use the custom $name, " +
                    "please change the CustomText setting in ClickGUI, " +
                    "with ${formatValue(NAME)} being the username of the killed player"
            )
        }
    }

    private fun reset() {
        attackedPlayers.clear()
        confirmedKills.clear()
    }
}
