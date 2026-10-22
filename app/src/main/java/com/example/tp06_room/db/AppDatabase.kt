package com.example.tp06_room.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.tp06_room.dao.ProductDao
import com.example.tp06_room.model.Product

@Database(entities = [Product::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}
