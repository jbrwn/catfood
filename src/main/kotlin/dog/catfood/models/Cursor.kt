package dog.catfood.models

open class Cursor<T> (
    val id: T?,
    limit: Int?
) {
    private companion object {
        private const val DEFAULT_LIMIT = 20
        private const val MAX_LIMIT = 100
    }
    val limit = when {
        limit == null || limit <=0 -> DEFAULT_LIMIT
        limit > MAX_LIMIT -> MAX_LIMIT
        else -> limit
    }
}

class LimitCursor<T>(limit: Int): Cursor<T>(null, limit)