package cum.xiaomao.zerohack.command.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.command.ClientCommand
import cum.xiaomao.zerohack.translation.I18N_LOCAL_DIR
import cum.xiaomao.zerohack.translation.TranslationManager
import cum.xiaomao.zerohack.util.text.NoSpamMessage
import cum.xiaomao.zerohack.util.threads.defaultScope

object TranslationCommand : ClientCommand(
    name = "translation",
    alias = arrayOf("i18n")
) {
    init {
        literal("dump") {
            execute {
                defaultScope.launch(Dispatchers.Default) {
                    TranslationManager.dump()
                    NoSpamMessage.sendMessage(TranslationCommand, "Dumped root lang to $I18N_LOCAL_DIR")
                }
            }
        }

        literal("reload") {
            execute {
                defaultScope.launch(Dispatchers.IO) {
                    TranslationManager.reload()
                    NoSpamMessage.sendMessage(TranslationCommand, "Reloaded translations")
                }
            }
        }

        literal("update") {
            string("language") {
                execute {
                    defaultScope.launch(Dispatchers.IO) {
                        TranslationManager.update()
                        NoSpamMessage.sendMessage(TranslationCommand, "Updated translation")
                    }
                }
            }
        }
    }
}