// data/preferences/ConfigHelper.kt
package com.example.foodorderingapp_ver2.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.GeneralSecurityException
import java.io.IOException

class ConfigHelper private constructor(context: Context) {

    companion object : SingletonHolder<ConfigHelper, Context>(::ConfigHelper) {
        private const val ENCRYPTED_PREFS_NAME = "food_app_encrypted_settings"

        // All preferences keys (now all encrypted)
        private const val KEY_CONNECTION_INITIALIZED = "connection_initialized"
        private const val KEY_CONFIG_UPDATED = "config_updated"
        private const val KEY_CONNECTION_ESTABLISHED_ONCE = "connection_established_once"
        private const val KEY_SERVER_URL = "server_url"
        private const val KEY_APP_PASSWORD = "app_password"
        private const val KEY_TOKEN = "token"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_CREDENTIALS = "user_credentials"
    }

    // Single encrypted SharedPreferences instance
    private val encryptedSharedPreferences: SharedPreferences by lazy {
        createEncryptedSharedPreferences(context)
    }

    private fun createEncryptedSharedPreferences(context: Context): SharedPreferences {
        return try {
            // Create or retrieve the master key for encryption
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            // Create encrypted shared preferences
            EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: GeneralSecurityException) {
            throw RuntimeException("Failed to create encrypted preferences", e)
        } catch (e: IOException) {
            throw RuntimeException("Failed to create encrypted preferences", e)
        }
    }

    // ========== SERVER CONFIGURATION ==========

    fun getServerUrl(): String? {
        return encryptedSharedPreferences.getString(KEY_SERVER_URL, "")
    }

    fun setServerUrl(value: String?) {
        encryptedSharedPreferences.edit().putString(KEY_SERVER_URL, value).apply()
    }

    // ========== CONNECTION STATE ==========

    fun isConnectionInitialized(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_CONNECTION_INITIALIZED, false)
    }

    fun setConnectionInitialized(value: Boolean) {
        encryptedSharedPreferences.edit().putBoolean(KEY_CONNECTION_INITIALIZED, value).apply()
    }

    fun hasEstablishedConnectionOnce(): Boolean {
        return encryptedSharedPreferences.getBoolean(KEY_CONNECTION_ESTABLISHED_ONCE, false)
    }

    fun setConnectionEstablishedOnce(value: Boolean) {
        encryptedSharedPreferences.edit().putBoolean(KEY_CONNECTION_ESTABLISHED_ONCE, value).apply()
    }

    fun getConfigUpdatedTimestamp(): Long {
        return encryptedSharedPreferences.getLong(KEY_CONFIG_UPDATED, 0L)
    }

    fun setConfigUpdatedTimestamp(value: Long) {
        encryptedSharedPreferences.edit().putLong(KEY_CONFIG_UPDATED, value).apply()
    }

    // ========== AUTHENTICATION DATA ==========

    fun getAppPassword(): String? {
        return encryptedSharedPreferences.getString(KEY_APP_PASSWORD, "")
    }

    fun setAppPassword(value: String?) {
        encryptedSharedPreferences.edit().putString(KEY_APP_PASSWORD, value).apply()
    }

    fun getToken(): String? {
        return encryptedSharedPreferences.getString(KEY_TOKEN, "")
    }

    fun setToken(value: String?) {
        encryptedSharedPreferences.edit().putString(KEY_TOKEN, value).apply()
    }

    fun getApiKey(): String? {
        return encryptedSharedPreferences.getString(KEY_API_KEY, "")
    }

    fun setApiKey(value: String?) {
        encryptedSharedPreferences.edit().putString(KEY_API_KEY, value).apply()
    }

    fun getUserCredentials(): String? {
        return encryptedSharedPreferences.getString(KEY_USER_CREDENTIALS, "")
    }

    fun setUserCredentials(value: String?) {
        encryptedSharedPreferences.edit().putString(KEY_USER_CREDENTIALS, value).apply()
    }

    // ========== UTILITY METHODS ==========

    fun isConnectionReady(): Boolean {
        val serverUrl = getServerUrl()
        val connectionInitialized = isConnectionInitialized()
        val appPassword = getAppPassword()

        return !serverUrl.isNullOrEmpty() && connectionInitialized && !appPassword.isNullOrEmpty()
    }

    fun saveCompleteConfiguration(serverUrl: String, password: String) {
        // Save all data to encrypted preferences
        setServerUrl(serverUrl)
        setAppPassword(password)
        setConnectionInitialized(true)
        setConnectionEstablishedOnce(true)
        setConfigUpdatedTimestamp(System.currentTimeMillis())
    }

    fun clearAllData() {
        // Clear all encrypted preferences
        encryptedSharedPreferences.edit().clear().apply()
    }

    fun clearSensitiveData() {
        // Clear only authentication-related data
        encryptedSharedPreferences.edit().apply {
            remove(KEY_APP_PASSWORD)
            remove(KEY_TOKEN)
            remove(KEY_API_KEY)
            remove(KEY_USER_CREDENTIALS)
        }.apply()
    }

    // Migration method to move existing unencrypted data to encrypted storage
    fun migrateToEncryptedStorage(context: Context) {
        try {
            // Check if old unencrypted preferences exist
            val oldPrefs = context.getSharedPreferences("food_app_settings", Context.MODE_PRIVATE)

            if (oldPrefs.all.isNotEmpty()) {
                // Migrate data from old preferences to encrypted storage
                val editor = encryptedSharedPreferences.edit()

                // Migrate all existing data
                oldPrefs.all.forEach { (key, value) ->
                    when (value) {
                        is String -> editor.putString(key, value)
                        is Boolean -> editor.putBoolean(key, value)
                        is Long -> editor.putLong(key, value)
                        is Int -> editor.putInt(key, value)
                        is Float -> editor.putFloat(key, value)
                    }
                }

                // Apply changes
                editor.apply()

                // Clear old unencrypted preferences
                oldPrefs.edit().clear().apply()
            }
        } catch (e: Exception) {
            // Handle migration errors gracefully
            e.printStackTrace()
        }
    }

    // Check if encrypted preferences are working
    fun testEncryption(): Boolean {
        return try {
            val testKey = "test_encryption_key"
            val testValue = "test_value_${System.currentTimeMillis()}"

            // Try to write and read
            encryptedSharedPreferences.edit().putString(testKey, testValue).apply()
            val retrievedValue = encryptedSharedPreferences.getString(testKey, null)

            // Clean up test data
            encryptedSharedPreferences.edit().remove(testKey).apply()

            retrievedValue == testValue
        } catch (e: Exception) {
            false
        }
    }
}

// Singleton holder pattern for thread-safe singleton with parameters
open class SingletonHolder<out T: Any, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}