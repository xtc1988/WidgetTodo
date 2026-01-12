package com.example.widgettodo.widget

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager

object TodoWidgetUpdater {
    suspend fun updateAll(context: Context) {
        Log.d("TodoWidgetUpdater", "updateAll() start")
        val manager = GlanceAppWidgetManager(context.applicationContext)
        val glanceIds = manager.getGlanceIds(TodoWidget::class.java)
        Log.d("TodoWidgetUpdater", "updateAll() glanceIds size=${glanceIds.size}")
        if (glanceIds.isEmpty()) {
            Log.d("TodoWidgetUpdater", "updateAll() no widgets to update")
            return
        }

        val widget = TodoWidget()
        glanceIds.forEach { glanceId ->
            Log.d("TodoWidgetUpdater", "updateAll() updating widget id=$glanceId")
            widget.update(context, glanceId)
        }
        Log.d("TodoWidgetUpdater", "updateAll() completed")
    }
}
