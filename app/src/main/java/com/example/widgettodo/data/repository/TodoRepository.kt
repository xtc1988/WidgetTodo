package com.example.widgettodo.data.repository

import com.example.widgettodo.data.local.TodoDao
import com.example.widgettodo.data.local.entity.Todo
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoDao: TodoDao
) {
    fun getAllTodos(): Flow<List<Todo>> = todoDao.getAllTodos()

    suspend fun getAllTodosOnce(): List<Todo> = todoDao.getAllTodosOnce()

    suspend fun addTodo(title: String): Long {
        val todo = Todo(title = title)
        return todoDao.insert(todo)
    }

    suspend fun deleteTodo(todo: Todo) {
        todoDao.delete(todo)
    }

    suspend fun deleteTodoById(id: Long) {
        todoDao.deleteById(id)
    }

    suspend fun undoDelete(todo: Todo): Long {
        return todoDao.insert(todo)
    }
}
