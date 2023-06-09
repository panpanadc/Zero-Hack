package cum.xiaomao.zerohack.module.modules.player

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.module.Category
import cum.xiaomao.zerohack.module.Module
import cum.xiaomao.zerohack.util.MovementUtils.realSpeed
import cum.xiaomao.zerohack.util.TickTimer
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.threads.defaultScope
import java.io.File
import java.io.FileWriter
import java.time.LocalTime
import java.time.format.DateTimeFormatter

internal object MotionLogger : Module(
    name = "MotionLogger",
    description = "Logs player motion to a file",
    category = Category.PLAYER
) {
    private val fileTimeFormatter = DateTimeFormatter.ofPattern("HH-mm-ss_SSS")
    private val timer = TickTimer(TimeUnit.SECONDS)

    private const val directory = "${ZeroHackMod.DIRECTORY}/motionLogs"

    private var tick = 0
    private var filename = ""
    private var lines = ArrayList<String>()

    init {
        onEnable {
            filename = "${fileTimeFormatter.format(LocalTime.now())}.csv"

            synchronized(this) {
                lines.add("Tick,Pos X,Pos Y,Pos Z,Motion X,Motion Y,Motion Z,Speed\n")
                tick = 0
            }
        }

        onDisable {
            write()
        }

        safeParallelListener<TickEvent.Post> {
            val motionX = player.posX - player.lastTickPosX
            val motionY = player.posY - player.lastTickPosY
            val motionZ = player.posZ - player.lastTickPosZ
            val speed = player.realSpeed

            synchronized(MotionLogger) {
                lines.add("${tick++},${player.posX},${player.posY},${player.posZ},$motionX,$motionY,$motionZ,$speed\n")
            }

            if (lines.size >= 500 || timer.tickAndReset(15L)) {
                write()
            }
        }

        safeListener<ConnectionEvent.Disconnect> {
            disable()
        }
    }

    private fun write() {
        val lines = synchronized(this) {
            val cache = lines
            lines = ArrayList()
            cache
        }

        defaultScope.launch(Dispatchers.IO) {
            try {
                with(File(directory)) {
                    if (!exists()) mkdir()
                }

                FileWriter("$directory/${filename}", true).buffered().use {
                    for (line in lines) it.write(line)
                }
            } catch (e: Exception) {
                ZeroHackMod.logger.warn("$chatName Failed saving motion log!", e)
            }
        }
    }
}
