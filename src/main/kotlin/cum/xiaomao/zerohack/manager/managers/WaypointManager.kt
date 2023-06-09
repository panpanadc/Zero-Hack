package cum.xiaomao.zerohack.manager.managers

import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import cum.xiaomao.zerohack.ZeroHackMod
import cum.xiaomao.zerohack.event.events.WaypointUpdateEvent
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.util.ConfigUtils
import cum.xiaomao.zerohack.util.Wrapper
import cum.xiaomao.zerohack.util.math.CoordinateConverter
import cum.xiaomao.zerohack.util.math.CoordinateConverter.asString
import cum.xiaomao.zerohack.util.math.vector.toBlockPos
import net.minecraft.util.math.BlockPos
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentSkipListSet

object WaypointManager : Manager() {
    private val gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File("${ZeroHackMod.DIRECTORY}/waypoints.json")
    private val sdf = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

    val waypoints = ConcurrentSkipListSet<Waypoint>(compareBy { it.id })

    fun loadWaypoints(): Boolean {
        ConfigUtils.fixEmptyJson(file, true)

        val success = try {
            val cacheArray = gson.fromJson(file.readText(), Array<Waypoint>::class.java)

            waypoints.clear()
            waypoints.addAll(cacheArray)

            ZeroHackMod.logger.info("Waypoint loaded")
            true
        } catch (e: Exception) {
            ZeroHackMod.logger.warn("Failed loading waypoints", e)
            false
        }

        WaypointUpdateEvent(WaypointUpdateEvent.Type.CLEAR, null).post()
        return success
    }

    fun saveWaypoints(): Boolean {
        return try {
            FileWriter(file, false).buffered().use {
                gson.toJson(waypoints, it)
            }
            ZeroHackMod.logger.info("Waypoint saved")
            true
        } catch (e: Exception) {
            ZeroHackMod.logger.warn("Failed saving waypoint", e)
            false
        }
    }

    fun get(id: Int): Waypoint? {
        val waypoint = waypoints.firstOrNull { it.id == id }
        WaypointUpdateEvent(WaypointUpdateEvent.Type.GET, waypoint).post()
        return waypoint
    }

    fun get(pos: BlockPos, currentDimension: Boolean = false): Waypoint? {
        val waypoint = waypoints.firstOrNull { (if (currentDimension) it.currentPos() else it.pos) == pos }
        WaypointUpdateEvent(WaypointUpdateEvent.Type.GET, waypoint).post()
        return waypoint
    }

    fun add(locationName: String): Waypoint {
        val pos = Wrapper.player?.positionVector?.toBlockPos()
        return if (pos != null) {
            val waypoint = add(pos, locationName)
            WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint).post()
            waypoint
        } else {
            ZeroHackMod.logger.error("Error during waypoint adding")
            dateFormatter(BlockPos(0, 0, 0), locationName) // This shouldn't happen
        }
    }

    fun add(pos: BlockPos, locationName: String): Waypoint {
        val waypoint = dateFormatter(pos, locationName)
        waypoints.add(waypoint)
        WaypointUpdateEvent(WaypointUpdateEvent.Type.ADD, waypoint).post()
        return waypoint
    }

    fun remove(pos: BlockPos, currentDimension: Boolean = false): Boolean {
        val waypoint = get(pos, currentDimension)
        val removed = waypoints.remove(waypoint)
        WaypointUpdateEvent(WaypointUpdateEvent.Type.REMOVE, waypoint).post()
        return removed
    }

    fun remove(id: Int): Boolean {
        val waypoint = get(id) ?: return false
        val removed = waypoints.remove(waypoint)
        WaypointUpdateEvent(WaypointUpdateEvent.Type.REMOVE, waypoint).post()
        return removed
    }

    fun clear() {
        waypoints.clear()
        WaypointUpdateEvent(WaypointUpdateEvent.Type.CLEAR, null).post()
    }

    fun genServer(): String? {
        return Wrapper.minecraft.currentServerData?.serverIP
            ?: if (Wrapper.minecraft.isIntegratedServerRunning) "Singleplayer"
            else null
    }

    fun genDimension(): Int {
        return Wrapper.player?.dimension ?: -2 /* this shouldn't ever happen at all */
    }

    private fun dateFormatter(pos: BlockPos, locationName: String): Waypoint {
        val date = sdf.format(Date())
        return Waypoint(pos, locationName, date)
    }

    class Waypoint(
        @SerializedName("position")
        val pos: BlockPos,
        val name: String,

        @SerializedName(value = "date", alternate = ["time"])
        val date: String
    ) {
        val id: Int = genID()
        val server: String? = genServer() /* can be null from old configs */
        val dimension: Int = genDimension()

        fun currentPos() = CoordinateConverter.toCurrent(dimension, pos)

        private fun genID(): Int = waypoints.lastOrNull()?.id?.plus(1) ?: 0

        override fun toString() = currentPos().asString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Waypoint) return false

            if (pos != other.pos) return false
            if (name != other.name) return false
            if (date != other.date) return false
            if (id != other.id) return false
            if (server != other.server) return false
            if (dimension != other.dimension) return false

            return true
        }

        override fun hashCode(): Int {
            var result = pos.hashCode()
            result = 31 * result + name.hashCode()
            result = 31 * result + date.hashCode()
            result = 31 * result + id
            result = 31 * result + (server?.hashCode() ?: 0)
            result = 31 * result + dimension
            return result
        }
    }
}