package com.example.widgettodo.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager

object TodoWidgetUpdater {
    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context.applicationContext)
        val glanceIds = manager.getGlanceIds(TodoWidget::class.java)
        if (glanceIds.isEmpty()) return

        val widget = TodoWidget()
        glanceIds.forEach { glanceId ->
            widget.update(context, glanceId)
        }
    }
}
