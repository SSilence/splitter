package de.aditu.splitter.repository

import android.content.Context

private const val SHARED_PREF_NAME = "SPLITTER"
private const val LAST_MODIFIED_KEY = "LAST_MODIFIED_KEY"
private const val LAST_REFRESH_KEY = "LAST_REFRESH_KEY"

class SharedPreferencesRepository(private val context: Context) {

    var lastModified: Long
        get() = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).getLong(LAST_MODIFIED_KEY, 0)
        set(value: Long) {
            context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .apply {
                    putLong(LAST_MODIFIED_KEY, value)
                    commit()
                }
        }

    var lastRefresh: String
        get() = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE).getString(LAST_REFRESH_KEY, "") ?: ""
        set(value: String) {
            context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .apply {
                    putString(LAST_REFRESH_KEY, value)
                    commit()
                }
        }

}