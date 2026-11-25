package com.betostock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.ui.components.Banner
import com.betostock.ui.components.BetoCard
import com.betostock.ui.components.BetoHeader
import com.betostock.ui.components.BigButton
import com.betostock.ui.components.BtnStyle
import com.betostock.ui.components.SectionTitle
import com.betostock.ui.components.Tone
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.theme.PriceDisplay
import com.betostock.ui.theme.PriceItem
import com.betostock.ui.viewmodel.HomeViewModel
import com.betostock.util.OWNER_NAME
import com.betostock.util.STORE_NAME
import com.betostock.util.longText
import com.betostock.util.money
import com.betostock.util.qty
import com.betostock.util.timeText
import java.time.LocalDate

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    onNewSale: () -> Unit,
    onNewOrder: () -> Unit,
    onHistory: () -> Unit,
    onLowStock: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.refreshDay() }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Inicio", subtitle = STORE_NAME)
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BetoCard(background = BetoColors.SkyLight, borderColor = BetoColors.Sky) {
                Text("¡Hola, $OWNER_NAME!", style = MaterialTheme.typography.headlineMedium, color = BetoColors.TextPrimary)
                Text(LocalDate.now().longText(), style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
            }

            BigButton("Nueva venta", onNewSale, icon = Icons.Filled.AddShoppingCart)
            BigButton("Pedido a proveedor", onNewOrder, icon = Icons.Filled.LocalShipping, style = BtnStyle.Secondary)

            BetoCard(onClick = onHistory) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ReceiptLong, null, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("VENTAS DE HOY", style = MaterialTheme.typography.labelMedium, color = BetoColors.TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Text(state.salesTotal.money(), style = PriceDisplay, color = BetoColors.TextPrimary)
                Text(
                    if (state.salesCount == 1) "1 cobro" else "${state.salesCount} cobros",
                    style = MaterialTheme.typography.titleMedium,
                    color = BetoColors.Primary
                )
            }

            BetoCard(onClick = onHistory) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AccountBalanceWallet, null, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("DINERO DEL DÍA", style = MaterialTheme.typography.labelMedium, color = BetoColors.TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    state.cash.money(),
                    style = PriceDisplay,
                    color = if (state.cash < 0.0) BetoColors.Error else BetoColors.TextPrimary
                )
                Text(
                    if (state.supplierPayments > 0.0) {
                        "Ventas ${state.salesTotal.money()} − proveedores ${state.supplierPayments.money()}"
                    } else {
                        "Ventas de hoy. Se descuenta lo que pagues a proveedores cuando llegue su pedido."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = BetoColors.TextSecondary
                )
            }

            BetoCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Payments, null, tint = BetoColors.Success, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("GANANCIA DE HOY", style = MaterialTheme.typography.labelMedium, color = BetoColors.TextSecondary)
                }
                Spacer(Modifier.height(6.dp))
                Text(state.profit.money(), style = PriceDisplay, color = BetoColors.TextPrimary)
                Text(
                    "Lo que vendiste menos lo que te costó la mercancía.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = BetoColors.TextSecondary
                )
            }

            if (state.lowStock.isNotEmpty()) {
                val names = state.lowStock.take(3).joinToString(", ") { "${it.name} (${it.stock.qty()})" }
                Banner(
                    tone = Tone.Error,
                    icon = Icons.Filled.Warning,
                    title = "Faltan productos (${state.lowStock.size})",
                    message = "Quedan pocas piezas de: $names" + if (state.lowStock.size > 3) " y más." else "."
                ) {
                    Spacer(Modifier.height(12.dp))
                    BigButton("Ver productos por agotar", onLowStock, style = BtnStyle.Danger, compact = true)
                }
            } else {
                Banner(
                    tone = Tone.Ok,
                    icon = Icons.Filled.CheckCircle,
                    title = "Todo en orden",
                    message = "No hay productos por agotarse."
                )
            }

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Últimas ventas", Modifier.weight(1f))
                TextButton(onClick = onHistory) {
                    Text("Ver todo", style = MaterialTheme.typography.labelLarge, color = BetoColors.Primary)
                }
            }
            if (state.recentSales.isEmpty()) {
                Text(
                    "Todavía no hay ventas hoy. Toca «Nueva venta» para empezar.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = BetoColors.TextSecondary
                )
            } else {
                state.recentSales.forEach { s ->
                    BetoCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Venta #${s.sale.id}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold))
                                Text(
                                    "${s.sale.timestamp.timeText()} • ${s.items.size} producto(s)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BetoColors.TextSecondary
                                )
                            }
                            Text("+" + s.sale.total.money(), style = PriceItem, color = BetoColors.Success)
                        }
                    }
                }
            }
        }
    }
}
