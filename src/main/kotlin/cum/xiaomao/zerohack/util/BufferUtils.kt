package cum.xiaomao.zerohack.util

import java.nio.Buffer

fun Buffer.skip(count: Int) {
    this.position(position() + count)
}