package cum.xiaomao.zerohack.module.modules.player

import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.accessor.blockHitDelay
import cum.xiaomao.zerohack.util.threads.runSafe

internal object FastBreak : Module(
    name = "FastBreak",
    category = Category.PLAYER,
    description = "Breaks block faster and nullifies the break delay"
) {
    private val breakDelay by setting("Break Delay", 0, 0..5, 1)

    @JvmStatic
    fun updateBreakDelay() {
        runSafe {
            if (isEnabled) {
                playerController.blockHitDelay = breakDelay
            }
        }
    }
}