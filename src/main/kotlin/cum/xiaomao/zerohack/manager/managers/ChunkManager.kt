package cum.xiaomao.zerohack.manager.managers

import io.netty.util.internal.ConcurrentSet
import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.event.events.ConnectionEvent
import cum.xiaomao.zerohack.event.events.PacketEvent
import cum.xiaomao.zerohack.event.listener
import cum.xiaomao.zerohack.event.safeListener
import cum.xiaomao.zerohack.manager.Manager
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.delegate.AsyncCachedValue
import cum.xiaomao.zerohack.util.extension.fastFloor
import cum.xiaomao.zerohack.util.threads.defaultScope
import net.minecraft.network.play.server.SPacketChunkData
import net.minecraft.util.math.ChunkPos
import net.minecraft.world.chunk.Chunk

object ChunkManager : Manager() {
    private val newChunks0 = ConcurrentSet<ChunkPos>()
    val newChunks by AsyncCachedValue(1L, TimeUnit.SECONDS) {
        newChunks0.toList()
    }

    init {
        listener<ConnectionEvent.Disconnect> {
            newChunks0.clear()
        }

        safeListener<PacketEvent.PostReceive> { event ->
            defaultScope.launch {
                if (event.packet !is SPacketChunkData || event.packet.isFullChunk) return@launch
                val chunk = world.getChunk(event.packet.chunkX, event.packet.chunkZ)
                if (chunk.isEmpty) return@launch

                if (newChunks0.add(chunk.pos)) {
                    if (newChunks0.size > 8192) {
                        val playerX = player.posX.fastFloor()
                        val playerZ = player.posZ.fastFloor()

                        newChunks0.maxByOrNull {
                            (playerX - it.x) + (playerZ - it.z)
                        }?.let {
                            newChunks0.remove(it)
                        }
                    }
                }
            }
        }
    }

    fun isNewChunk(chunk: Chunk) = isNewChunk(chunk.pos)

    fun isNewChunk(chunkPos: ChunkPos) = newChunks0.contains(chunkPos)
}