package com.example.widgettodo.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.widgettodo.R
import com.example.widgettodo.data.local.entity.Todo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    uiState: MainUiState,
    onAddTodo: (String) -> Unit,
    onDeleteTodo: (Todo) -> Unit,
    onUndo: () -> Unit,
    onClearLastDeleted: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.lastDeletedTodo) {
        uiState.lastDeletedTodo?.let {
            val result = snackbarHostState.showSnackbar(
                message = "タスクを削除しました",
                actionLabel = "元に戻す",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onUndo()
            } else {
                onClearLastDeleted()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "追加")
            }
        }
    ) { paddingValues ->
        if (uiState.todos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.empty_state_message),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.todos, key = { it.id }) { todo ->
                    TodoItem(
                        todo = todo,
                        onComplete = { onDeleteTodo(todo) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title ->
                onAddTodo(title)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TodoItem(
    todo: Todo,
    onComplete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = todo.title,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
            IconButton(onClick = onComplete) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "完了",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新しいタスク") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text(stringResource(R.string.add_todo_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("todo_input")
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(text) },
                enabled = text.isNotBlank()
            ) {
                Text(stringResource(R.string.add_button))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel_button))
            }
        }
    )
}
