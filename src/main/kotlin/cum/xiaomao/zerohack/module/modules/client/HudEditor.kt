package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.event.events.ShutdownEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.gui.hudgui.ZeroHudGui
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import org.lwjgl.input.Keyboard

internal object HudEditor : Module(
    name = "HudEditor",
    description = "Edits the Hud",
    category = Category.CLIENT,
    visible = false
) {
    init {
        onEnable {
            if (mc.currentScreen !is ZeroHudGui) {
                ClickGUI.disable()
                mc.displayGuiScreen(ZeroHudGui)
                ZeroHudGui.onDisplayed()
            }
        }

        onDisable {
            if (mc.currentScreen is ZeroHudGui) {
                mc.displayGuiScreen(null)
            }
        }

        listener<ShutdownEvent> {
            disable()
            bind.value.setBind(Keyboard.KEY_I)
        }
    }
}
