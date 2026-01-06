package com.example.widgettodo

import com.example.widgettodo.data.local.TodoDao
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class TodoRepositoryTest {

    @Mock
    private lateinit var todoDao: TodoDao

    private lateinit var repository: TodoRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = TodoRepository(todoDao)
    }

    @Test
    fun `getAllTodos returns flow from dao`() = runTest {
        val testTodos = listOf(
            Todo(id = 1, title = "Todo 1"),
            Todo(id = 2, title = "Todo 2")
        )
        `when`(todoDao.getAllTodos()).thenReturn(flowOf(testTodos))

        val result = repository.getAllTodos().first()

        assertEquals(2, result.size)
        assertEquals("Todo 1", result[0].title)
        assertEquals("Todo 2", result[1].title)
    }

    @Test
    fun `addTodo inserts new todo with title`() = runTest {
        `when`(todoDao.insert(any())).thenReturn(1L)

        repository.addTodo("New Task")

        verify(todoDao).insert(argThat { title == "New Task" })
    }

    @Test
    fun `deleteTodo calls dao delete`() = runTest {
        val todoToDelete = Todo(id = 1, title = "Delete me")

        repository.deleteTodo(todoToDelete)

        verify(todoDao).delete(todoToDelete)
    }

    @Test
    fun `undoDelete calls dao insert`() = runTest {
        val todoToRestore = Todo(id = 1, title = "Restore me")
        `when`(todoDao.insert(todoToRestore)).thenReturn(1L)

        repository.undoDelete(todoToRestore)

        verify(todoDao).insert(todoToRestore)
    }

    @Test
    fun `deleteTodoById calls dao deleteById`() = runTest {
        repository.deleteTodoById(42L)

        verify(todoDao).deleteById(42L)
    }

    @Test
    fun `getAllTodosOnce returns list from dao`() = runTest {
        val testTodos = listOf(
            Todo(id = 1, title = "Todo 1"),
            Todo(id = 2, title = "Todo 2")
        )
        `when`(todoDao.getAllTodosOnce()).thenReturn(testTodos)

        val result = repository.getAllTodosOnce()

        assertEquals(2, result.size)
        assertEquals("Todo 1", result[0].title)
    }
}
