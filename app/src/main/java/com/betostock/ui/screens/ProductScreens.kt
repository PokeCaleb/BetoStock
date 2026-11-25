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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.data.Category
import com.betostock.data.Product
import com.betostock.data.SaleUnit
import com.betostock.data.needsRestock
import com.betostock.ui.components.Banner
import com.betostock.ui.components.BetoCard
import com.betostock.ui.components.BetoChip
import com.betostock.ui.components.BetoHeader
import com.betostock.ui.components.BigButton
import com.betostock.ui.components.BtnStyle
import com.betostock.ui.components.ConfirmDialog
import com.betostock.ui.components.EmptyState
import com.betostock.ui.components.LabeledField
import com.betostock.ui.components.SearchField
import com.betostock.ui.components.SectionTitle
import com.betostock.ui.components.Tone
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.theme.PriceItem
import com.betostock.ui.viewmodel.ProductsViewModel
import com.betostock.util.STORE_NAME
import com.betostock.util.money
import com.betostock.util.qty
import com.betostock.util.toAmount

// ===========================================================================
// Lista de productos (RF-01)
// ===========================================================================

@Composable
fun ProductsScreen(vm: ProductsViewModel, onEdit: (Long) -> Unit, onAdd: () -> Unit) {
    val products by vm.visible.collectAsStateWithLifecycle()
    val all by vm.allProducts.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val category by vm.category.collectAsStateWithLifecycle()
    val lowOnly by vm.lowOnly.collectAsStateWithLifecycle()
    val lowCount = all.count { it.needsRestock }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Mis productos", subtitle = STORE_NAME)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    if (all.size == 1) "1 producto registrado" else "${all.size} productos registrados",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BetoColors.TextSecondary
                )
            }
            item { SearchField(query, { vm.query.value = it }, "Buscar por nombre") }
            if (lowCount > 0) {
                item {
                    Banner(
                        tone = Tone.Warn,
                        icon = Icons.Filled.Warning,
                        title = if (lowCount == 1) "1 producto por agotarse" else "$lowCount productos por agotarse",
                        message = "Revisa para no quedarte sin mercancía."
                    ) {
                        Spacer(Modifier.height(10.dp))
                        BigButton(
                            if (lowOnly) "Ver todos los productos" else "Ver solo los que faltan",
                            { vm.lowOnly.value = !lowOnly },
                            style = BtnStyle.Secondary,
                            compact = true
                        )
                    }
                }
            }
            item { CategoryChips(category, { vm.category.value = it }) }
            if (products.isEmpty()) {
                item {
                    EmptyState(
                        if (all.isEmpty()) "Aún no hay productos" else "No encontré productos",
                        if (all.isEmpty()) "Toca «Agregar nuevo producto» para registrar el primero." else "Cambia lo que escribiste o el filtro."
                    )
                }
            }
            items(products, key = { it.id }) { product ->
                ProductCard(product, onEdit = { onEdit(product.id) })
            }
        }
        Column(Modifier.fillMaxWidth().background(Color.White)) {
            HorizontalDivider(color = BetoColors.Border)
            Box(Modifier.padding(16.dp)) {
                BigButton("Agregar nuevo producto", onAdd, icon = Icons.Filled.Add)
            }
        }
    }
}

@Composable
private fun ProductCard(product: Product, onEdit: () -> Unit) {
    BetoCard(onClick = onEdit) {
        Text(product.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = BetoColors.TextPrimary)
        Text(
            "${product.category.label} • ${product.unit.label}",
            style = MaterialTheme.typography.bodyMedium,
            color = BetoColors.TextSecondary
        )
        Spacer(Modifier.height(8.dp))
        StockBadge(product)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Precio de venta", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                Text(product.salePrice.money(), style = PriceItem, color = BetoColors.Primary)
            }
            Column(Modifier.weight(1f)) {
                Text("Costo", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                Text(product.costPrice.money(), style = PriceItem, color = BetoColors.TextPrimary)
            }
            BigButton("Ajustar", onEdit, Modifier.width(140.dp), icon = Icons.Filled.Edit, compact = true)
        }
    }
}

// ===========================================================================
// Agregar / editar producto (RF-01, RF-02)
// ===========================================================================

@Composable
fun ProductEditScreen(productId: Long, vm: ProductsViewModel, onBack: () -> Unit) {
    var loaded by remember(productId) { mutableStateOf<Product?>(null) }
    var ready by remember(productId) { mutableStateOf(productId == 0L) }

    LaunchedEffect(productId) {
        if (productId != 0L) {
            val product = vm.get(productId)
            if (product == null) {
                onBack()
            } else {
                loaded = product
                ready = true
            }
        }
    }

    if (!ready) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BetoColors.Primary)
        }
    } else {
        ProductForm(initial = loaded, vm = vm, onBack = onBack)
    }
}

@Composable
private fun ProductForm(initial: Product?, vm: ProductsViewModel, onBack: () -> Unit) {
    val suppliers by vm.suppliers.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf(initial?.name ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.ABARROTES) }
    var unit by remember { mutableStateOf(initial?.unit ?: SaleUnit.PIECE) }
    var cost by remember { mutableStateOf(initial?.costPrice?.takeIf { it > 0.0 }?.qty() ?: "") }
    var sale by remember { mutableStateOf(initial?.salePrice?.takeIf { it > 0.0 }?.qty() ?: "") }
    var stock by remember { mutableStateOf(initial?.stock?.qty() ?: "") }
    var minStock by remember { mutableStateOf(initial?.minStock?.qty() ?: "") }
    var supplierId by remember { mutableStateOf(initial?.supplierId) }
    var showErrors by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    val costValue = cost.toAmount()
    val saleValue = sale.toAmount()
    val nameOk = name.isNotBlank()
    val saleOk = saleValue != null && saleValue > 0.0
    val costOk = cost.isBlank() || costValue != null
    val stockOk = stock.isBlank() || stock.toAmount() != null
    val minOk = minStock.isBlank() || minStock.toAmount() != null
    val canSave = nameOk && saleOk && costOk && stockOk && minOk

    Column(Modifier.fillMaxSize().imePadding()) {
        BetoHeader(
            title = if (initial == null) "Nuevo producto" else "Editar producto",
            subtitle = STORE_NAME,
            onBack = onBack
        )
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            LabeledField(
                label = "Nombre del producto",
                value = name,
                onValueChange = { name = it },
                placeholder = "Ej. Coca-Cola 600 ml",
                isError = showErrors && !nameOk,
                supportingText = if (showErrors && !nameOk) "Escribe el nombre del producto" else null
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("Categoría")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Category.entries.forEach { c -> BetoChip(c.label, category == c, { category = c }) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("¿Cómo se vende?")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SaleUnit.entries.forEach { u -> BetoChip(u.label, unit == u, { unit = u }) }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Precios y ganancia")
                LabeledField(
                    label = "Precio al que compras (costo)",
                    value = cost,
                    onValueChange = { cost = it },
                    keyboardType = KeyboardType.Decimal,
                    prefix = "\$",
                    isError = showErrors && !costOk
                )
                LabeledField(
                    label = "Precio al que vendes",
                    value = sale,
                    onValueChange = { sale = it },
                    keyboardType = KeyboardType.Decimal,
                    prefix = "\$",
                    isError = showErrors && !saleOk,
                    supportingText = if (showErrors && !saleOk) "Escribe el precio de venta" else null
                )
                if (saleValue != null && saleValue > 0.0 && costValue != null) {
                    val profit = saleValue - costValue
                    if (profit >= 0.0) {
                        val percent = if (costValue > 0.0) " (${(profit / costValue * 100.0).toInt()}% de ganancia)" else ""
                        Banner(
                            tone = Tone.Ok,
                            icon = Icons.Filled.CheckCircle,
                            title = "Ganas ${profit.money()} por ${unit.word}$percent"
                        )
                    } else {
                        Banner(
                            tone = Tone.Error,
                            icon = Icons.Filled.Warning,
                            title = "Estás perdiendo ${(-profit).money()} por ${unit.word}",
                            message = "El precio de venta es menor que el costo."
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionTitle("Inventario en tienda")
                LabeledField(
                    label = "Cantidad actual en tienda",
                    value = stock,
                    onValueChange = { stock = it },
                    keyboardType = KeyboardType.Decimal,
                    suffix = unit.short,
                    isError = showErrors && !stockOk
                )
                LabeledField(
                    label = "Avisarme cuando queden menos de",
                    value = minStock,
                    onValueChange = { minStock = it },
                    keyboardType = KeyboardType.Decimal,
                    suffix = unit.short,
                    isError = showErrors && !minOk,
                    supportingText = "Te avisaremos en la pantalla de inicio antes de que se termine."
                )
            }

            if (suppliers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("¿Quién te lo surte?")
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BetoChip("Nadie en especial", supplierId == null, { supplierId = null })
                        suppliers.forEach { s -> BetoChip(s.name, supplierId == s.id, { supplierId = s.id }) }
                    }
                }
            }

            BigButton(
                text = if (initial == null) "Guardar producto" else "Guardar cambios",
                icon = Icons.Filled.CheckCircle,
                onClick = {
                    if (!canSave) {
                        showErrors = true
                    } else {
                        vm.save(
                            Product(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                category = category,
                                unit = unit,
                                costPrice = costValue ?: 0.0,
                                salePrice = saleValue ?: 0.0,
                                stock = stock.toAmount() ?: 0.0,
                                minStock = minStock.toAmount() ?: 0.0,
                                supplierId = supplierId
                            ),
                            onDone = onBack
                        )
                    }
                }
            )
            if (initial != null) {
                BigButton(
                    "Eliminar este producto",
                    { confirmDelete = true },
                    icon = Icons.Filled.Delete,
                    style = BtnStyle.DangerSoft
                )
            }
            BigButton("Cancelar sin guardar", onBack, style = BtnStyle.Secondary, compact = true)
        }
    }

    if (confirmDelete && initial != null) {
        ConfirmDialog(
            title = "¿Eliminar producto?",
            message = "Se borrará «${initial.name}» del inventario. Las ventas ya registradas no se pierden.",
            confirmText = "Sí, eliminar",
            danger = true,
            onConfirm = {
                confirmDelete = false
                vm.delete(initial, onDone = onBack)
            },
            onDismiss = { confirmDelete = false }
        )
    }
}
