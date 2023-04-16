package cum.xiaomao.zerohack

import cum.xiaomao.zerohack.command.CommandManager
import cum.xiaomao.zerohack.gui.GuiManager
import cum.xiaomao.zerohack.manager.ManagerLoader
import cum.xiaomao.zerohack.module.ModuleManager
import cum.xiaomao.zerohack.util.ClassUtils
import cum.xiaomao.zerohack.util.threads.mainScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlin.system.measureTimeMillis

internal object LoaderWrapper {
    private val loaderList = ArrayList<AsyncLoader<*>>()

    init {
        loaderList.add(ModuleManager)
        loaderList.add(CommandManager)
        loaderList.add(ManagerLoader)
        loaderList.add(GuiManager)
    }

    @JvmStatic
    fun preLoadAll() {
        loaderList.forEach { it.preLoad() }
    }

    @JvmStatic
    fun loadAll() {
        runBlocking {
            loaderList.forEach { it.load() }
        }
    }
}

internal interface AsyncLoader<T> {
    var deferred: Deferred<T>?

    fun preLoad() {
        deferred = preLoadAsync()
    }

    private fun preLoadAsync(): Deferred<T> {
        return mainScope.async { preLoad0() }
    }

    suspend fun load() {
        println("Loading mixins by 猫猫")
        load0((deferred ?: preLoadAsync()).await())
    }

    suspend fun preLoad0(): T
    suspend fun load0(input: T)

    companion object {
        val classes = mainScope.async {
            val list: List<Class<*>>
            val time = measureTimeMillis {
                list = ClassUtils.findClasses("cum.xiaomao.zerohack") {
                    !it.contains("mixins")
                }
            }

            ZeroHackMod.logger.info("${list.size} classes found, took ${time}ms")
            list
        }
    }
}