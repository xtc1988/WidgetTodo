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
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

// 共有ActionParametersキー（トップレベル定数である必要あり）
private val TODO_ID_KEY = ActionParameters.Key<Long>("todo_id")

// 禅庭園ウィジェットカラー（透明度なし）
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

    // GlanceStateDefinitionを使用してState変更時に更新をトリガー
    override val stateDefinition = TodoWidgetStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        android.util.Log.d("TodoWidget", "=== provideGlance() START === id: $id, instance: $this")

        provideContent {
            // Composable内でStateを読み取り、State変更に反応させる
            val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
            val lastUpdate = prefs[TodoWidgetStateDefinition.LAST_UPDATE_KEY] ?: 0L
            val todosJson = prefs[TodoWidgetStateDefinition.TODOS_JSON_KEY] ?: "[]"

            android.util.Log.d("TodoWidget", "State lastUpdate timestamp: $lastUpdate")
            android.util.Log.d("TodoWidget", "State todosJson: $todosJson")

            // JSONからTODOをパース
            val todos = parseTodosFromJson(todosJson)
            android.util.Log.d("TodoWidget", "Parsed ${todos.size} todos from state")

            ZenWidgetContent(todos = todos)
        }
        android.util.Log.d("TodoWidget", "=== provideGlance() END ===")
    }

    private fun parseTodosFromJson(json: String): List<Todo> {
        return try {
            val jsonArray = org.json.JSONArray(json)
            (0 until jsonArray.length()).map { i ->
                val obj = jsonArray.getJSONObject(i)
                Todo(
                    id = obj.getLong("id"),
                    title = obj.getString("title"),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("TodoWidget", "Error parsing todos JSON", e)
            emptyList()
        }
    }
}

@Composable
fun ZenWidgetContent(todos: List<Todo>) {
    // 禅庭園カラー - 透明度なし
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
                // ヘッダー
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

                // 区切り線
                Box(
                    modifier = GlanceModifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(borderColor)
                ) {}

                Spacer(modifier = GlanceModifier.height(8.dp))

                // コンテンツ
                if (todos.isEmpty()) {
                    ZenEmptyState(textColor)
                } else {
                    ZenTodoList(todos, textColor, accentColor, itemBgColor)
                }
            }

            // 右下のFABボタン
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd
            ) {
                Row(
                    modifier = GlanceModifier.padding(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    // アプリ起動ボタン
                    Box(
                        modifier = GlanceModifier
                            .size(36.dp)
                            .cornerRadius(8.dp)
                            .background(ColorProvider(ZenWidgetColors.Wood))
                            .clickable(actionStartActivity<MainActivity>()),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↗",  // 北東矢印: アプリを開く
                            style = TextStyle(
                                fontSize = 18.sp,  // 視認性向上のため少し大きめ
                                fontWeight = FontWeight.Bold,
                                color = ColorProvider(Color.White)
                            )
                        )
                    }
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    // 追加ボタン
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
    // LazyColumnではなくColumnを使用（LazyColumn内のclickableが動作しない問題の回避）
    Column(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(bottom = 44.dp)
    ) {
        todos.forEach { todo ->
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
    // Boxでラップしてclickableを付ける（LazyColumn内での動作改善）
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .background(itemBgColor)
            .cornerRadius(4.dp)
            .clickable(
                actionRunCallback<CompleteTodoAction>(
                    actionParametersOf(TODO_ID_KEY to todo.id)
                )
            )
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側アクセントボーダー（苔色）
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

            // チェックボックスインジケーター
            Box(
                modifier = GlanceModifier
                    .size(20.dp)
                    .cornerRadius(4.dp)
                    .background(
                        if (todo.isCompleted) ColorProvider(ZenWidgetColors.Moss)
                        else ColorProvider(ZenWidgetColors.Sand)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (todo.isCompleted) {
                    Text(
                        text = "✓",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(Color.White)
                        )
                    )
                }
            }

            Spacer(modifier = GlanceModifier.width(6.dp))
        }
    }
}

// 互換性のため旧関数名を維持
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
        android.util.Log.d("CompleteTodoAction", "=== onAction CALLED ===")
        val todoId = parameters[TODO_ID_KEY]
        if (todoId == null) {
            android.util.Log.e("CompleteTodoAction", "todoId is NULL!")
            return
        }

        // Step 1: チェック済みに更新
        withContext(Dispatchers.IO) {
            val db = TodoDatabase.getInstance(context)
            db.todoDao().updateCompleted(todoId, true)
        }
        TodoWidgetUpdater.updateAll(context)

        // Step 2: 遅延（500ms）
        delay(500)

        // Step 3: 削除
        withContext(Dispatchers.IO) {
            val db = TodoDatabase.getInstance(context)
            db.todoDao().deleteById(todoId)
        }
        TodoWidgetUpdater.updateAll(context)
    }
}
