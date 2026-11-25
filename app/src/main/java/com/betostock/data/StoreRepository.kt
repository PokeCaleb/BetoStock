package com.betostock.data

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

class StoreRepository(private val db: AppDatabase) {
    private val productDao = db.productDao()
    private val supplierDao = db.supplierDao()
    private val saleDao = db.saleDao()
    private val orderDao = db.orderDao()

    val products: Flow<List<Product>> = productDao.observeAll()
    val suppliers: Flow<List<Supplier>> = supplierDao.observeAll()
    val orders: Flow<List<OrderWithItems>> = orderDao.observeAll()

    fun salesBetween(from: Long, to: Long): Flow<List<SaleWithItems>> = saleDao.observeBetween(from, to)
    fun sale(id: Long): Flow<SaleWithItems?> = saleDao.observeById(id)
    fun order(id: Long): Flow<OrderWithItems?> = orderDao.observeById(id)
    fun receivedOrdersBetween(from: Long, to: Long): Flow<List<PurchaseOrder>> =
        orderDao.observeReceivedBetween(from, to)

    suspend fun product(id: Long): Product? = productDao.getById(id)
    suspend fun supplier(id: Long): Supplier? = supplierDao.getById(id)
    suspend fun productCount(): Int = productDao.count()

    // ---------- Productos (RF-01, RF-02) ----------

    suspend fun saveProduct(product: Product): Long =
        if (product.id == 0L) {
            productDao.insert(product)
        } else {
            productDao.update(product)
            product.id
        }

    suspend fun deleteProduct(product: Product) = productDao.delete(product)

    // ---------- Proveedores ----------

    suspend fun saveSupplier(supplier: Supplier): Long =
        if (supplier.id == 0L) {
            supplierDao.insert(supplier)
        } else {
            supplierDao.update(supplier)
            supplier.id
        }

    suspend fun deleteSupplier(supplier: Supplier) = supplierDao.delete(supplier)

    // ---------- Ventas (RF-03, RF-04, RF-05) ----------

    /**
     * Guarda la venta y descuenta el stock en una sola transacción.
     * Si algún producto no tiene suficiente stock, se cancela todo.
     */
    suspend fun registerSale(lines: List<CartLine>, paidWith: Double): Long = db.withTransaction {
        val total = lines.sumOf { it.subtotal }.round2()
        val profit = lines.sumOf { (it.product.salePrice - it.product.costPrice) * it.quantity }.round2()

        for (line in lines) {
            val updated = productDao.decreaseStock(line.product.id, line.quantity)
            check(updated > 0) { "No hay suficiente ${line.product.name} en el inventario." }
        }

        val saleId = saleDao.insertSale(
            Sale(
                timestamp = System.currentTimeMillis(),
                total = total,
                paidWith = paidWith.round2(),
                change = (paidWith - total).round2(),
                profit = profit
            )
        )
        saleDao.insertItems(
            lines.map {
                SaleItem(
                    saleId = saleId,
                    productId = it.product.id,
                    productName = it.product.name,
                    unit = it.product.unit,
                    quantity = it.quantity,
                    unitPrice = it.product.salePrice,
                    unitCost = it.product.costPrice
                )
            }
        )
        saleId
    }

    // ---------- Pedidos (RF-06, RF-08, RF-09) ----------

    suspend fun createOrder(supplier: Supplier, lines: List<OrderLine>): Long = db.withTransaction {
        val orderId = orderDao.insertOrder(
            PurchaseOrder(
                supplierId = supplier.id,
                supplierName = supplier.name,
                createdAt = System.currentTimeMillis(),
                status = OrderStatus.PENDING,
                total = lines.sumOf { it.subtotal }.round2()
            )
        )
        orderDao.insertItems(
            lines.map {
                PurchaseOrderItem(
                    orderId = orderId,
                    productId = it.product.id,
                    productName = it.product.name,
                    unit = it.product.unit,
                    quantity = it.quantity,
                    unitCost = it.product.costPrice
                )
            }
        )
        orderId
    }

    /** Marca el pedido como recibido y suma sus productos al inventario (RF-09). */
    suspend fun receiveOrder(orderId: Long) {
        db.withTransaction {
            val data = orderDao.getWithItems(orderId)
            if (data != null && data.order.status == OrderStatus.PENDING) {
                data.items.forEach { productDao.increaseStock(it.productId, it.quantity) }
                orderDao.markStatus(orderId, OrderStatus.RECEIVED, System.currentTimeMillis())
            }
        }
    }
}
