package com.betostock.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.data.CartLine
import com.betostock.data.Product
import com.betostock.data.isOut
import com.betostock.data.round2
import com.betostock.ui.components.BetoCard
import com.betostock.ui.components.BetoChip
import com.betostock.ui.components.BetoHeader
import com.betostock.ui.components.BigButton
import com.betostock.ui.components.BtnStyle
import com.betostock.ui.components.EmptyState
import com.betostock.ui.components.LabeledField
import com.betostock.ui.components.QuantityStepper
import com.betostock.ui.components.SearchField
import com.betostock.ui.components.SectionTitle
import com.betostock.ui.components.SquareIconButton
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.theme.PriceDisplay
import com.betostock.ui.theme.PriceItem
import com.betostock.ui.viewmodel.SellViewModel
import com.betostock.util.STORE_NAME
import com.betostock.util.money
import com.betostock.util.openWhatsApp
import com.betostock.util.saleTicketMessage
import com.betostock.util.qty
import com.betostock.util.timeText
import com.betostock.util.toAmount

// ===========================================================================
// Vender: elegir productos
// ===========================================================================

@Composable
fun SellScreen(vm: SellViewModel, onOpenCart: () -> Unit) {
    val products by vm.catalog.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val category by vm.category.collectAsStateWithLifecycle()
    val quantities by vm.cartQuantities.collectAsStateWithLifecycle()
    val lines by vm.lines.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Vender", subtitle = STORE_NAME)
        Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            SearchField(query, { vm.query.value = it }, "Buscar producto")
            Spacer(Modifier.height(12.dp))
            CategoryChips(category, { vm.category.value = it })
        }
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (products.isEmpty()) {
                item {
                    EmptyState(
                        "No encontré productos",
                        "Revisa lo que escribiste o registra el producto en la pestaña «Productos»."
                    )
                }
            }
            items(products, key = { it.id }) { product ->
                SaleProductCard(
                    product = product,
                    inCart = quantities[product.id],
                    onAdd = { vm.add(product) },
                    onQuantity = { vm.setQuantity(product, it) }
                )
            }
        }
        if (lines.isNotEmpty()) {
            val total = lines.sumOf { it.subtotal }.round2()
            Column(Modifier.fillMaxWidth().background(Color.White)) {
                HorizontalDivider(color = BetoColors.Border)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total a cobrar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(total.money(), style = PriceDisplay, color = BetoColors.Primary)
                    }
                    BigButton("Ver venta (${lines.size})", onOpenCart, icon = Icons.Filled.ShoppingCart)
                }
            }
        }
    }
}

@Composable
private fun SaleProductCard(
    product: Product,
    inCart: Double?,
    onAdd: () -> Unit,
    onQuantity: (Double) -> Unit
) {
    BetoCard {
        Text(product.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = BetoColors.TextPrimary)
        Spacer(Modifier.height(6.dp))
        StockBadge(product)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Precio por ${product.unit.word}", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                Text(product.salePrice.money(), style = PriceItem, color = BetoColors.TextPrimary)
            }
            when {
                product.isOut -> Text("No hay", style = MaterialTheme.typography.labelLarge, color = BetoColors.OnErrorContainer)
                inCart == null -> BigButton("Agregar", onAdd, Modifier.width(160.dp), icon = Icons.Filled.Add, compact = true)
                else -> QuantityStepper(
                    value = inCart,
                    onValueChange = onQuantity,
                    step = product.unit.step,
                    unit = product.unit.short,
                    max = product.stock
                )
            }
        }
    }
}

// ===========================================================================
// Resumen de venta y cobro
// ===========================================================================

@Composable
fun CartScreen(vm: SellViewModel, onBack: () -> Unit, onSold: (Long) -> Unit) {
    val lines by vm.lines.collectAsStateWithLifecycle()
    var paidText by rememberSaveable { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val total = lines.sumOf { it.subtotal }.round2()
    // Si no se escribe nada, se asume que el cliente pagó exacto.
    val paid = if (paidText.isBlank()) total else (paidText.toAmount() ?: 0.0)
    val enough = lines.isNotEmpty() && paid + 0.001 >= total
    val change = (paid - total).round2()

    Column(Modifier.fillMaxSize().imePadding()) {
        BetoHeader(title = "Resumen de venta", subtitle = STORE_NAME, onBack = onBack)

        if (lines.isEmpty()) {
            Column(Modifier.weight(1f).padding(20.dp), verticalArrangement = Arrangement.Center) {
                EmptyState("No hay productos en la venta", "Regresa y toca «Agregar» en los productos que se llevó el cliente.")
                BigButton("Agregar productos", onBack, icon = Icons.Filled.Add)
            }
        } else {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lines, key = { it.product.id }) { line ->
                    CartLineCard(
                        line = line,
                        onQuantity = { vm.setQuantity(line.product, it) },
                        onRemove = { vm.remove(line.product) }
                    )
                }
                item {
                    PaymentCard(
                        total = total,
                        paidText = paidText,
                        onPaidChange = { paidText = it },
                        paid = paid,
                        change = change,
                        enough = enough
                    )
                }
            }

            Column(Modifier.fillMaxWidth().background(Color.White)) {
                HorizontalDivider(color = BetoColors.Border)
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        Text(total.money(), style = PriceDisplay, color = BetoColors.Primary)
                    }
                    error?.let { message ->
                        Text(message, style = MaterialTheme.typography.bodyMedium, color = BetoColors.Error)
                    }
                    BigButton(
                        text = "Cobrar y guardar venta",
                        icon = Icons.Filled.Check,
                        enabled = enough && !saving,
                        onClick = {
                            saving = true
                            error = null
                            vm.checkout(
                                paid = paid,
                                onSuccess = { id ->
                                    saving = false
                                    onSold(id)
                                },
                                onError = { message ->
                                    saving = false
                                    error = message
                                }
                            )
                        }
                    )
                    BigButton("Seguir agregando", onBack, style = BtnStyle.Secondary, compact = true)
                }
            }
        }
    }
}

@Composable
private fun CartLineCard(line: CartLine, onQuantity: (Double) -> Unit, onRemove: () -> Unit) {
    val product = line.product
    BetoCard {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Text(product.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    "${product.salePrice.money()} por ${product.unit.word}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BetoColors.TextSecondary
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Filled.Delete, contentDescription = "Quitar de la venta", tint = BetoColors.Error, modifier = Modifier.size(28.dp))
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            QuantityStepper(
                value = line.quantity,
                onValueChange = onQuantity,
                step = product.unit.step,
                unit = product.unit.short,
                max = product.stock,
                modifier = Modifier.weight(1f)
            )
            Text(line.subtotal.money(), style = PriceItem, color = BetoColors.TextPrimary)
        }
        if (line.quantity >= product.stock - 1e-9) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Es todo lo que hay en inventario.",
                style = MaterialTheme.typography.bodyMedium,
                color = BetoColors.OnWarningContainer
            )
        }
    }
}

@Composable
private fun PaymentCard(
    total: Double,
    paidText: String,
    onPaidChange: (String) -> Unit,
    paid: Double,
    change: Double,
    enough: Boolean
) {
    BetoCard(background = BetoColors.SkyLight, borderColor = BetoColors.Sky) {
        SectionTitle("¿Con cuánto pagó el cliente?")
        Spacer(Modifier.height(12.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BetoChip("Exacto", paidText.isBlank(), { onPaidChange("") })
            listOf("50", "100", "200", "500").forEach { amount ->
                BetoChip("\$$amount", paidText == amount, { onPaidChange(amount) })
            }
        }
        Spacer(Modifier.height(12.dp))
        LabeledField(
            label = "El cliente pagó con",
            value = paidText,
            onValueChange = onPaidChange,
            keyboardType = KeyboardType.Decimal,
            prefix = "\$",
            placeholder = total.qty()
        )
        Spacer(Modifier.height(12.dp))
        if (enough) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Cambio a entregar", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(change.money(), style = PriceItem, color = BetoColors.Success)
            }
        } else {
            Text(
                "Falta dinero: ${(total - paid).round2().money()}",
                style = MaterialTheme.typography.titleMedium,
                color = BetoColors.Error
            )
        }
    }
}

// ===========================================================================
// Venta guardada
// ===========================================================================

@Composable
fun SaleDoneScreen(saleId: Long, vm: SellViewModel, onAnotherSale: () -> Unit, onHome: () -> Unit) {
    val flow = remember(saleId) { vm.sale(saleId) }
    val data by flow.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    var customerPhone by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Cobro de venta", subtitle = STORE_NAME)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                Modifier.size(112.dp).background(BetoColors.Success, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(72.dp))
            }
            Text(
                "¡Venta cobrada con éxito!",
                style = MaterialTheme.typography.headlineMedium,
                color = BetoColors.TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            val sale = data
            if (sale != null) {
                Text(
                    "Ticket #${sale.sale.id} • ${sale.sale.timestamp.timeText()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BetoColors.TextSecondary
                )
                BetoCard {
                    Text("Monto total cobrado", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                    Text(sale.sale.total.money(), style = PriceDisplay, color = BetoColors.Primary)
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BetoColors.Border)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Cliente pagó con", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(sale.sale.paidWith.money(), style = PriceItem)
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth()) {
                        Text("Cambio entregado", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                        Text(sale.sale.change.money(), style = PriceItem, color = BetoColors.Success)
                    }
                }
                BetoCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Inventory2, null, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Text("Inventario actualizado", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Se descontó automáticamente:", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    sale.items.forEach { item ->
                        Text(
                            "✓ ${item.productName} (-${item.quantity.qty()} ${item.unit.short})",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                BetoCard {
                    SectionTitle("Ticket para el cliente")
                    Spacer(Modifier.height(10.dp))
                    LabeledField(
                        label = "WhatsApp del cliente (opcional)",
                        value = customerPhone,
                        onValueChange = { customerPhone = it },
                        keyboardType = KeyboardType.Phone,
                        supportingText = "Si lo dejas vacío, WhatsApp te deja elegir el contacto."
                    )
                    Spacer(Modifier.height(12.dp))
                    BigButton(
                        "Enviar ticket por WhatsApp",
                        { openWhatsApp(context, customerPhone, saleTicketMessage(sale)) },
                        icon = Icons.AutoMirrored.Filled.Send,
                        style = BtnStyle.Secondary
                    )
                }
            }
        }
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            HorizontalDivider(color = BetoColors.Border)
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                BigButton("Hacer otra venta", onAnotherSale, icon = Icons.Filled.Add)
                BigButton("Ir a la pantalla de inicio", onHome, icon = Icons.Filled.Home, style = BtnStyle.Secondary)
            }
        }
    }
}
