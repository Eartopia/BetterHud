package kr.toxicity.hud.util

fun <T> List<T>.split(splitSize: Int): List<List<T>> {
    val result = ArrayList<List<T>>()
    var index = 0
    while (index < size) {
        val subList = subList(index, (index + splitSize).coerceAtMost(size))
        if (subList.isNotEmpty()) result += subList
        index += splitSize
    }
    return result
}

fun <T> Collection<T>.forEachAsync(block: (T) -> Unit) {
    toList().forEachAsync(block)
}

fun <T> MutableCollection<T>.removeIfSync(block: (T) -> Boolean) {
    synchronized(this) {
        val iterator = iterator()
        while (iterator.hasNext()) {
            if (block(iterator.next())) iterator.remove()
        }
    }
}


fun <T> List<T>.forEachAsync(block: (T) -> Unit) {
    forEachAsync({ it }, block)
}
fun <T> List<T>.forEachAsync(multiplier: (Int) -> Int, block: (T) -> Unit) {
    forEach(block)
}
