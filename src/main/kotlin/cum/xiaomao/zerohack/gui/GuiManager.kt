package cum.xiaomao.zerohack.gui

import kotlinx.coroutines.Deferred
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.gui.clickgui.ZeroClickGui
import cum.xiaomao.zerohack.gui.hudgui.AbstractHudElement
import cum.xiaomao.zerohack.gui.hudgui.ZeroHudGui
import cum.xiaomao.zerohack.util.ClassUtils.instance
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.collections.AliasSet
import cum.xiaomao.zerohack.util.delegate.AsyncCachedValue
import cum.xiaomao.zerohack.AsyncLoader
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

internal object GuiManager : AsyncLoader<List<Class<out AbstractHudElement>>> {
    override var deferred: Deferred<List<Class<out AbstractHudElement>>>? = null
    private val hudElementSet = AliasSet<AbstractHudElement>()

    val hudElements by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        hudElementSet.distinct().sortedBy { it.nameAsString }
    }

    override suspend fun preLoad0(): List<Class<out AbstractHudElement>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = AbstractHudElement::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("cum.xiaomao.zerohack.gui.hudgui.elements") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        ZeroHackMod.logger.info("${list.size} hud elements found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out AbstractHudElement>>
    }

    override suspend fun load0(input: List<Class<out AbstractHudElement>>) {
        val time = measureTimeMillis {
            for (clazz in input) {
                register(clazz.instance)
            }
        }

        ZeroHackMod.logger.info("${input.size} hud elements loaded, took ${time}ms")

        ZeroClickGui.onGuiClosed()
        ZeroHudGui.onGuiClosed()

        ZeroClickGui.subscribe()
        ZeroHudGui.subscribe()
    }

    internal fun register(hudElement: AbstractHudElement) {
        hudElementSet.add(hudElement)
        ZeroHudGui.register(hudElement)
    }

    internal fun unregister(hudElement: AbstractHudElement) {
        hudElementSet.remove(hudElement)
        ZeroHudGui.unregister(hudElement)
    }

    fun getHudElementOrNull(name: String?) = name?.let { hudElementSet[it] }
}