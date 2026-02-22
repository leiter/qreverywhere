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

    private val cache = mutableMapOf<String, ByteArray>()
    private val accessOrder = mutableListOf<String>()
    private val mutex = Mutex()

    override suspend fun generateQrCode(
        text: String,
        size: Int,
        foregroundColor: Int,
        backgroundColor: Int
    ): ByteArray {
        val cacheKey = buildCacheKey(text, size, foregroundColor, backgroundColor)

        mutex.withLock {
            cache[cacheKey]?.let { cachedData ->
                accessOrder.remove(cacheKey)
                accessOrder.add(cacheKey)
                Logger.d("CachingQrCodeGenerator") { "Cache hit for QR code" }
                return cachedData
            }
        }

        Logger.d("CachingQrCodeGenerator") { "Cache miss, generating QR code" }
        val qrCodeData = delegate.generateQrCode(text, size, foregroundColor, backgroundColor)

        mutex.withLock {
            while (cache.size >= maxCacheSize && accessOrder.isNotEmpty()) {
                val oldest = accessOrder.removeAt(0)
                cache.remove(oldest)
            }
            cache[cacheKey] = qrCodeData
            accessOrder.add(cacheKey)
        }

        return qrCodeData
    }

    suspend fun clearCache() {
        mutex.withLock {
            cache.clear()
            accessOrder.clear()
        }
        Logger.d("CachingQrCodeGenerator") { "Cache cleared" }
    }

    suspend fun cacheSize(): Int {
        mutex.withLock {
            return cache.size
        }
    }

    private fun buildCacheKey(text: String, size: Int, foregroundColor: Int, backgroundColor: Int): String {
        return "${text.hashCode()}_${size}_${foregroundColor}_${backgroundColor}"
    }
}
