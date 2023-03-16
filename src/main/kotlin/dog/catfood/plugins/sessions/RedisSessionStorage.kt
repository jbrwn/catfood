package dog.catfood.plugins.sessions

import io.ktor.server.sessions.SessionStorage
import kotlinx.coroutines.future.await
import org.redisson.api.RMapCacheAsync
import org.redisson.api.RedissonClient
import java.time.Duration
import java.util.concurrent.TimeUnit

class RedisSessionStorage(
    redissonClient: RedissonClient,
    ttl: Duration,
    keyPrefix: String = "ktor:session:"
): SessionStorage {
    private val map: RMapCacheAsync<String, String> = redissonClient.getMapCache(keyPrefix)
    private val ttlSeconds: Long = ttl.toSeconds()

    override suspend fun invalidate(id: String) {
        map.fastRemoveAsync(id)
            .toCompletableFuture()
            .await()
    }

    override suspend fun read(id: String): String {
        return map.getAsync(id)
            .toCompletableFuture()
            .thenApply { it ?: throw NoSuchElementException("Session $id not found") }
            .await()
    }

    override suspend fun write(id: String, value: String) {
        map.fastPutAsync(id, value, ttlSeconds, TimeUnit.SECONDS)
            .toCompletableFuture()
            .await()
    }
}
