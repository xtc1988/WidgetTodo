package com.example.widgettodo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.widgettodo.ui.main.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E Test for WidgetTodo app
 *
 * Tests the complete user flow:
 * 1. App launches with empty state
 * 2. Add a new todo via dialog
 * 3. Todo appears in the list
 * 4. Complete/delete the todo
 * 5. Undo delete via snackbar
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class TodoE2ETest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun appLaunches_showsEmptyStateOrTodos() {
        // App should launch successfully
        composeTestRule.waitForIdle()

        // Either shows empty state message or existing todos
        // This verifies the app doesn't crash on startup
        composeTestRule.onRoot().assertIsDisplayed()
    }

    @Test
    fun addTodo_todoAppearsInList() {
        val testTodoTitle = "E2E Test Todo ${System.currentTimeMillis()}"

        composeTestRule.waitForIdle()

        // Click FAB to open add dialog
        composeTestRule.onNodeWithContentDescription("追加")
            .performClick()

        composeTestRule.waitForIdle()

        // Type in the text field in dialog
        composeTestRule.onNodeWithTag("todo_input")
            .performTextInput(testTodoTitle)

        // Click add button in dialog
        composeTestRule.onNodeWithText("追加")
            .performClick()

        composeTestRule.waitForIdle()

        // Verify the todo appears in the list
        composeTestRule.onNodeWithText(testTodoTitle)
            .assertIsDisplayed()
    }

    @Test
    fun completeTodo_todoRemovedFromList_snackbarShown() {
        val testTodoTitle = "Complete Test ${System.currentTimeMillis()}"

        // First add a todo
        composeTestRule.onNodeWithContentDescription("追加")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("todo_input")
            .performTextInput(testTodoTitle)
        composeTestRule.onNodeWithText("追加")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify it exists
        composeTestRule.onNodeWithText(testTodoTitle)
            .assertIsDisplayed()

        // Click the complete button (check icon) - use first match to handle multiple todos
        composeTestRule.onAllNodesWithContentDescription("完了")[0]
            .performClick()
        composeTestRule.waitForIdle()

        // Wait for snackbar to appear (may take a moment)
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("タスクを削除しました")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Verify snackbar is shown
        composeTestRule.onNodeWithText("タスクを削除しました")
            .assertIsDisplayed()

        // Verify the todo is removed from list
        composeTestRule.onNodeWithText(testTodoTitle)
            .assertDoesNotExist()
    }

    @Test
    fun undoDelete_todoReappearsInList() {
        val testTodoTitle = "Undo Test ${System.currentTimeMillis()}"

        // Add a todo
        composeTestRule.onNodeWithContentDescription("追加")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("todo_input")
            .performTextInput(testTodoTitle)
        composeTestRule.onNodeWithText("追加")
            .performClick()
        composeTestRule.waitForIdle()

        // Complete/delete it - use first match to handle multiple todos
        composeTestRule.onAllNodesWithContentDescription("完了")[0]
            .performClick()
        composeTestRule.waitForIdle()

        // Wait for snackbar to appear
        composeTestRule.waitUntil(timeoutMillis = 3000) {
            composeTestRule.onAllNodesWithText("元に戻す")
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Click undo in snackbar
        composeTestRule.onNodeWithText("元に戻す")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify todo is back
        composeTestRule.onNodeWithText(testTodoTitle)
            .assertIsDisplayed()
    }

    @Test
    fun cancelAddDialog_noTodoAdded() {
        composeTestRule.waitForIdle()

        // Open dialog
        composeTestRule.onNodeWithContentDescription("追加")
            .performClick()
        composeTestRule.waitForIdle()

        // Type something
        composeTestRule.onNodeWithTag("todo_input")
            .performTextInput("Should not be added")

        // Cancel
        composeTestRule.onNodeWithText("キャンセル")
            .performClick()
        composeTestRule.waitForIdle()

        // Verify it was not added
        composeTestRule.onNodeWithText("Should not be added")
            .assertDoesNotExist()
    }

    @Test
    fun multipleTodos_allDisplayedInList() {
        val timestamp = System.currentTimeMillis()
        val todos = listOf(
            "First $timestamp",
            "Second $timestamp",
            "Third $timestamp"
        )

        // Add multiple todos
        todos.forEach { title ->
            composeTestRule.onNodeWithContentDescription("追加")
                .performClick()
            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithTag("todo_input")
                .performTextInput(title)
            composeTestRule.onNodeWithText("追加")
                .performClick()
            composeTestRule.waitForIdle()
        }

        // Verify all are displayed
        todos.forEach { title ->
            composeTestRule.onNodeWithText(title)
                .assertIsDisplayed()
        }
    }
}
