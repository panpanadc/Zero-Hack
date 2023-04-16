package cum.xiaomao.zerohack.command.args

import cum.xiaomao.zerohack.util.interfaces.Nameable

/**
 * The ID for an argument
 */
@Suppress("UNUSED")
data class ArgIdentifier<T : Any>(override val name: CharSequence) : Nameable
