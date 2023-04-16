package cum.xiaomao.zerohack.event

import cum.xiaomao.zerohack.command.CommandManager
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.events.baritone.BaritoneCommandEvent
import cum.xiaomao.zerohack.event.events.player.InteractEvent
import cum.xiaomao.zerohack.event.events.player.PlayerPushOutOfBlockEvent
import cum.xiaomao.zerohack.event.events.render.FogColorEvent
import cum.xiaomao.zerohack.event.events.render.Render3DEvent
import cum.xiaomao.zerohack.event.events.render.RenderOverlayEvent
import cum.xiaomao.zerohack.event.events.render.ResolutionUpdateEvent
import cum.xiaomao.zerohack.gui.mc.ZeroGuiChat
import cum.xiaomao.zerohack.manager.managers.WorldManager
import cum.xiaomao.zerohack.util.Wrapper
import cum.xiaomao.zerohack.util.graphics.GlStateUtils
import cum.xiaomao.zerohack.util.graphics.ProjectionUtils
import cum.xiaomao.zerohack.util.graphics.RenderUtils3D
import cum.xiaomao.zerohack.util.text.MessageDetection
import net.minecraftforge.client.event.*
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.event.world.WorldEvent
import net.minecraftforge.fml.common.eventhandler.EventPriority
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.InputEvent
import net.minecraftforge.fml.common.network.FMLNetworkEvent
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.util.*

@Suppress("UNUSED_PARAMETER")
internal object ForgeEventProcessor : ListenerOwner() {
    private val mc = Wrapper.minecraft
    private var prevWidth = -1
    private var prevHeight = -1

    init {
        listener<TickEvent.Post>(true) {
            if (prevWidth != mc.displayWidth || prevHeight != mc.displayHeight) {
                prevWidth = mc.displayWidth
                prevHeight = mc.displayHeight
                ResolutionUpdateEvent(mc.displayWidth, mc.displayHeight).post()
                GlStateUtils.useProgramForce(0)
            }
        }
    }

    @SubscribeEvent
    fun onWorldRender(event: RenderWorldLastEvent) {
        ProjectionUtils.updateMatrix()
        RenderUtils3D.prepareGL()
        Render3DEvent.post()
        RenderUtils3D.releaseGL()
        GlStateUtils.useProgramForce(0)
    }

    @SubscribeEvent
    fun onRenderGameOverlayPre(event: RenderGameOverlayEvent.Pre) {
        GlStateUtils.alpha(false)
        RenderOverlayEvent.Pre(event).post()
        GlStateUtils.alpha(true)
    }

    @SubscribeEvent
    fun onRenderGameOverlayPost(event: RenderGameOverlayEvent.Post) {
        GlStateUtils.alpha(false)
        RenderOverlayEvent.Post(event).post()
        GlStateUtils.alpha(true)
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    fun onKeyInput(event: InputEvent.KeyInputEvent) {
        val key = Keyboard.getEventKey()
        val state = Keyboard.getEventKeyState()
        cum.xiaomao.zerohack.event.events.InputEvent.Keyboard(key, state).post()

        if (!state) return

        if (!mc.gameSettings.keyBindSneak.isKeyDown) {
            val prefix = CommandManager.prefix
            val typedChar = Keyboard.getEventCharacter().toString()
            if (prefix.length == 1 && typedChar.equals(CommandManager.prefix, true)) {
                mc.displayGuiScreen(ZeroGuiChat(CommandManager.prefix))
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    fun onChatSent(event: ClientChatEvent) {
        MessageDetection.Command.BARITONE.removedOrNull(event.message)?.let {
            BaritoneCommandEvent(it.toString().substringBefore(' ').lowercase(Locale.ROOT)).post()
        }

        if (MessageDetection.Command.TROLL_HACK detect event.message) {
            CommandManager.runCommand(event.message.removePrefix(CommandManager.prefix))
            event.isCanceled = true
        }
    }

    @SubscribeEvent
    fun onEventMouse(event: InputEvent.MouseInputEvent) {
        cum.xiaomao.zerohack.event.events.InputEvent.Mouse(Mouse.getEventButton(), Mouse.getEventButtonState()).post()
    }

    @SubscribeEvent
    fun onRenderBlockOverlay(event: RenderBlockOverlayEvent) {
        cum.xiaomao.zerohack.event.events.render.RenderBlockOverlayEvent(event).post()
    }

    @SubscribeEvent
    fun onInputUpdate(event: InputUpdateEvent) {
        cum.xiaomao.zerohack.event.events.player.InputUpdateEvent(event).post()
    }

    @SubscribeEvent
    fun onClientDisconnect(event: FMLNetworkEvent.ClientDisconnectionFromServerEvent) {
        ConnectionEvent.Disconnect.post()
    }

    @SubscribeEvent
    fun onClientConnect(event: FMLNetworkEvent.ClientConnectedToServerEvent) {
        ConnectionEvent.Connect.post()
    }

    @SubscribeEvent
    fun onRenderFogColors(event: EntityViewRenderEvent.FogColors) {
        FogColorEvent(event).post()
    }

    @SubscribeEvent
    fun onRightClickItem(event: PlayerInteractEvent.RightClickItem) {
        InteractEvent.Item.RightClick(event.hand).post()
    }

    @SubscribeEvent
    fun onLoadWorld(event: WorldEvent.Load) {
        if (event.world.isRemote) {
            event.world.addEventListener(WorldManager)
            cum.xiaomao.zerohack.event.events.WorldEvent.Load.post()
        }
    }

    @SubscribeEvent
    fun onUnloadWorld(event: WorldEvent.Unload) {
        if (event.world.isRemote) {
            event.world.removeEventListener(WorldManager)
            cum.xiaomao.zerohack.event.events.WorldEvent.Unload.post()
        }
    }

    @SubscribeEvent
    fun onPlayerSPPushOutOfBlocks(event: PlayerSPPushOutOfBlocksEvent) {
        PlayerPushOutOfBlockEvent(event).post()
    }
}
