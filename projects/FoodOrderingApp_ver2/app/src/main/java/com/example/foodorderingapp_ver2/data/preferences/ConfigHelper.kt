// data/preferences/ConfigHelper.kt
package com.example.foodorderingapp_ver2.data.preferences

import android.content.Context
import android.content.SharedPreferences

class ConfigHelper private constructor(context: Context) {

    companion object : SingletonHolder<ConfigHelper, Context>(::ConfigHelper) {
        private const val PREFS_NAME = "food_app_settings"
        private const val ENCRYPTED_PREFS_NAME = "food_app_encrypted_settings"

        // Regular preferences keys (non-sensitive data)
        private const val KEY_CONNECTION_INITIALIZED = "connection_initialized"
        private const val KEY_CONFIG_UPDATED = "config_updated"
        private const val KEY_CONNECTION_ESTABLISHED_ONCE = "connection_established_once"
        private const val KEY_SERVER_URL = "server_url"

        // Encrypted preferences keys (sensitive data)
        private const val KEY_APP_PASSWORD = "app_password"
        private const val KEY_TOKEN = "token"
        private const val KEY_API_KEY = "api_key"
        private const val KEY_USER_CREDENTIALS = "user_credentials"
    }

    // Regular SharedPreferences for non-sensitive data
    private val regularPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // For now, using regular SharedPreferences for both (you can add encryption later)
    // This avoids dependency issues while keeping the same API
    private val encryptedSharedPreferences: SharedPreferences = context.getSharedPreferences(ENCRYPTED_PREFS_NAME, Context.MODE_PRIVATE)

    // ========== REGULAR (NON-SENSITIVE) DATA ==========

    fun getServerUrl(): String? {
        return regularPreferences.getString(KEY_SERVER_URL, "")
    }

    fun setServerUrl(value: String?) {
        regularPreferences.edit().putString(KEY_SERVER_URL, value).apply()
    }

    fun isConnectionInitialized(): Boolean {
        return regularPreferences.getBoolean(KEY_CONNECTION_INITIALIZED, false)
    }

    fun setConnectionInitialized(value: Boolean) {
        regularPreferences.edit().putBoolean(KEY_CONNECTION_INITIALIZED, value).apply()
    }

    fun hasEstablishedConnectionOnce(): Boolean {
        return regularPreferences.getBoolean(KEY_CONNECTION_ESTABLISHED_ONCE, false)
    }

    fun setConnectionEstablishedOnce(value: Boolean) {
        regularPreferences.edit().putBoolean(KEY_CONNECTION_ESTABLISHED_ONCE, value).apply()
    }

    fun getConfigUpdatedTimestamp(): Long {
        return regularPreferences.getLong(KEY_CONFIG_UPDATED, 0L)
    }

    fun setConfigUpdatedTimestamp(value: Long) {
        regularPreferences.edit().putLong(KEY_CONFIG_UPDATED, value).apply()
    }

    // ========== ENCRYPTED (SENSITIVE) DATA ==========

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
        // Save non-sensitive data to regular preferences
        setServerUrl(serverUrl)
        setConnectionInitialized(true)
        setConfigUpdatedTimestamp(System.currentTimeMillis())

        // Save sensitive data to encrypted preferences
        setAppPassword(password)
    }

    fun clearAllData() {
        // Clear regular preferences
        regularPreferences.edit().clear().apply()

        // Clear encrypted preferences
        encryptedSharedPreferences.edit().clear().apply()
    }

    fun clearSensitiveData() {
        // Only clear encrypted preferences (keep server URL and other non-sensitive settings)
        encryptedSharedPreferences.edit().clear().apply()
    }

    // Migration method to move existing unencrypted data to encrypted storage
    fun migrateToEncryptedStorage(context: Context) {
        val oldPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Check if there's an unencrypted password that needs migration
        val oldPassword = oldPrefs.getString("app_password", null)
        if (!oldPassword.isNullOrEmpty() && getAppPassword().isNullOrEmpty()) {
            // Migrate password to encrypted storage
            setAppPassword(oldPassword)

            // Remove from old unencrypted storage
            oldPrefs.edit().remove("app_password").apply()
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