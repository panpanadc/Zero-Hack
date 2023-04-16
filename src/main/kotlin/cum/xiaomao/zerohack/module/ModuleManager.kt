package cum.xiaomao.zerohack.module

import kotlinx.coroutines.Deferred
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.util.ClassUtils.instance
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.collections.AliasSet
import cum.xiaomao.zerohack.util.delegate.AsyncCachedValue
import cum.xiaomao.zerohack.util.extension.rootName
import cum.xiaomao.zerohack.util.interfaces.Helper
import cum.xiaomao.zerohack.AsyncLoader
import java.lang.reflect.Modifier
import kotlin.system.measureTimeMillis

object ModuleManager : AsyncLoader<List<Class<out AbstractModule>>>, Helper {
    override var deferred: Deferred<List<Class<out AbstractModule>>>? = null

    private val moduleSet = AliasSet<AbstractModule>()
    private val modulesDelegate = AsyncCachedValue(5L, TimeUnit.SECONDS) {
        moduleSet.distinct().sortedBy { it.rootName }
    }
    val modules by modulesDelegate

    override suspend fun preLoad0(): List<Class<out AbstractModule>> {
        val classes = AsyncLoader.classes.await()
        val list: List<Class<*>>

        val time = measureTimeMillis {
            val clazz = AbstractModule::class.java

            list = classes.asSequence()
                .filter { Modifier.isFinal(it.modifiers) }
                .filter { it.name.startsWith("cum.xiaomao.zerohack.module.modules") }
                .filter { clazz.isAssignableFrom(it) }
                .sortedBy { it.simpleName }
                .toList()
        }

        ZeroHackMod.logger.info("${list.size} modules found, took ${time}ms")

        @Suppress("UNCHECKED_CAST")
        return list as List<Class<out AbstractModule>>
    }

    override suspend fun load0(input: List<Class<out AbstractModule>>) {
        val time = measureTimeMillis {
            for (clazz in input) {
                register(clazz.instance)
            }
        }

        ZeroHackMod.logger.info("${input.size} modules loaded, took ${time}ms")
    }

    internal fun register(module: AbstractModule) {
        moduleSet.add(module)
        if (module.enabledByDefault || module.alwaysEnabled) module.enable()
        if (module.alwaysListening) module.subscribe()

        modulesDelegate.update()
    }

    internal fun unregister(module: AbstractModule) {
        moduleSet.remove(module)
        module.unsubscribe()

        modulesDelegate.update()
    }

    fun getModuleOrNull(moduleName: String?) = moduleName?.let { moduleSet[it] }
}