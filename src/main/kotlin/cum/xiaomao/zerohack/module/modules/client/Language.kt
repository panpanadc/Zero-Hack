package cum.xiaomao.zerohack.module.modules.client

import cum.xiaomao.zerohack.event.events.GuiEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.setting.GenericConfig
import cum.xiaomao.zerohack.translation.TranslationManager
import cum.xiaomao.zerohack.util.Wrapper
import net.minecraft.client.gui.GuiMainMenu

internal object Language : AbstractModule(
    name = "Language",
    description = "Change language",
    category = Category.CLIENT,
    alwaysEnabled = true,
    visible = false,
    config = GenericConfig
) {
    private val overrideLanguage = setting("Override Language", false)
    private val language = setting("Language", "en_us", { overrideLanguage.value })

    init {
        listener<GuiEvent.Displayed>(114514) {
            if (it.screen is GuiMainMenu || it.screen is MainMenu.TrollGuiMainMenu) {
                TranslationManager.reload()
            }
        }
    }

    val settingLanguage: String
        get() = if (overrideLanguage.value) {
            language.value
        } else {
            Wrapper.minecraft.gameSettings.language
        }

    init {
        overrideLanguage.listeners.add {
            TranslationManager.reload()
        }

        language.listeners.add {
            TranslationManager.reload()
        }
    }
}