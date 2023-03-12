package cut.the.crap.qreverywhere.utils.data

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class EncryptedPrefs @Inject constructor(@ApplicationContext context: Context, fileName: String) {

    private val masterKeyAlias by lazy {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M ) ""
            else MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    }

    private val sharedPreferences by lazy {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M )
            context.getSharedPreferences(fileName,Context.MODE_PRIVATE)
        else EncryptedSharedPreferences.create(
            fileName,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
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
    val editor = this.edit()
    when (T::class) {
        Boolean::class -> editor.putBoolean(key, value as Boolean)
        Float::class -> editor.putFloat(key, value as Float)
        Int::class -> editor.putInt(key, value as Int)
        Long::class -> editor.putLong(key, value as Long)
        String::class -> editor.putString(key, value as String)
        else -> {
            if (value is Set<*>) {
                editor.putStringSet(key, value as Set<String>)
            }
        }
    }
    editor.apply()
}