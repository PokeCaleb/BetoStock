package com.betostock.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.betostock.data.CartLine
import com.betostock.data.Category
import com.betostock.data.OrderLine
import com.betostock.data.OrderStatus
import com.betostock.data.OrderWithItems
import com.betostock.data.Product
import com.betostock.data.PurchaseOrder
import com.betostock.data.SaleWithItems
import com.betostock.data.StoreRepository
import com.betostock.data.Supplier
import com.betostock.data.needsRestock
import com.betostock.util.endMillis
import com.betostock.util.startMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

private const val STOP_TIMEOUT = 5_000L

/** Una sola fábrica para todos los ViewModels de la app. */
fun betoViewModelFactory(repo: StoreRepository): ViewModelProvider.Factory = viewModelFactory {
    initializer<HomeViewModel> { HomeViewModel(repo) }
    initializer<SellViewModel> { SellViewModel(repo) }
    initializer<ProductsViewModel> { ProductsViewModel(repo) }
    initializer<OrdersViewModel> { OrdersViewModel(repo) }
    initializer<SuppliersViewModel> { SuppliersViewModel(repo) }
    initializer<HistoryViewModel> { HistoryViewModel(repo) }
}

// ===========================================================================
// Inicio
// ===========================================================================

data class HomeState(
    val salesTotal: Double = 0.0,
    val salesCount: Int = 0,
    val profit: Double = 0.0,
    /** Lo pagado a proveedores por pedidos que llegaron hoy. */
    val supplierPayments: Double = 0.0,
    val lowStock: List<Product> = emptyList(),
    val recentSales: List<SaleWithItems> = emptyList()
) {
    /** Dinero del día = ventas cobradas - pedidos de proveedor que llegaron hoy. */
    val cash: Double get() = salesTotal - supplierPayments
}

class HomeViewModel(repo: StoreRepository) : ViewModel() {
    private val day = MutableStateFlow(LocalDate.now())

    /** Se llama al abrir la pantalla por si la app estuvo abierta pasando la medianoche. */
    fun refreshDay() {
        day.value = LocalDate.now()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeState> = day.flatMapLatest { d ->
        combine(
            repo.salesBetween(d.startMillis(), d.endMillis()),
            repo.products,
            repo.receivedOrdersBetween(d.startMillis(), d.endMillis())
        ) { sales, products, received ->
            HomeState(
                salesTotal = sales.sumOf { it.sale.total },
                salesCount = sales.size,
                profit = sales.sumOf { it.sale.profit },
                supplierPayments = received.sumOf { it.total },
                lowStock = products.filter { it.needsRestock },
                recentSales = sales.take(3)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), HomeState())
}

// ===========================================================================
// Ventas (RF-03, RF-04)
// ===========================================================================

class SellViewModel(private val repo: StoreRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val category = MutableStateFlow<Category?>(null)

    val catalog: StateFlow<List<Product>> =
        combine(repo.products, query, category) { all, q, c ->
            all.filter { p ->
                (c == null || p.category == c) && p.name.contains(q.trim(), ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    /** productId -> cantidad en el carrito (conserva el orden en que se agregaron). */
    private val cart = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val cartQuantities: StateFlow<Map<Long, Double>> = cart

    val lines: StateFlow<List<CartLine>> =
        combine(repo.products, cart) { products, items ->
            items.entries.mapNotNull { (id, qty) ->
                products.firstOrNull { it.id == id }?.let { CartLine(it, qty) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    /** Fija la cantidad (nunca más que el stock disponible). 0 o menos la quita del carrito. */
    fun setQuantity(product: Product, quantity: Double) {
        val q = minOf(quantity, product.stock)
        cart.update { if (q <= 0.0) it - product.id else it + (product.id to q) }
    }

    fun add(product: Product) {
        setQuantity(product, (cart.value[product.id] ?: 0.0) + product.unit.step)
    }

    fun remove(product: Product) {
        cart.update { it - product.id }
    }

    fun sale(id: Long): Flow<SaleWithItems?> = repo.sale(id)

    fun checkout(paid: Double, onSuccess: (Long) -> Unit, onError: (String) -> Unit) {
        val current = lines.value
        if (current.isEmpty()) return
        viewModelScope.launch {
            try {
                val id = repo.registerSale(current, paid)
                onSuccess(id)
                cart.value = emptyMap()
            } catch (e: Exception) {
                onError(e.message ?: "No se pudo guardar la venta.")
            }
        }
    }
}

// ===========================================================================
// Productos (RF-01, RF-02)
// ===========================================================================

class ProductsViewModel(private val repo: StoreRepository) : ViewModel() {
    val query = MutableStateFlow("")
    val category = MutableStateFlow<Category?>(null)
    val lowOnly = MutableStateFlow(false)

    val allProducts: StateFlow<List<Product>> =
        repo.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val visible: StateFlow<List<Product>> =
        combine(repo.products, query, category, lowOnly) { all, q, c, low ->
            all.filter { p ->
                (c == null || p.category == c) &&
                    (!low || p.needsRestock) &&
                    p.name.contains(q.trim(), ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val suppliers: StateFlow<List<Supplier>> =
        repo.suppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    suspend fun get(id: Long): Product? = repo.product(id)

    fun save(product: Product, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.saveProduct(product)
            onDone()
        }
    }

    fun delete(product: Product, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteProduct(product)
            onDone()
        }
    }
}

// ===========================================================================
// Pedidos a proveedores (RF-06, RF-07, RF-08, RF-09)
// ===========================================================================

class OrdersViewModel(private val repo: StoreRepository) : ViewModel() {
    val products: StateFlow<List<Product>> =
        repo.products.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val suppliers: StateFlow<List<Supplier>> =
        repo.suppliers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    val statusFilter = MutableStateFlow<OrderStatus?>(null)

    val orders: StateFlow<List<OrderWithItems>> =
        combine(repo.orders, statusFilter) { all, status ->
            if (status == null) all else all.filter { it.order.status == status }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    fun order(id: Long): Flow<OrderWithItems?> = repo.order(id)

    // ----- Borrador de pedido -----

    val draftSupplier = MutableStateFlow<Supplier?>(null)
    val productQuery = MutableStateFlow("")
    private val draft = MutableStateFlow<Map<Long, Double>>(emptyMap())
    val draftQuantities: StateFlow<Map<Long, Double>> = draft

    val draftLines: StateFlow<List<OrderLine>> =
        combine(repo.products, draft) { products, items ->
            items.entries.mapNotNull { (id, qty) ->
                products.firstOrNull { it.id == id }?.let { OrderLine(it, qty) }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    fun startDraft(supplier: Supplier) {
        draftSupplier.value = supplier
        draft.value = emptyMap()
        productQuery.value = ""
    }

    fun setDraftQuantity(product: Product, quantity: Double) {
        draft.update { if (quantity <= 0.0) it - product.id else it + (product.id to quantity) }
    }

    /** Guarda el pedido como "pendiente". Devuelve el id por onSaved. */
    fun saveDraft(onSaved: (Long) -> Unit) {
        val supplier = draftSupplier.value ?: return
        val lines = draftLines.value
        if (lines.isEmpty()) return
        viewModelScope.launch {
            val id = repo.createOrder(supplier, lines)
            draft.value = emptyMap()
            onSaved(id)
        }
    }

    /** Al llegar la mercancía: suma todo al inventario de una vez (RF-09). */
    fun receive(orderId: Long, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.receiveOrder(orderId)
            onDone()
        }
    }
}

// ===========================================================================
// Proveedores
// ===========================================================================

class SuppliersViewModel(private val repo: StoreRepository) : ViewModel() {
    val query = MutableStateFlow("")

    val suppliers: StateFlow<List<Supplier>> =
        combine(repo.suppliers, query) { all, q ->
            all.filter {
                it.name.contains(q.trim(), ignoreCase = true) ||
                    it.contactName.contains(q.trim(), ignoreCase = true)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyList())

    /** supplierId -> cuántos de sus productos están por agotarse. */
    val lowStockBySupplier: StateFlow<Map<Long, Int>> =
        repo.products.let { products ->
            combine(products, repo.suppliers) { list, _ ->
                list.filter { it.needsRestock && it.supplierId != null }
                    .groupingBy { it.supplierId!! }
                    .eachCount()
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), emptyMap())

    suspend fun get(id: Long): Supplier? = repo.supplier(id)

    fun save(supplier: Supplier, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.saveSupplier(supplier)
            onDone()
        }
    }

    fun delete(supplier: Supplier, onDone: () -> Unit) {
        viewModelScope.launch {
            repo.deleteSupplier(supplier)
            onDone()
        }
    }
}

// ===========================================================================
// Historial / ventas del día (RF-05)
// ===========================================================================

data class DayReport(
    val date: LocalDate = LocalDate.now(),
    val sales: List<SaleWithItems> = emptyList(),
    /** Pedidos de proveedor que llegaron ese día. */
    val receivedOrders: List<PurchaseOrder> = emptyList()
) {
    val total: Double get() = sales.sumOf { it.sale.total }
    val supplierPayments: Double get() = receivedOrders.sumOf { it.total }
    /** Dinero del día = ventas - lo que se pagó a proveedores al recibir su mercancía. */
    val cash: Double get() = total - supplierPayments
    val profit: Double get() = sales.sumOf { it.sale.profit }
    val marginPercent: Double get() = if (total > 0.0) profit / total * 100.0 else 0.0
}

class HistoryViewModel(private val repo: StoreRepository) : ViewModel() {
    private val date = MutableStateFlow(LocalDate.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val report: StateFlow<DayReport> = date.flatMapLatest { d ->
        combine(
            repo.salesBetween(d.startMillis(), d.endMillis()),
            repo.receivedOrdersBetween(d.startMillis(), d.endMillis())
        ) { sales, received -> DayReport(d, sales, received) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DayReport())

    fun goToToday() {
        date.value = LocalDate.now()
    }

    fun previousDay() {
        date.update { it.minusDays(1) }
    }

    fun nextDay() {
        date.update { if (it.isBefore(LocalDate.now())) it.plusDays(1) else it }
    }
}
