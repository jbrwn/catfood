package dog.catfood.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> List<T>.nullableSingle(): T? {
    return when (size) {
        0 -> null
        1 -> this[0]
        else -> throw IllegalArgumentException("List has more than one element.")
    }
}

fun <T, K, V> Flow<T>.groupBy(getKey: (T) -> K, getValue: (T) -> V?): Flow<Pair<K, List<V>>> = flow {
    val storage = mutableMapOf<K, MutableList<V>>()
    collect { t ->
        val list = storage.getOrPut(getKey(t)) { mutableListOf() }
        val value = getValue(t)
        if (value != null) {
            list += value
        }
    }
    storage.forEach { (k, ts) -> emit(k to ts) }
}