package cut.the.crap.qreverywhere.utils.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import timber.log.Timber
import java.io.File
import androidx.core.content.edit

class EncryptedPrefs(context: Context, fileName: String) {

    private val masterKeyAlias by lazy {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) ""
            else MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    private val sharedPreferences by lazy {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) {
            context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
        } else {
            createEncryptedSharedPreferences(context, fileName)
        }
    }

    private fun createEncryptedSharedPreferences(context: Context, fileName: String): SharedPreferences {
        return try {
            EncryptedSharedPreferences.create(
                fileName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create EncryptedSharedPreferences, attempting recovery...")

            // Delete corrupted preferences file and try again
            try {
                val prefsFile = File(context.applicationInfo.dataDir + "/shared_prefs/$fileName.xml")
                if (prefsFile.exists()) {
                    prefsFile.delete()
                    Timber.d("Deleted corrupted preferences file: $fileName.xml")
                }

                // Try creating again after deletion
                EncryptedSharedPreferences.create(
                    fileName,
                    masterKeyAlias,
                    context,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
            } catch (retryException: Exception) {
                Timber.e(retryException, "Recovery failed, falling back to regular SharedPreferences")
                // Fall back to regular SharedPreferences if encryption continues to fail
                context.getSharedPreferences(fileName, Context.MODE_PRIVATE)
            }
        }
    }

    var backgroundColor = sharedPreferences.getInt(BACKGROUND, -0x1)
    set(value) {
        field = value
        sharedPreferences.put(BACKGROUND, value)
    }

    var foregroundColor = sharedPreferences.getInt(FOREGROUND, -0x1000000)
    set(value){
        field = value
        sharedPreferences.put(FOREGROUND, value)
    }

    companion object {
        const val BACKGROUND = "background"
        const val FOREGROUND = "foreground"
    }

}

inline fun <reified T> SharedPreferences.get(key: String, defaultValue: T): T {
    when (T::class) {
        Boolean::class -> return this.getBoolean(key, defaultValue as Boolean) as T
        Float::class -> return this.getFloat(key, defaultValue as Float) as T
        Int::class -> return this.getInt(key, defaultValue as Int) as T
        Long::class -> return this.getLong(key, defaultValue as Long) as T
        String::class -> return this.getString(key, defaultValue as String) as T
        else -> {
            if (defaultValue is Set<*>) {
                return this.getStringSet(key, defaultValue as Set<String>) as T
            }
        }
    }
    return defaultValue
}

inline fun <reified T> SharedPreferences.put(key: String, value: T) {
    this.edit {
        when (T::class) {
            Boolean::class -> putBoolean(key, value as Boolean)
            Float::class -> putFloat(key, value as Float)
            Int::class -> putInt(key, value as Int)
            Long::class -> putLong(key, value as Long)
            String::class -> putString(key, value as String)
            else -> {
                if (value is Set<*>) {
                    putStringSet(key, value as Set<String>)
                }
            }
        }
    }
}