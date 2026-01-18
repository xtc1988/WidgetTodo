package com.example.widgettodo.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import com.example.widgettodo.data.local.TodoDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

object TodoWidgetUpdater {
    private const val TAG = "TodoWidgetUpdater"

    suspend fun updateAll(context: Context) {
        Log.d(TAG, "=== updateAll() START ===")

        val manager = GlanceAppWidgetManager(context.applicationContext)
        val glanceIds = manager.getGlanceIds(TodoWidget::class.java)
        Log.d(TAG, "getGlanceIds() returned: ${glanceIds.size} ids = $glanceIds")

        if (glanceIds.isEmpty()) {
            Log.w(TAG, "glanceIds is EMPTY - no widgets found, returning early")
            return
        }

        // データベースからTODOを取得
        val todos = withContext(Dispatchers.IO) {
            val db = TodoDatabase.getInstance(context)
            db.todoDao().getAllTodosOnce()
        }
        Log.d(TAG, "Fetched ${todos.size} todos from DB")

        // TODOをJSON文字列に変換
        val todosJson = JSONArray().apply {
            todos.forEach { todo ->
                put(JSONObject().apply {
                    put("id", todo.id)
                    put("title", todo.title)
                    put("isCompleted", todo.isCompleted)
                    put("createdAt", todo.createdAt)
                })
            }
        }.toString()
        Log.d(TAG, "Todos JSON: $todosJson")

        // 各ウィジェットのStateを更新
        val timestamp = System.currentTimeMillis()
        Log.d(TAG, "Updating state timestamp to: $timestamp")

        glanceIds.forEach { glanceId ->
            Log.d(TAG, "Updating state for glanceId: $glanceId")
            updateAppWidgetState(context, TodoWidgetStateDefinition, glanceId) { prefs: Preferences ->
                prefs.toMutablePreferences().apply {
                    this[TodoWidgetStateDefinition.LAST_UPDATE_KEY] = timestamp
                    this[TodoWidgetStateDefinition.TODOS_JSON_KEY] = todosJson
                }
            }
        }

        // 新しいStateで再描画をトリガーするためupdateAllを呼び出す
        Log.d(TAG, "Calling TodoWidget().updateAll(context)...")
        TodoWidget().updateAll(context)
        Log.d(TAG, "TodoWidget().updateAll(context) completed")

        Log.d(TAG, "=== updateAll() END ===")
    }
}
