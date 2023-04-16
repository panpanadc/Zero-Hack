package cum.xiaomao.zerohack.util.extension

import cum.xiaomao.zerohack.util.interfaces.DisplayEnum

fun <E : Enum<E>> E.next(): E = declaringJavaClass.enumConstants.run {
    get((ordinal + 1) % size)
}

fun Enum<*>.readableName() = (this as? DisplayEnum)?.displayName
    ?: name.mapEach('_') { it.lowercase().capitalize() }.joinToString(" ")