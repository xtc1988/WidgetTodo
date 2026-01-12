package com.example.widgettodo.widget

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.state.GlanceStateDefinition
import java.io.File

/**
 * GlanceStateDefinition for TodoWidget.
 * Uses DataStore Preferences to track update timestamps and todo data.
 * When the state changes, Glance will re-compose the widget.
 */
object TodoWidgetStateDefinition : GlanceStateDefinition<Preferences> {

    private const val DATA_STORE_NAME = "todo_widget_prefs"

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATA_STORE_NAME)

    // Key for tracking last update time
    val LAST_UPDATE_KEY = longPreferencesKey("last_update_timestamp")

    // Key for storing todo data as JSON string
    val TODOS_JSON_KEY = stringPreferencesKey("todos_json")

    override suspend fun getDataStore(context: Context, fileKey: String): DataStore<Preferences> {
        return context.dataStore
    }

    override fun getLocation(context: Context, fileKey: String): File {
        return File(context.filesDir, "datastore/$DATA_STORE_NAME.preferences_pb")
    }
}
