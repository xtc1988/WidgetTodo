package com.example.widgettodo.ui.add

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.widgettodo.R
import com.example.widgettodo.data.repository.TodoRepository
import com.example.widgettodo.ui.theme.WidgetTodoTheme
import com.example.widgettodo.widget.TodoWidgetUpdater
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class AddTodoActivity : ComponentActivity() {

    @Inject
    lateinit var repository: TodoRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            WidgetTodoTheme {
                var text by remember { mutableStateOf("") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = "新しいタスク",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = text,
                            onValueChange = { text = it },
                            placeholder = { Text(stringResource(R.string.add_todo_hint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { finish() }) {
                                Text(stringResource(R.string.cancel_button))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (text.isNotBlank()) {
                                        addTodoAndFinish(text)
                                    }
                                },
                                enabled = text.isNotBlank()
                            ) {
                                Text(stringResource(R.string.add_button))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun addTodoAndFinish(title: String) {
        lifecycleScope.launch {
            // DB書き込みをIOスレッドで実行し、完了を待つ
            withContext(Dispatchers.IO) {
                repository.addTodo(title)
            }
            // DB書き込み完了後にウィジェット更新
            updateWidget()
            finish()
        }
    }

    private suspend fun updateWidget() {
        // アクティブなウィジェットを確実に更新
        TodoWidgetUpdater.updateAll(applicationContext)
    }
}
