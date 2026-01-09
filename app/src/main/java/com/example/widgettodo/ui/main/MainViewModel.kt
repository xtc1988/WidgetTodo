package com.example.widgettodo.ui.main

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import com.example.widgettodo.widget.TodoWidget
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
        android.util.Log.d("MainViewModel", "updateWidget() called")
        try {
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(TodoWidget::class.java)
            android.util.Log.d("MainViewModel", "Found ${glanceIds.size} widgets to update")
            glanceIds.forEach { glanceId ->
                android.util.Log.d("MainViewModel", "Updating widget: $glanceId")
                TodoWidget().update(context, glanceId)
            }
            android.util.Log.d("MainViewModel", "Widget update completed")
        } catch (e: Exception) {
            android.util.Log.e("MainViewModel", "Widget update failed", e)
        }
    }

    fun deleteTodo(todo: Todo) {
        viewModelScope.launch {
            repository.deleteTodo(todo)
            _uiState.value = _uiState.value.copy(lastDeletedTodo = todo)
        }
    }

    fun undoDelete() {
        val todo = _uiState.value.lastDeletedTodo ?: return
        viewModelScope.launch {
            repository.undoDelete(todo)
            _uiState.value = _uiState.value.copy(lastDeletedTodo = null)
        }
    }

    fun clearLastDeleted() {
        _uiState.value = _uiState.value.copy(lastDeletedTodo = null)
    }
}
