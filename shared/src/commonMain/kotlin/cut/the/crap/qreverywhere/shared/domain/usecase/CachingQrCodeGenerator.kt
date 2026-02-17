package cut.the.crap.qreverywhere.shared.domain.usecase

import cut.the.crap.qreverywhere.shared.utils.Logger
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Caching decorator for QrCodeGenerator
 * Caches generated QR codes in memory using a simple bounded cache
 *
 * @param delegate The underlying QrCodeGenerator to delegate to
 * @param maxCacheSize Maximum number of entries to keep in cache
 */
class CachingQrCodeGenerator(
    private val delegate: QrCodeGenerator,
    private val maxCacheSize: Int = 50
) : QrCodeGenerator {

    // Simple bounded cache using MutableMap (KMP compatible)
    private val cache = mutableMapOf<String, ByteArray>()
    private val accessOrder = mutableListOf<String>()
    private val mutex = Mutex()

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray {
        // Create cache key from all parameters
        val cacheKey = buildCacheKey(text, size, foregroundColor, backgroundColor)

        // Check cache first
        mutex.withLock {
            cache[cacheKey]?.let { cachedData ->
                // Move to end for LRU tracking
                accessOrder.remove(cacheKey)
                accessOrder.add(cacheKey)
                Logger.d("CachingQrCodeGenerator") { "Cache hit for QR code" }
                return cachedData
            }
        }

        // Generate QR code if not in cache
        Logger.d("CachingQrCodeGenerator") { "Cache miss, generating QR code" }
        val qrCodeData = delegate.generateQrCode(text, size, foregroundColor, backgroundColor)

        // Store in cache with LRU eviction
        mutex.withLock {
            // Evict oldest if at capacity
            while (cache.size >= maxCacheSize && accessOrder.isNotEmpty()) {
                val oldest = accessOrder.removeAt(0)
                cache.remove(oldest)
            }
            cache[cacheKey] = qrCodeData
            accessOrder.add(cacheKey)
        }

        return qrCodeData
    }

    /**
     * Clear the cache
     */
    suspend fun clearCache() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
        Logger.d("CachingQrCodeGenerator") { "Cache cleared" }
    }

    /**
     * Get the current cache size
     */
    suspend fun cacheSize(): Int {
        mutex.withLock {
            return cache.size
        }
    }

    private fun buildCacheKey(text: String, size: Int, foregroundColor: Int, backgroundColor: Int): String {
        // Simple hash-based key
        return "${text.hashCode()}_${size}_${foregroundColor}_${backgroundColor}"
    }
}
