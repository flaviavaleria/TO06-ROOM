package com.example.tp06_room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.tp06_room.model.Product

@Dao
interface ProductDao {
    // suspend: o Room não permite consultas na main thread
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(product: Product)

    @Update
    suspend fun update(product: Product)
}
