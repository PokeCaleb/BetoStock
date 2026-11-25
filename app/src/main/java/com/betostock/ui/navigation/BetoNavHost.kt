package com.betostock.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.betostock.ui.screens.CartScreen
import com.betostock.ui.screens.HistoryScreen
import com.betostock.ui.screens.HomeScreen
import com.betostock.ui.screens.OrderDetailScreen
import com.betostock.ui.screens.OrderDoneScreen
import com.betostock.ui.screens.OrderProductsScreen
import com.betostock.ui.screens.OrderReviewScreen
import com.betostock.ui.screens.OrdersScreen
import com.betostock.ui.screens.ProductEditScreen
import com.betostock.ui.screens.ProductsScreen
import com.betostock.ui.screens.SaleDoneScreen
import com.betostock.ui.screens.SellScreen
import com.betostock.ui.screens.SupplierEditScreen
import com.betostock.ui.screens.SupplierPickerScreen
import com.betostock.ui.screens.SuppliersScreen
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.viewmodel.HistoryViewModel
import com.betostock.ui.viewmodel.HomeViewModel
import com.betostock.ui.viewmodel.OrdersViewModel
import com.betostock.ui.viewmodel.ProductsViewModel
import com.betostock.ui.viewmodel.SellViewModel
import com.betostock.ui.viewmodel.SuppliersViewModel

object Routes {
    const val HOME = "home"
    const val SELL = "sell"
    const val CART = "cart"
    const val SALE_DONE = "sale_done/{id}"
    const val PRODUCTS = "products"
    const val PRODUCT_EDIT = "product_edit/{id}"
    const val ORDERS = "orders"
    const val ORDER_SUPPLIER = "order_supplier"
    const val ORDER_PRODUCTS = "order_products"
    const val ORDER_REVIEW = "order_review"
    const val ORDER_DETAIL = "order_detail/{id}"
    const val ORDER_DONE = "order_done/{id}"
    const val SUPPLIERS = "suppliers"
    const val SUPPLIER_EDIT = "supplier_edit/{id}"
    const val HISTORY = "history"

    fun saleDone(id: Long) = "sale_done/$id"
    fun productEdit(id: Long) = "product_edit/$id"
    fun orderDetail(id: Long) = "order_detail/$id"
    fun orderDone(id: Long) = "order_done/$id"
    fun supplierEdit(id: Long) = "supplier_edit/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.HOME, "Inicio", Icons.Filled.Home),
    Tab(Routes.SELL, "Vender", Icons.Filled.AddShoppingCart),
    Tab(Routes.PRODUCTS, "Productos", Icons.Filled.Inventory2),
    Tab(Routes.ORDERS, "Pedidos", Icons.Filled.LocalShipping)
)

/** Cambia de pestaña limpiando las pantallas intermedias; "atrás" regresa a Inicio. */
private fun NavController.goTab(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) { inclusive = false }
        launchSingleTop = true
    }
}

private val idArg = listOf(navArgument("id") { type = NavType.LongType })

@Composable
fun BetoApp(factory: ViewModelProvider.Factory) {
    val nav = rememberNavController()

    // ViewModels compartidos entre pantallas (el carrito y el borrador de pedido viven aquí).
    val homeVm: HomeViewModel = viewModel(factory = factory)
    val sellVm: SellViewModel = viewModel(factory = factory)
    val productsVm: ProductsViewModel = viewModel(factory = factory)
    val ordersVm: OrdersViewModel = viewModel(factory = factory)
    val suppliersVm: SuppliersViewModel = viewModel(factory = factory)
    val historyVm: HistoryViewModel = viewModel(factory = factory)

    val backStackEntry by nav.currentBackStackEntryAsState()
    val route = backStackEntry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        containerColor = BetoColors.Background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Color.White) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                if (tab.route == Routes.PRODUCTS) productsVm.lowOnly.value = false
                                nav.goTab(tab.route)
                            },
                            icon = { Icon(tab.icon, contentDescription = null, modifier = Modifier.size(28.dp)) },
                            label = { Text(tab.label, style = androidx.compose.material3.MaterialTheme.typography.labelMedium) },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BetoColors.Primary,
                                selectedTextColor = BetoColors.Primary,
                                indicatorColor = BetoColors.SkyLight,
                                unselectedIconColor = BetoColors.TextSecondary,
                                unselectedTextColor = BetoColors.TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = nav,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    vm = homeVm,
                    onNewSale = { nav.goTab(Routes.SELL) },
                    onNewOrder = { nav.navigate(Routes.ORDER_SUPPLIER) },
                    onHistory = { nav.navigate(Routes.HISTORY) },
                    onLowStock = {
                        productsVm.query.value = ""
                        productsVm.category.value = null
                        productsVm.lowOnly.value = true
                        nav.goTab(Routes.PRODUCTS)
                    }
                )
            }

            // ----- Ventas -----
            composable(Routes.SELL) {
                SellScreen(vm = sellVm, onOpenCart = { nav.navigate(Routes.CART) })
            }
            composable(Routes.CART) {
                CartScreen(
                    vm = sellVm,
                    onBack = { nav.popBackStack() },
                    onSold = { id ->
                        nav.navigate(Routes.saleDone(id)) {
                            popUpTo(Routes.CART) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.SALE_DONE, arguments = idArg) { entry ->
                SaleDoneScreen(
                    saleId = entry.arguments?.getLong("id") ?: 0L,
                    vm = sellVm,
                    onAnotherSale = { nav.goTab(Routes.SELL) },
                    onHome = { nav.goTab(Routes.HOME) }
                )
            }
            composable(Routes.HISTORY) {
                HistoryScreen(vm = historyVm, onBack = { nav.popBackStack() })
            }

            // ----- Productos -----
            composable(Routes.PRODUCTS) {
                ProductsScreen(
                    vm = productsVm,
                    onEdit = { id -> nav.navigate(Routes.productEdit(id)) },
                    onAdd = { nav.navigate(Routes.productEdit(0L)) }
                )
            }
            composable(Routes.PRODUCT_EDIT, arguments = idArg) { entry ->
                ProductEditScreen(
                    productId = entry.arguments?.getLong("id") ?: 0L,
                    vm = productsVm,
                    onBack = { nav.popBackStack() }
                )
            }

            // ----- Pedidos -----
            composable(Routes.ORDERS) {
                OrdersScreen(
                    vm = ordersVm,
                    onNewOrder = { nav.navigate(Routes.ORDER_SUPPLIER) },
                    onSuppliers = { nav.navigate(Routes.SUPPLIERS) },
                    onOpenOrder = { id -> nav.navigate(Routes.orderDetail(id)) }
                )
            }
            composable(Routes.ORDER_SUPPLIER) {
                SupplierPickerScreen(
                    vm = ordersVm,
                    suppliersVm = suppliersVm,
                    onBack = { nav.popBackStack() },
                    onPicked = { supplier ->
                        ordersVm.startDraft(supplier)
                        nav.navigate(Routes.ORDER_PRODUCTS)
                    },
                    onAddSupplier = { nav.navigate(Routes.supplierEdit(0L)) }
                )
            }
            composable(Routes.ORDER_PRODUCTS) {
                OrderProductsScreen(
                    vm = ordersVm,
                    onBack = { nav.popBackStack() },
                    onReview = { nav.navigate(Routes.ORDER_REVIEW) }
                )
            }
            composable(Routes.ORDER_REVIEW) {
                OrderReviewScreen(
                    vm = ordersVm,
                    onBack = { nav.popBackStack() },
                    onSaved = { nav.goTab(Routes.ORDERS) }
                )
            }
            composable(Routes.ORDER_DETAIL, arguments = idArg) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                OrderDetailScreen(
                    orderId = id,
                    vm = ordersVm,
                    onBack = { nav.popBackStack() },
                    onReceived = {
                        nav.navigate(Routes.orderDone(id)) {
                            popUpTo(Routes.ORDER_DETAIL) { inclusive = true }
                        }
                    }
                )
            }
            composable(Routes.ORDER_DONE, arguments = idArg) { entry ->
                OrderDoneScreen(
                    orderId = entry.arguments?.getLong("id") ?: 0L,
                    vm = ordersVm,
                    onInventory = {
                        productsVm.lowOnly.value = false
                        nav.goTab(Routes.PRODUCTS)
                    },
                    onHome = { nav.goTab(Routes.HOME) }
                )
            }

            // ----- Proveedores -----
            composable(Routes.SUPPLIERS) {
                SuppliersScreen(
                    vm = suppliersVm,
                    onBack = { nav.popBackStack() },
                    onAdd = { nav.navigate(Routes.supplierEdit(0L)) },
                    onEdit = { id -> nav.navigate(Routes.supplierEdit(id)) }
                )
            }
            composable(Routes.SUPPLIER_EDIT, arguments = idArg) { entry ->
                SupplierEditScreen(
                    supplierId = entry.arguments?.getLong("id") ?: 0L,
                    vm = suppliersVm,
                    onBack = { nav.popBackStack() }
                )
            }
        }
    }
}
