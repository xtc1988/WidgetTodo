package com.example.widgettodo.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.widgettodo.data.local.entity.Todo
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    fun getAllTodos(): Flow<List<Todo>>

    @Query("SELECT * FROM todos ORDER BY createdAt DESC")
    suspend fun getAllTodosOnce(): List<Todo>

    @Insert
    suspend fun insert(todo: Todo): Long

    @Delete
    suspend fun delete(todo: Todo)

    @Query("DELETE FROM todos WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE todos SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun updateCompleted(id: Long, isCompleted: Boolean)
}
