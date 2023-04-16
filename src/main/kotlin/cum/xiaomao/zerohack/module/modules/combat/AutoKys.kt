package cum.xiaomao.zerohack.module.modules.combat

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendServerMessage
import cum.xiaomao.zerohack.util.threads.runSafe

internal object AutoKys : Module(
    name = "AutoKys",
    description = "Do /kill",
    category = Category.COMBAT
) {
    init {
        onEnable {
            runSafe {
                MessageSendUtils.sendServerMessage("/kill")
            }
            disable()
        }
    }
}
