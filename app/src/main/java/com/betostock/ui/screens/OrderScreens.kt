package com.betostock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.data.OrderStatus
import com.betostock.data.OrderWithItems
import com.betostock.data.Product
import com.betostock.data.Supplier
import com.betostock.data.needsRestock
import com.betostock.data.round2
import com.betostock.data.subtotal
import com.betostock.ui.components.Banner
import com.betostock.ui.components.BetoCard
import com.betostock.ui.components.BetoChip
import com.betostock.ui.components.BetoHeader
import com.betostock.ui.components.BigButton
import com.betostock.ui.components.BtnStyle
import com.betostock.ui.components.ConfirmDialog
import com.betostock.ui.components.EmptyState
import com.betostock.ui.components.QuantityStepper
import com.betostock.ui.components.SearchField
import com.betostock.ui.components.SectionTitle
import com.betostock.ui.components.StatusBadge
import com.betostock.ui.components.Tone
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.theme.PriceDisplay
import com.betostock.ui.theme.PriceItem
import com.betostock.ui.viewmodel.OrdersViewModel
import com.betostock.ui.viewmodel.SuppliersViewModel
import com.betostock.util.STORE_NAME
import com.betostock.util.dateTimeText
import com.betostock.util.money
import com.betostock.util.openWhatsApp
import com.betostock.util.orderMessageFromDraft
import com.betostock.util.orderMessageFromItems
import com.betostock.util.qty
import com.betostock.util.visitDaysText
import com.betostock.util.visitsToday
import kotlin.math.ceil
import kotlin.math.max

@Composable
private fun OrderStatusBadge(status: OrderStatus) {
    when (status) {
        OrderStatus.PENDING -> StatusBadge("Pendiente", Tone.Warn, Icons.Filled.Schedule)
        OrderStatus.RECEIVED -> StatusBadge("Recibido", Tone.Ok, Icons.Filled.CheckCircle)
    }
}

// ===========================================================================
// Pedidos: historial (RF-08)
// ===========================================================================

@Composable
fun OrdersScreen(
    vm: OrdersViewModel,
    onNewOrder: () -> Unit,
    onSuppliers: () -> Unit,
    onOpenOrder: (Long) -> Unit
) {
    val orders by vm.orders.collectAsStateWithLifecycle()
    val filter by vm.statusFilter.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Pedidos", subtitle = STORE_NAME)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BigButton("Hacer nuevo pedido", onNewOrder, icon = Icons.Filled.Add) }
            item { BigButton("Mis proveedores", onSuppliers, icon = Icons.Filled.LocalShipping, style = BtnStyle.Secondary) }
            item {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BetoChip("Todos", filter == null, { vm.statusFilter.value = null })
                    BetoChip("Pendientes", filter == OrderStatus.PENDING, { vm.statusFilter.value = OrderStatus.PENDING })
                    BetoChip("Recibidos", filter == OrderStatus.RECEIVED, { vm.statusFilter.value = OrderStatus.RECEIVED })
                }
            }
            if (orders.isEmpty()) {
                item {
                    EmptyState(
                        "Todavía no hay pedidos",
                        "Cuando hagas un pedido a un proveedor, aparecerá aquí."
                    )
                }
            }
            items(orders, key = { it.order.id }) { data ->
                OrderCard(data, onClick = { onOpenOrder(data.order.id) })
            }
        }
    }
}

@Composable
private fun OrderCard(data: OrderWithItems, onClick: () -> Unit) {
    val order = data.order
    BetoCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(order.supplierName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(order.createdAt.dateTimeText(), style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
            }
            OrderStatusBadge(order.status)
        }
        Spacer(Modifier.height(8.dp))
        val summary = data.items.take(3).joinToString(", ") { "${it.productName} (${it.quantity.qty()})" } +
            if (data.items.size > 3) " y más…" else ""
        Text(summary, style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextPrimary)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Total", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(order.total.money(), style = PriceItem, color = BetoColors.Primary)
        }
    }
}

// ===========================================================================
// Nuevo pedido, paso 1: proveedor
// ===========================================================================

@Composable
fun SupplierPickerScreen(
    vm: OrdersViewModel,
    suppliersVm: SuppliersViewModel,
    onBack: () -> Unit,
    onPicked: (Supplier) -> Unit,
    onAddSupplier: () -> Unit
) {
    val suppliers by vm.suppliers.collectAsStateWithLifecycle()
    val lowStock by suppliersVm.lowStockBySupplier.collectAsStateWithLifecycle()
    val sorted = suppliers.sortedByDescending { it.visitsToday() }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Nuevo pedido", subtitle = "Paso 1 de 2", onBack = onBack)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("¿A quién le vas a hacer el pedido?", style = MaterialTheme.typography.headlineSmall)
                Text("Toca a tu repartidor de confianza para armar la lista.", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
            }
            if (sorted.isEmpty()) {
                item {
                    EmptyState("Aún no tienes proveedores", "Registra a tu primer proveedor para poder hacerle pedidos.")
                }
            }
            items(sorted, key = { it.id }) { supplier ->
                BetoCard {
                    Text(supplier.name, style = MaterialTheme.typography.titleLarge)
                    if (supplier.contactName.isNotBlank()) {
                        Text("Repartidor: ${supplier.contactName}", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                    }
                    if (supplier.visitsToday()) {
                        Spacer(Modifier.height(8.dp))
                        StatusBadge("¡Hoy pasa el camión!", Tone.Info, Icons.Filled.LocalShipping)
                    }
                    val low = lowStock[supplier.id] ?: 0
                    if (low > 0) {
                        Spacer(Modifier.height(8.dp))
                        StatusBadge(
                            if (low == 1) "1 producto por agotarse" else "$low productos por agotarse",
                            Tone.Warn,
                            Icons.Filled.Warning
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    BigButton("Elegir este proveedor", { onPicked(supplier) }, icon = Icons.Filled.Check, compact = true)
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            HorizontalDivider(color = BetoColors.Border)
            Column(Modifier.padding(16.dp)) {
                BigButton("Registrar proveedor nuevo", onAddSupplier, icon = Icons.Filled.Add, style = BtnStyle.Secondary)
            }
        }
    }
}

// ===========================================================================
// Nuevo pedido, paso 2: productos y cantidades (RF-06)
// ===========================================================================

private fun suggestedQuantity(p: Product): Double =
    max(1.0, ceil(max(p.minStock * 2 - p.stock, 1.0)))

@Composable
fun OrderProductsScreen(vm: OrdersViewModel, onBack: () -> Unit, onReview: () -> Unit) {
    val supplierState by vm.draftSupplier.collectAsStateWithLifecycle()
    val all by vm.products.collectAsStateWithLifecycle()
    val query by vm.productQuery.collectAsStateWithLifecycle()
    val quantities by vm.draftQuantities.collectAsStateWithLifecycle()
    val lines by vm.draftLines.collectAsStateWithLifecycle()

    val supplier = supplierState
    if (supplier == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }

    // Productos de este proveedor y los que no tienen proveedor asignado.
    val candidates = all.filter {
        (it.supplierId == supplier.id || it.supplierId == null) &&
            it.name.contains(query.trim(), ignoreCase = true)
    }
    val suggested = candidates.filter { it.needsRestock }
    val others = candidates.filterNot { it.needsRestock }
    val total = lines.sumOf { it.subtotal }.round2()

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = supplier.name, subtitle = "Paso 2 de 2 • Elegir productos", onBack = onBack)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { SearchField(query, { vm.productQuery.value = it }, "Buscar producto") }
            if (suggested.isNotEmpty()) {
                item {
                    Banner(
                        tone = Tone.Warn,
                        icon = Icons.Filled.Warning,
                        title = "Sugeridos por poco stock",
                        message = "Estos se te van a terminar pronto. Conviene surtirlos."
                    )
                }
                items(suggested, key = { "s${it.id}" }) { p ->
                    OrderProductCard(p, quantities[p.id], suggestedQuantity(p)) { vm.setDraftQuantity(p, it) }
                }
            }
            if (others.isNotEmpty()) {
                item { SectionTitle("Otros productos") }
                items(others, key = { "o${it.id}" }) { p ->
                    OrderProductCard(p, quantities[p.id], 1.0) { vm.setDraftQuantity(p, it) }
                }
            }
            if (candidates.isEmpty()) {
                item {
                    EmptyState(
                        "No hay productos para pedir",
                        "Registra productos en la pestaña «Productos» y asígnales este proveedor."
                    )
                }
            }
        }
        if (lines.isNotEmpty()) {
            Column(Modifier.fillMaxWidth().background(Color.White)) {
                HorizontalDivider(color = BetoColors.Border)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total del pedido", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(total.money(), style = PriceDisplay, color = BetoColors.Primary)
                    }
                    BigButton("Ver pedido (${lines.size})", onReview, icon = Icons.Filled.Check)
                }
            }
        }
    }
}

@Composable
private fun OrderProductCard(
    product: Product,
    quantity: Double?,
    suggested: Double,
    onQuantity: (Double) -> Unit
) {
    BetoCard {
        Text(product.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(6.dp))
        StockBadge(product)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Te cuesta por ${product.unit.word}", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                Text(product.costPrice.money(), style = PriceItem)
            }
            if (quantity == null) {
                BigButton(
                    "Pedir ${suggested.qty()}",
                    { onQuantity(suggested) },
                    Modifier.width(150.dp),
                    icon = Icons.Filled.Add,
                    compact = true
                )
            } else {
                QuantityStepper(
                    value = quantity,
                    onValueChange = onQuantity,
                    step = product.unit.step,
                    unit = product.unit.short
                )
            }
        }
    }
}

// ===========================================================================
// Resumen del pedido y envío por WhatsApp (RF-07)
// ===========================================================================

@Composable
fun OrderReviewScreen(vm: OrdersViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val supplierState by vm.draftSupplier.collectAsStateWithLifecycle()
    val lines by vm.draftLines.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val supplier = supplierState
    if (supplier == null) {
        LaunchedEffect(Unit) { onBack() }
        return
    }
    val total = lines.sumOf { it.subtotal }.round2()
    val message = remember(lines, supplier) { orderMessageFromDraft(supplier, lines) }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Resumen del pedido", subtitle = supplier.name, onBack = onBack)
        if (lines.isEmpty()) {
            Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.Center) {
                EmptyState("El pedido está vacío", "Regresa y elige los productos que vas a pedir.")
                BigButton("Elegir productos", onBack)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lines, key = { it.product.id }) { line ->
                    BetoCard {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                line.product.name,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { vm.setDraftQuantity(line.product, 0.0) }, modifier = Modifier.size(52.dp)) {
                                Icon(Icons.Filled.Delete, "Quitar del pedido", tint = BetoColors.Error, modifier = Modifier.size(28.dp))
                            }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            QuantityStepper(
                                value = line.quantity,
                                onValueChange = { vm.setDraftQuantity(line.product, it) },
                                step = line.product.unit.step,
                                unit = line.product.unit.short,
                                modifier = Modifier.weight(1f)
                            )
                            Text(line.subtotal.money(), style = PriceItem)
                        }
                    }
                }
                item {
                    BetoCard(background = BetoColors.SkyLight, borderColor = BetoColors.Sky) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Total del pedido", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(total.money(), style = PriceDisplay, color = BetoColors.Primary)
                        }
                        if (supplier.phone.isBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Este proveedor no tiene teléfono guardado; WhatsApp te dejará elegir el contacto.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BetoColors.OnWarningContainer
                            )
                        }
                    }
                }
            }
            Column(Modifier.fillMaxWidth().background(Color.White)) {
                HorizontalDivider(color = BetoColors.Border)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigButton(
                        "Enviar por WhatsApp",
                        {
                            vm.saveDraft {
                                openWhatsApp(context, supplier.phone, message)
                                onSaved()
                            }
                        },
                        icon = Icons.AutoMirrored.Filled.Send,
                        style = BtnStyle.Success
                    )
                    BigButton("Guardar sin enviar", { vm.saveDraft { onSaved() } }, style = BtnStyle.Secondary, compact = true)
                }
            }
        }
    }
}

// ===========================================================================
// Detalle del pedido y recepción de mercancía (RF-09)
// ===========================================================================

@Composable
fun OrderDetailScreen(orderId: Long, vm: OrdersViewModel, onBack: () -> Unit, onReceived: () -> Unit) {
    val flow = remember(orderId) { vm.order(orderId) }
    val data by flow.collectAsStateWithLifecycle(initialValue = null)
    val suppliers by vm.suppliers.collectAsStateWithLifecycle()
    val checked = remember { mutableStateMapOf<Long, Boolean>() }
    var confirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Pedido #$orderId", subtitle = STORE_NAME, onBack = onBack)
        val current = data
        if (current == null) {
            EmptyState("Cargando…", "Un momento por favor.")
            return@Column
        }
        val order = current.order
        val pending = order.status == OrderStatus.PENDING
        val allChecked = current.items.all { checked[it.id] == true }
        val supplier = suppliers.firstOrNull { it.id == order.supplierId }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BetoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(order.supplierName, style = MaterialTheme.typography.titleLarge)
                        Text(order.createdAt.dateTimeText(), style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                    }
                    OrderStatusBadge(order.status)
                }
                Spacer(Modifier.height(12.dp))
                Text("Total del pedido", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                Text(order.total.money(), style = PriceDisplay, color = BetoColors.Primary)
            }

            if (!pending) {
                Banner(
                    tone = Tone.Ok,
                    icon = Icons.Filled.CheckCircle,
                    title = "Mercancía recibida",
                    message = (order.receivedAt?.let { "El ${it.dateTimeText()} se sumó a tu inventario." }
                        ?: "Ya se sumó a tu inventario.") +
                        " Se descontaron ${order.total.money()} del dinero del día."
                )
            }

            SectionTitle(if (pending) "Revisión de productos" else "Productos del pedido")
            if (pending) {
                Text(
                    "Toca cada casilla conforme cuentes lo que bajó el repartidor.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BetoColors.TextSecondary
                )
            }
            current.items.forEach { item ->
                BetoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (pending) {
                            Checkbox(
                                checked = checked[item.id] == true,
                                onCheckedChange = { checked[item.id] = it },
                                modifier = Modifier.size(56.dp),
                                colors = CheckboxDefaults.colors(checkedColor = BetoColors.Primary)
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Column(Modifier.weight(1f)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "${item.quantity.qty()} ${item.unit.short} × ${item.unitCost.money()}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BetoColors.TextSecondary
                            )
                        }
                        Text(item.subtotal.money(), style = PriceItem)
                    }
                }
            }

            if (pending) {
                BetoCard(background = BetoColors.SkyLight, borderColor = BetoColors.Sky) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, null, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Se sumará a tu inventario", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    current.items.forEach { item ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                            Text(item.productName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Text("+${item.quantity.qty()} ${item.unit.short}", style = MaterialTheme.typography.labelLarge, color = BetoColors.Primary)
                        }
                    }
                }
                Banner(
                    tone = Tone.Info,
                    icon = Icons.Filled.Payments,
                    title = "Se descontará del dinero del día",
                    message = "Al confirmar, ${order.total.money()} se resta del dinero de hoy porque ya le pagas al proveedor."
                )
                BigButton("Ya llegó: confirmar recibido", { confirm = true }, icon = Icons.Filled.Check, style = BtnStyle.Success)
                if (supplier != null) {
                    BigButton(
                        "Enviar de nuevo por WhatsApp",
                        { openWhatsApp(context, supplier.phone, orderMessageFromItems(supplier, current.items)) },
                        icon = Icons.AutoMirrored.Filled.Send,
                        style = BtnStyle.Secondary,
                        compact = true
                    )
                }
            }
        }

        if (confirm) {
            ConfirmDialog(
                title = "¿Ya llegó el pedido?",
                message = if (allChecked) {
                    "Se sumarán estos productos a tu inventario y se descontarán ${order.total.money()} del dinero del día."
                } else {
                    "Todavía hay productos sin marcar. ¿Quieres confirmar de todos modos? Se sumará todo el pedido a tu inventario y se descontarán ${order.total.money()} del dinero del día."
                },
                confirmText = "Sí, actualizar inventario",
                onConfirm = {
                    confirm = false
                    vm.receive(orderId, onDone = onReceived)
                },
                onDismiss = { confirm = false }
            )
        }
    }
}

// ===========================================================================
// Mercancía recibida
// ===========================================================================

@Composable
fun OrderDoneScreen(orderId: Long, vm: OrdersViewModel, onInventory: () -> Unit, onHome: () -> Unit) {
    val flow = remember(orderId) { vm.order(orderId) }
    val data by flow.collectAsStateWithLifecycle(initialValue = null)
    val products by vm.products.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Pedido #$orderId", subtitle = STORE_NAME)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.foundation.layout.Box(
                Modifier.size(112.dp).background(BetoColors.Primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(72.dp))
            }
            Text(
                "¡Mercancía recibida!",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            val current = data
            if (current != null) {
                Text(
                    "Pedido a ${current.order.supplierName} • ${current.order.total.money()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BetoColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
                BetoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, null, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Inventario actualizado", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Se descontaron ${current.order.total.money()} del dinero del día.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = BetoColors.Error
                    )
                    Spacer(Modifier.height(10.dp))
                    current.items.forEach { item ->
                        val now = products.firstOrNull { it.id == item.productId }
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(item.productName, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                                if (now != null) {
                                    Text(
                                        "Ahora tienes: ${now.stock.qty()} ${now.unit.short}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BetoColors.TextSecondary
                                    )
                                }
                            }
                            StatusBadge("+${item.quantity.qty()} ${item.unit.short}", Tone.Info)
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            HorizontalDivider(color = BetoColors.Border)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BigButton("Ver inventario actualizado", onInventory, icon = Icons.Filled.Inventory2)
                BigButton("Volver al inicio", onHome, icon = Icons.Filled.Home, style = BtnStyle.Secondary)
            }
        }
    }
}
