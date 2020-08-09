package com.kennyc.data_enphase.cache

import com.kennyc.solarviewer.data.Clock
import com.kennyc.solarviewer.data.Logger
import java.util.*
import java.util.concurrent.TimeUnit

private const val TAG = "TimedCache"

// Stats are updated every 15 minutes, do not request if we are in between the 15 minute window
class TimedCache(private val logger: Logger, private val clock: Clock) {
    private val map: LinkedHashMap<String, TimedWrapper> = linkedMapOf()

    fun get(key: String): Any? {
        return map[key]
            .takeIf { it != null }
            ?.run {
                val currentTime = clock.currentTime()

                if (currentTime < expirationTime) {
                    val remainingTime = expirationTime - currentTime
                    logger.i(
                        TAG,
                        "Item present and valid in cache, will expire in " +
                                "${TimeUnit.MILLISECONDS.toSeconds(remainingTime)} seconds}"
                    )

                    return any
                } else {
                    logger.i(TAG, "Item present but has expired, removing")
                    remove(key)
                }
            }
            ?: run {
                logger.i(TAG, "Item not present")
                null
            }
    }

    fun put(key: String, any: Any) {
        val cal = Calendar.getInstance(Locale.getDefault()).apply { time = clock.currentDate() }
        val minutes = cal.get(Calendar.MINUTE)

        val remaining = when {
            minutes < 15 -> 15 - minutes
            minutes < 30 -> 30 - minutes
            minutes < 45 -> 45 - minutes
            else -> 60 - minutes
        }

        val expiration = TimeUnit.MINUTES.toMillis(remaining.toLong()) + clock.currentTime()
        val wrapped = TimedWrapper(expiration, any)
        map[key] = wrapped
        logger.i(TAG, "Inserting ${any.javaClass.simpleName} with expiration time: $expiration")
    }

    fun remove(key: String): Boolean = map.remove(key) != null
}

private data class TimedWrapper(val expirationTime: Long, val any: Any)