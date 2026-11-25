package com.betostock.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getById(id: Long): Product?

    @Query("SELECT COUNT(*) FROM products")
    suspend fun count(): Int

    @Insert
    suspend fun insert(product: Product): Long

    @Update
    suspend fun update(product: Product)

    @Delete
    suspend fun delete(product: Product)

    /** Descuenta stock solo si alcanza. Devuelve 0 filas si no hay suficiente. */
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :id AND stock + 0.0001 >= :qty")
    suspend fun decreaseStock(id: Long, qty: Double): Int

    @Query("UPDATE products SET stock = stock + :qty WHERE id = :id")
    suspend fun increaseStock(id: Long, qty: Double)
}

@Dao
interface SupplierDao {
    @Query("SELECT * FROM suppliers ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getById(id: Long): Supplier?

    @Insert
    suspend fun insert(supplier: Supplier): Long

    @Update
    suspend fun update(supplier: Supplier)

    @Delete
    suspend fun delete(supplier: Supplier)
}

@Dao
interface SaleDao {
    @Insert
    suspend fun insertSale(sale: Sale): Long

    @Insert
    suspend fun insertItems(items: List<SaleItem>)

    @Transaction
    @Query("SELECT * FROM sales WHERE timestamp BETWEEN :from AND :to ORDER BY timestamp DESC")
    fun observeBetween(from: Long, to: Long): Flow<List<SaleWithItems>>

    @Transaction
    @Query("SELECT * FROM sales WHERE id = :id")
    fun observeById(id: Long): Flow<SaleWithItems?>
}

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: PurchaseOrder): Long

    @Insert
    suspend fun insertItems(items: List<PurchaseOrderItem>)

    @Transaction
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<OrderWithItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    fun observeById(id: Long): Flow<OrderWithItems?>

    /** Pedidos que llegaron (se recibieron) dentro del rango: se descuentan del dinero del día. */
    @Query("SELECT * FROM orders WHERE status = 'RECEIVED' AND receivedAt BETWEEN :from AND :to ORDER BY receivedAt DESC")
    fun observeReceivedBetween(from: Long, to: Long): Flow<List<PurchaseOrder>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :id")
    suspend fun getWithItems(id: Long): OrderWithItems?

    @Query("UPDATE orders SET status = :status, receivedAt = :receivedAt WHERE id = :id")
    suspend fun markStatus(id: Long, status: OrderStatus, receivedAt: Long?)
}
