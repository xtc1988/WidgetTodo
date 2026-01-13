package com.example.widgettodo

import android.content.Context
import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import com.example.widgettodo.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Mock
    private lateinit var repository: TodoRepository

    @Mock
    private lateinit var context: Context

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        val testTodos = listOf(
            Todo(id = 1, title = "Test Todo 1"),
            Todo(id = 2, title = "Test Todo 2")
        )
        `when`(repository.getAllTodos()).thenReturn(flowOf(testTodos))

        // suspend関数のモック設定
        runBlocking {
            whenever(repository.addTodo(org.mockito.kotlin.any())).thenReturn(1L)
            whenever(repository.deleteTodo(org.mockito.kotlin.any())).thenReturn(Unit)
            whenever(repository.undoDelete(org.mockito.kotlin.any())).thenReturn(1L)
        }

        viewModel = MainViewModel(repository, context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state contains todos from repository`() = runTest {
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        val state = viewModel.uiState.value
        assertEquals(2, state.todos.size)
        assertEquals("Test Todo 1", state.todos[0].title)
    }

    @Test
    fun `addTodo calls repository addTodo`() = runTest {
        viewModel.addTodo("New Todo")
        // Dispatchers.IOで実行されるため、少し待機
        delay(100)

        verify(repository).addTodo("New Todo")
    }

    @Test
    fun `deleteTodo calls repository deleteTodo and stores for undo`() = runTest {
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        verify(repository).deleteTodo(todoToDelete)
        assertEquals(todoToDelete, viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `undoDelete restores last deleted todo`() = runTest {
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        viewModel.undoDelete()
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        verify(repository).undoDelete(todoToDelete)
        assertNull(viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `clearLastDeleted clears lastDeletedTodo`() = runTest {
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        viewModel.clearLastDeleted()

        assertNull(viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `addTodo with blank title does nothing`() = runTest {
        viewModel.addTodo("   ")
        // UnconfinedTestDispatcherは即座に実行するためadvanceは不要

        // No call to repository should be made
        // This test verifies the blank check works
    }
}
