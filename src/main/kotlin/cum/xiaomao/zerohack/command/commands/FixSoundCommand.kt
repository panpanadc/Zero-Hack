package cum.xiaomao.zerohack.command.commands

import cum.xiaomao.zerohack.command.ClientCommand

object FixSoundCommand : ClientCommand(
    name = "fixsound",
    description = "Fix sound device switching"
) {
    init {
        executeSafe {
            mc.soundHandler.onResourceManagerReload(mc.resourceManager)
        }
    }
}