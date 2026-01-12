package com.example.widgettodo.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.widgettodo.BuildConfig
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
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "やること",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 2.sp
                        )
                    )
                },
                actions = {
                    Text(
                        text = "v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "静寂",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Light,
                            letterSpacing = 8.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Text(
                        text = stringResource(R.string.empty_state_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.todos, key = { it.id }) { todo ->
                    ZenTodoItem(
                        todo = todo,
                        onComplete = { onDeleteTodo(todo) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ZenAddTodoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { title ->
                onAddTodo(title)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ZenTodoItem(
    todo: Todo,
    onComplete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left accent border (Moss green)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(56.dp)
                    .background(MaterialTheme.colorScheme.primary)
            )

            Text(
                text = todo.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(
                    letterSpacing = 0.5.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            IconButton(
                onClick = onComplete,
                modifier = Modifier.padding(end = 8.dp)
            ) {
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
fun ZenAddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "新しいやること",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.sp
                )
            )
        },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = {
                    Text(
                        stringResource(R.string.add_todo_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("todo_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(text) },
                enabled = text.isNotBlank()
            ) {
                Text(
                    stringResource(R.string.add_button),
                    color = if (text.isNotBlank())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(R.string.cancel_button),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

// Keep old names for compatibility
@Composable
fun TodoItem(
    todo: Todo,
    onComplete: () -> Unit
) = ZenTodoItem(todo, onComplete)

@Composable
fun AddTodoDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) = ZenAddTodoDialog(onDismiss, onAdd)
