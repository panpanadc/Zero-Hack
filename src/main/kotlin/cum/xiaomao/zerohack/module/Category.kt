package cum.xiaomao.zerohack.module

import cum.xiaomao.zerohack.translation.TranslateType
import cum.xiaomao.zerohack.util.interfaces.DisplayEnum

enum class Category(override val displayName: CharSequence) : DisplayEnum {
    CHAT(TranslateType.COMMON commonKey "Chat"),
    CLIENT(TranslateType.COMMON commonKey "Client"),
    COMBAT(TranslateType.COMMON commonKey "Combat"),
    MISC(TranslateType.COMMON commonKey "Misc"),
    MOVEMENT(TranslateType.COMMON commonKey "Movement"),
    PLAYER(TranslateType.COMMON commonKey "Player"),
    WORLD(TranslateType.COMMON commonKey "World"),
    RENDER(TranslateType.COMMON commonKey "Render");

    override fun toString() = displayString
}