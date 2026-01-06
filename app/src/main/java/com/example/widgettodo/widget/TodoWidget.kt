package com.example.widgettodo.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.local.TodoDatabase
import com.example.widgettodo.ui.add.AddTodoActivity
import com.example.widgettodo.ui.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.room.Room
import androidx.glance.color.ColorProviders
import androidx.compose.ui.graphics.Color

class TodoWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Single

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val todos = withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java,
                "todo_database"
            ).build()
            db.todoDao().getAllTodosOnce()
        }

        provideContent {
            TodoWidgetContent(todos = todos)
        }
    }
}

@Composable
fun TodoWidgetContent(todos: List<Todo>) {
    val backgroundColor = ColorProvider(Color(0x80FFFFFF))
    val textColor = ColorProvider(Color.Black)
    val accentColor = ColorProvider(Color(0xFF6200EE))

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(8.dp)
        ) {
            if (todos.isEmpty()) {
                EmptyState(textColor)
            } else {
                TodoList(todos, textColor)
            }

            // FABs at bottom right
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    modifier = GlanceModifier.padding(8.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // App launch button
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .cornerRadius(20.dp)
                            .background(accentColor)
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "📱",
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    // Add button
                    Box(
                        modifier = GlanceModifier
                            .size(40.dp)
                            .cornerRadius(20.dp)
                            .background(accentColor)
                            .clickable(actionStartActivity<AddTodoActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(textColor: ColorProvider) {
    Box(
        modifier = GlanceModifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "+をタップしてタスクを追加",
            style = TextStyle(
                fontSize = 14.sp,
                color = textColor
            )
        )
    }
}

@Composable
fun TodoList(todos: List<Todo>, textColor: ColorProvider) {
    LazyColumn(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 56.dp)
    ) {
        items(todos, itemId = { it.id }) { todo ->
            TodoItem(todo = todo, textColor = textColor)
        }
    }
}

@Composable
fun TodoItem(todo: Todo, textColor: ColorProvider) {
    val todoIdKey = ActionParameters.Key<Long>("todo_id")

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Checkbox
        Box(
            modifier = GlanceModifier
                .size(24.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(Color(0x40000000)))
                .clickable(
                    actionRunCallback<CompleteTodoAction>(
                        actionParametersOf(todoIdKey to todo.id)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "☐",
                style = TextStyle(fontSize = 16.sp, color = textColor)
            )
        }
        Spacer(modifier = GlanceModifier.width(8.dp))
        Text(
            text = todo.title,
            style = TextStyle(
                fontSize = 14.sp,
                color = textColor
            ),
            maxLines = 1,
            modifier = GlanceModifier.defaultWeight()
        )
    }
}

class CompleteTodoAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val todoIdKey = ActionParameters.Key<Long>("todo_id")
        val todoId = parameters[todoIdKey] ?: return

        withContext(Dispatchers.IO) {
            val db = Room.databaseBuilder(
                context.applicationContext,
                TodoDatabase::class.java,
                "todo_database"
            ).build()
            db.todoDao().deleteById(todoId)
        }

        TodoWidget().update(context, glanceId)
    }
}
