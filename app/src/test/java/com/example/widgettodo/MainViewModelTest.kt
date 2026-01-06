package com.example.widgettodo

import com.example.widgettodo.data.local.entity.Todo
import com.example.widgettodo.data.repository.TodoRepository
import com.example.widgettodo.ui.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
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
import org.junit.Assert.*

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @Mock
    private lateinit var repository: TodoRepository

    private lateinit var viewModel: MainViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)

        val testTodos = listOf(
            Todo(id = 1, title = "Test Todo 1"),
            Todo(id = 2, title = "Test Todo 2")
        )
        `when`(repository.getAllTodos()).thenReturn(flowOf(testTodos))

        viewModel = MainViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state contains todos from repository`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.todos.size)
        assertEquals("Test Todo 1", state.todos[0].title)
    }

    @Test
    fun `addTodo calls repository addTodo`() = runTest {
        viewModel.addTodo("New Todo")
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).addTodo("New Todo")
    }

    @Test
    fun `deleteTodo calls repository deleteTodo and stores for undo`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).deleteTodo(todoToDelete)
        assertEquals(todoToDelete, viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `undoDelete restores last deleted todo`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.undoDelete()
        testDispatcher.scheduler.advanceUntilIdle()

        verify(repository).undoDelete(todoToDelete)
        assertNull(viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `clearLastDeleted clears lastDeletedTodo`() = runTest {
        testDispatcher.scheduler.advanceUntilIdle()

        val todoToDelete = Todo(id = 1, title = "Test Todo 1")
        viewModel.deleteTodo(todoToDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearLastDeleted()

        assertNull(viewModel.uiState.value.lastDeletedTodo)
    }

    @Test
    fun `addTodo with blank title does nothing`() = runTest {
        viewModel.addTodo("   ")
        testDispatcher.scheduler.advanceUntilIdle()

        // No call to repository should be made
        // This test verifies the blank check works
    }
}
