package com.example.widgettodo.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.widgettodo.ui.theme.WidgetTodoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WidgetTodoTheme {
                val viewModel: MainViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                MainScreen(
                    uiState = uiState,
                    onAddTodo = viewModel::addTodo,
                    onDeleteTodo = viewModel::deleteTodo,
                    onUndo = viewModel::undoDelete,
                    onClearLastDeleted = viewModel::clearLastDeleted
                )
            }
        }
    }
}
