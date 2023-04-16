package cum.xiaomao.zerohack.util.pause

import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.util.extension.firstEntryOrNull
import cum.xiaomao.zerohack.util.extension.synchronized
import java.util.*

abstract class PriorityTimeoutPause : ITimeoutPause {
    private val pauseMap = TreeMap<AbstractModule, Long>(Comparator.reverseOrder()).synchronized()

    override fun requestPause(module: AbstractModule, timeout: Long): Boolean {
        val flag = isOnTopPriority(module)

        if (flag) {
            pauseMap[module] = System.currentTimeMillis() + timeout
        }

        return flag
    }

    fun isOnTopPriority(module: AbstractModule): Boolean {
        val currentTime = System.currentTimeMillis()
        var entry = pauseMap.firstEntryOrNull()

        while (entry != null && entry.key != module && (!entry.key.isActive() || entry.value < currentTime)) {
            pauseMap.pollFirstEntry()
            entry = pauseMap.firstEntry()
        }

        return entry == null
            || entry.key == module
            || entry.key.modulePriority < module.modulePriority
    }

    fun getTopPriority(): AbstractModule? {
        val currentTime = System.currentTimeMillis()
        var entry = pauseMap.firstEntryOrNull()

        while (entry != null && (!entry.key.isActive() || entry.value < currentTime)) {
            pauseMap.pollFirstEntry()
            entry = pauseMap.firstEntry()
        }

        return entry?.key
    }
}