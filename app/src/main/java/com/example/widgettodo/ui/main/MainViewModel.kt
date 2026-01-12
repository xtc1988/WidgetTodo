package com.example.widgettodo.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import com.example.widgettodo.widget.TodoWidgetUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class MainUiState(
    val todos: List<Todo> = emptyList(),
    val lastDeletedTodo: Todo? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TodoRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllTodos().collect { todos ->
                _uiState.value = _uiState.value.copy(todos = todos)
                android.util.Log.d("MainViewModel", "Todos updated; refreshing widget")
                updateWidget()
            }
        }
    }

    fun addTodo(title: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            // DB書き込みをIOスレッドで実行し、完了を待つ
            withContext(Dispatchers.IO) {
                repository.addTodo(title)
            }
            // DB書き込み完了後にウィジェット更新
            updateWidget()
        }
    }

    private suspend fun updateWidget() {
        android.util.Log.d("MainViewModel", "updateWidget() called - using updateAll()")
        try {
            // アクティブなウィジェットを確実に更新
            TodoWidgetUpdater.updateAll(context)
            android.util.Log.d("MainViewModel", "Widget updateAll() completed")
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Widget update failed", e)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
            _uiState.value = _uiState.value.copy(lastDeletedTodo = todo)
            android.util.Log.d("MainViewModel", "Todo deleted; refreshing widget")
            updateWidget()
        }
    }

    fun undoDelete() {
        val todo = _uiState.value.lastDeletedTodo ?: return
        viewModelScope.launch {
            repository.undoDelete(todo)
            _uiState.value = _uiState.value.copy(lastDeletedTodo = null)
            android.util.Log.d("MainViewModel", "Todo undo; refreshing widget")
            updateWidget()
        }
    }

    fun clearLastDeleted() {
        _uiState.value = _uiState.value.copy(lastDeletedTodo = null)
    }
}
