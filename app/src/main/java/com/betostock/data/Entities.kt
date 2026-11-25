package com.betostock.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class SaleUnit(val label: String, val short: String, val step: Double) {
    PIECE("Por pieza", "pza", 1.0),
    KILO("Por kilo", "kg", 0.5),
    LITER("Por litro", "L", 0.5)
}

enum class Category(val label: String) {
    BEBIDAS("Bebidas"),
    BOTANAS("Botanas"),
    LACTEOS("Lácteos"),
    ABARROTES("Abarrotes"),
    LIMPIEZA("Limpieza")
}

enum class OrderStatus { PENDING, RECEIVED }

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val contactName: String = "",
    val phone: String = "",
    /** Códigos de día separados por coma: L,M,X,J,V,S,D */
    val visitDays: String = "",
    val notes: String = ""
)

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("supplierId")]
)
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: Category = Category.ABARROTES,
    val unit: SaleUnit = SaleUnit.PIECE,
    val costPrice: Double = 0.0,
    val salePrice: Double = 0.0,
    val stock: Double = 0.0,
    val minStock: Double = 0.0,
    val supplierId: Long? = null
)

val Product.isOut: Boolean get() = stock <= 0.0
val Product.isLow: Boolean get() = stock > 0.0 && stock <= minStock
val Product.needsRestock: Boolean get() = stock <= minStock

@Entity(tableName = "sales")
data class Sale(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val total: Double,
    val paidWith: Double,
    val change: Double,
    val profit: Double
)

@Entity(
    tableName = "sale_items",
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("saleId")]
)
data class SaleItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val saleId: Long,
    val productId: Long,
    val productName: String,
    val unit: SaleUnit,
    val quantity: Double,
    val unitPrice: Double,
    val unitCost: Double
)

val SaleItem.subtotal: Double get() = (unitPrice * quantity).round2()

data class SaleWithItems(
    @Embedded val sale: Sale,
    @Relation(parentColumn = "id", entityColumn = "saleId")
    val items: List<SaleItem>
)

@Entity(tableName = "orders")
data class PurchaseOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val supplierId: Long?,
    val supplierName: String,
    val createdAt: Long,
    val receivedAt: Long? = null,
    val status: OrderStatus = OrderStatus.PENDING,
    val total: Double
)

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseOrder::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId")]
)
data class PurchaseOrderItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val unit: SaleUnit,
    val quantity: Double,
    val unitCost: Double
)

val PurchaseOrderItem.subtotal: Double get() = (unitCost * quantity).round2()

data class OrderWithItems(
    @Embedded val order: PurchaseOrder,
    @Relation(parentColumn = "id", entityColumn = "orderId")
    val items: List<PurchaseOrderItem>
)

/** Línea del carrito de venta (solo en memoria). */
data class CartLine(val product: Product, val quantity: Double) {
    val subtotal: Double get() = (product.salePrice * quantity).round2()
}

/** Línea de un pedido en borrador (solo en memoria). */
data class OrderLine(val product: Product, val quantity: Double) {
    val subtotal: Double get() = (product.costPrice * quantity).round2()
}

fun Double.round2(): Double = Math.round(this * 100.0) / 100.0
