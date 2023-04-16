package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.event.events.ShutdownEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.gui.clickgui.ZeroClickGui
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.threads.onMainThreadSafe
import org.lwjgl.input.Keyboard

internal object ClickGUI : Module(
    name = "ClickGUI",
    description = "Opens the Click GUI",
    category = Category.CLIENT,
    visible = false,
    alwaysListening = true
) {

    init {
        listener<ShutdownEvent> {
            disable()
        }

        onEnable {
            onMainThreadSafe {
                if (mc.currentScreen !is ZeroClickGui) {
                    HudEditor.disable()
                    mc.displayGuiScreen(ZeroClickGui)
                    ZeroClickGui.onDisplayed()
                }
            }
        }

        onDisable {
            onMainThreadSafe {
                if (mc.currentScreen is ZeroClickGui) {
                    mc.displayGuiScreen(null)
                }
            }
        }

        bind.value.setBind(Keyboard.KEY_Y)
    }
}
