package com.example.widgettodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.widgettodo.data.local.entity.Todo

@Database(entities = [Todo::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
