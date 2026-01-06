package com.example.widgettodo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainUiState(
    val todos: List<Todo> = emptyList(),
    val lastDeletedTodo: Todo? = null
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: TodoRepository
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
            repository.addTodo(title)
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
