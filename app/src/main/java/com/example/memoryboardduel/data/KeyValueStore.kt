package com.example.memoryboardduel.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/** Minimal key-value abstraction so repositories can be unit tested without Android. */
interface KeyValueStore {
    fun getInt(key: String, default: Int): Int
    fun putInt(key: String, value: Int)
    fun getBoolean(key: String, default: Boolean): Boolean
    fun putBoolean(key: String, value: Boolean)
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
    fun remove(keys: Collection<String>)
}

class SharedPreferencesStore(private val prefs: SharedPreferences) : KeyValueStore {

    constructor(context: Context, name: String) :
        this(context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE))

    override fun getInt(key: String, default: Int) = prefs.getInt(key, default)
    override fun putInt(key: String, value: Int) = prefs.edit { putInt(key, value) }
    override fun getBoolean(key: String, default: Boolean) = prefs.getBoolean(key, default)
    override fun putBoolean(key: String, value: Boolean) = prefs.edit { putBoolean(key, value) }
    override fun getString(key: String, default: String) = prefs.getString(key, default) ?: default
    override fun putString(key: String, value: String) = prefs.edit { putString(key, value) }
    override fun remove(keys: Collection<String>) = prefs.edit { keys.forEach { remove(it) } }
}

class InMemoryStore : KeyValueStore {
    private val values = mutableMapOf<String, Any>()

    override fun getInt(key: String, default: Int) = values[key] as? Int ?: default
    override fun putInt(key: String, value: Int) { values[key] = value }
    override fun getBoolean(key: String, default: Boolean) = values[key] as? Boolean ?: default
    override fun putBoolean(key: String, value: Boolean) { values[key] = value }
    override fun getString(key: String, default: String) = values[key] as? String ?: default
    override fun putString(key: String, value: String) { values[key] = value }
    override fun remove(keys: Collection<String>) { keys.forEach { values.remove(it) } }
}
