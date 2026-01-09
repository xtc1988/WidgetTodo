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
import androidx.compose.ui.graphics.Color

// Zen Garden Widget Colors (NO TRANSPARENCY)
object ZenWidgetColors {
    val Washi = Color(0xFFF5F2EB)        // 和紙 - Background
    val Sand = Color(0xFFE8E4DA)          // 砂 - Item background
    val Moss = Color(0xFF5C7A5C)          // 苔 - Primary accent
    val Wood = Color(0xFF8B7355)          // 木 - Secondary accent
    val Ink = Color(0xFF2C2C2C)           // 墨 - Text
    val White = Color(0xFFFFFFFF)         // 白 - Card
    val Stone = Color(0xFFD4CFC4)         // 石 - Border
}

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
            ZenWidgetContent(todos = todos)
        }
    }
}

@Composable
fun ZenWidgetContent(todos: List<Todo>) {
    // Zen Garden colors - NO transparency
    val backgroundColor = ColorProvider(ZenWidgetColors.Washi)
    val textColor = ColorProvider(ZenWidgetColors.Ink)
    val accentColor = ColorProvider(ZenWidgetColors.Moss)
    val itemBgColor = ColorProvider(ZenWidgetColors.White)
    val borderColor = ColorProvider(ZenWidgetColors.Stone)

    GlanceTheme {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(backgroundColor)
                .cornerRadius(16.dp)
                .padding(12.dp)
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "やること",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor
                        )
                    )
                }

                // Divider
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(borderColor)
                ) {}

                Spacer(modifier = GlanceModifier.height(8.dp))

                // Content
                if (todos.isEmpty()) {
                    ZenEmptyState(textColor)
                } else {
                    ZenTodoList(todos, textColor, accentColor, itemBgColor)
                }
            }

            // FABs at bottom right
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    modifier = GlanceModifier.padding(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // App launch button
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .cornerRadius(8.dp)
                            .background(ColorProvider(ZenWidgetColors.Wood))
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",  // North-east arrow: "open app"
                            style = TextStyle(
                                fontSize = 18.sp,  // Slightly larger for better visibility
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    // Add button
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .cornerRadius(8.dp)
                            .background(accentColor)
                            .clickable(actionStartActivity<AddTodoActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            style = TextStyle(
                                fontSize = 20.sp,
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
fun ZenEmptyState(textColor: ColorProvider) {
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "静寂",
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = ColorProvider(ZenWidgetColors.Stone)
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            Text(
                text = "+をタップして追加",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = ColorProvider(ZenWidgetColors.Stone)
                )
            )
        }
    }
}

@Composable
fun ZenTodoList(
    todos: List<Todo>,
    textColor: ColorProvider,
    accentColor: ColorProvider,
    itemBgColor: ColorProvider
) {
    LazyColumn(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 44.dp)
    ) {
        items(todos, itemId = { it.id }) { todo ->
            ZenTodoItem(
                todo = todo,
                textColor = textColor,
                accentColor = accentColor,
                itemBgColor = itemBgColor
            )
        }
    }
}

@Composable
fun ZenTodoItem(
    todo: Todo,
    textColor: ColorProvider,
    accentColor: ColorProvider,
    itemBgColor: ColorProvider
) {
    val todoIdKey = ActionParameters.Key<Long>("todo_id")

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(itemBgColor)
            .cornerRadius(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent border (Moss green)
        Box(
            modifier = GlanceModifier
                .width(3.dp)
                .height(28.dp)
                .background(accentColor)
        ) {}

        Text(
            text = todo.title,
            style = TextStyle(
                fontSize = 12.sp,
                color = textColor
            ),
            maxLines = 1,
            modifier = GlanceModifier
                .defaultWeight()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        )

        // Checkbox
        Box(
            modifier = GlanceModifier
                .size(20.dp)
                .cornerRadius(4.dp)
                .background(ColorProvider(ZenWidgetColors.Sand))
                .clickable(
                    actionRunCallback<CompleteTodoAction>(
                        actionParametersOf(todoIdKey to todo.id)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✓",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = accentColor
                )
            )
        }

        Spacer(modifier = GlanceModifier.width(6.dp))
    }
}

// Keep old function names for compatibility
@Composable
fun TodoWidgetContent(todos: List<Todo>) = ZenWidgetContent(todos)

@Composable
fun EmptyState(textColor: ColorProvider) = ZenEmptyState(textColor)

@Composable
fun TodoList(todos: List<Todo>, textColor: ColorProvider) {
    ZenTodoList(
        todos,
        textColor,
        ColorProvider(ZenWidgetColors.Moss),
        ColorProvider(ZenWidgetColors.White)
    )
}

@Composable
fun TodoItem(todo: Todo, textColor: ColorProvider) {
    ZenTodoItem(
        todo,
        textColor,
        ColorProvider(ZenWidgetColors.Moss),
        ColorProvider(ZenWidgetColors.White)
    )
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
