package cum.xiaomao.zerohack.gui.hudgui

import cum.xiaomao.zerohack.event.IListenerOwner
import cum.xiaomao.zerohack.event.ListenerOwner
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.gui.rgui.windows.BasicWindow
import cum.xiaomao.zerohack.module.modules.client.GuiSetting
import cum.xiaomao.zerohack.module.modules.client.Hud
import cum.xiaomao.zerohack.setting.GuiConfig
import cum.xiaomao.zerohack.setting.GuiConfig.setting
import cum.xiaomao.zerohack.setting.configs.AbstractConfig
import cum.xiaomao.zerohack.util.Bind
import cum.xiaomao.zerohack.util.extension.rootName
import cum.xiaomao.zerohack.util.graphics.RenderUtils2D
import cum.xiaomao.zerohack.util.graphics.font.renderer.MainFontRenderer
import cum.xiaomao.zerohack.util.interfaces.Alias
import cum.xiaomao.zerohack.util.interfaces.DisplayEnum
import cum.xiaomao.zerohack.util.interfaces.Nameable
import cum.xiaomao.zerohack.util.math.vector.Vec2f
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import org.lwjgl.opengl.GL11.glScalef

abstract class AbstractHudElement(
    name: String,
    final override val alias: Array<out CharSequence>,
    val category: Category,
    val description: String,
    val alwaysListening: Boolean,
    enabledByDefault: Boolean,
    config: AbstractConfig<out Nameable>
) : BasicWindow(name, 20.0f, 20.0f, 100.0f, 50.0f, SettingGroup.HUD_GUI, config), Alias, IListenerOwner by ListenerOwner() {

    val bind by setting("Bind", Bind())
    val scale by setting("Scale", 1.0f, 0.1f..4.0f, 0.05f)
    val default = setting("Default", false)

    override val resizable = false

    final override val minWidth: Float get() = MainFontRenderer.getHeight() * scale * 2.0f
    final override val minHeight: Float get() = MainFontRenderer.getHeight() * scale

    final override val maxWidth: Float get() = hudWidth * scale
    final override val maxHeight: Float get() = hudHeight * scale

    open val hudWidth: Float get() = 20f
    open val hudHeight: Float get() = 10f

    val settingList get() = GuiConfig.getGroupOrPut(SettingGroup.HUD_GUI.groupName).getGroupOrPut(rootName).getSettings()

    init {
        safeParallelListener<TickEvent.Pre> {
            if (!visible) return@safeParallelListener
            width = maxWidth
            height = maxHeight
        }
    }

    override fun onGuiInit() {
        super.onGuiInit()
        if (alwaysListening || visible) subscribe()
    }

    override fun onClosed() {
        super.onClosed()
        if (alwaysListening || visible) subscribe()
    }

    final override fun onTick() {
        super.onTick()
    }

    final override fun onRender(absolutePos: Vec2f) {
        renderFrame()
        glScalef(scale, scale, scale)
        renderHud()
    }

    open fun renderHud() {}

    open fun renderFrame() {
        RenderUtils2D.drawRectFilled(renderWidth, renderHeight, GuiSetting.backGround)
        RenderUtils2D.drawRectOutline(renderWidth, renderHeight, 1.0f, GuiSetting.outline)
    }

    init {
        visibleSetting.valueListeners.add { _, it ->
            if (it) {
                subscribe()
                lastActiveTime = System.currentTimeMillis()
            } else if (!alwaysListening) {
                unsubscribe()
            }
        }

        default.valueListeners.add { _, it ->
            if (it) {
                settingList.filter { it != visibleSetting && it != default }.forEach { it.resetValue() }
                default.value = false
                MessageSendUtils.sendNoSpamChatMessage("$name Set to defaults!")
            }
        }

        if (!enabledByDefault) visible = false
    }

    enum class Category(override val displayName: CharSequence) : DisplayEnum {
        CLIENT("Client"),
        COMBAT("Combat"),
        PLAYER("Player"),
        WORLD("World"),
        MISC("Misc")
    }

    protected companion object {
        val primaryColor get() = Hud.primaryColor
        val secondaryColor get() = Hud.secondaryColor
    }

}