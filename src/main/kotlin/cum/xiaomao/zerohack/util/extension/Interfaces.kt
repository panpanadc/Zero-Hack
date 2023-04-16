package cum.xiaomao.zerohack.util.extension

import cum.xiaomao.zerohack.translation.TranslationKey
import cum.xiaomao.zerohack.util.interfaces.DisplayEnum
import cum.xiaomao.zerohack.util.interfaces.Nameable

val DisplayEnum.rootName: String
    get() = (displayName as? TranslationKey)?.rootString ?: displayName.toString()

val Nameable.rootName: String
    get() = (name as? TranslationKey)?.rootString ?: name.toString()