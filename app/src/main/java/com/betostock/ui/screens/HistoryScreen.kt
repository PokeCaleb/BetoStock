package com.betostock.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.data.subtotal
import com.betostock.ui.components.BetoCard
import com.betostock.ui.components.BetoHeader
import com.betostock.ui.components.BigButton
import com.betostock.ui.components.BtnStyle
import com.betostock.ui.components.EmptyState
import com.betostock.ui.components.SquareIconButton
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.theme.PriceDisplay
import com.betostock.ui.theme.PriceItem
import com.betostock.ui.viewmodel.HistoryViewModel
import com.betostock.util.STORE_NAME
import com.betostock.util.longText
import com.betostock.util.money
import com.betostock.util.openWhatsApp
import com.betostock.util.saleTicketMessage
import com.betostock.util.qty
import com.betostock.util.timeText
import java.time.LocalDate

/** Historial de ventas con total y ganancia del día (RF-05). */
@Composable
fun HistoryScreen(vm: HistoryViewModel, onBack: () -> Unit) {
    val report by vm.report.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current
    LaunchedEffect(Unit) { vm.goToToday() }

    val today = LocalDate.now()
    val dayLabel = when (report.date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> "Ventas"
    }

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Ventas del día", subtitle = STORE_NAME, onBack = onBack)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    SquareIconButton(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Día anterior", { vm.previousDay() })
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dayLabel, style = MaterialTheme.typography.titleLarge)
                        Text(
                            report.date.longText(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = BetoColors.TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (report.date.isBefore(today)) {
                        SquareIconButton(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Día siguiente", { vm.nextDay() })
                    } else {
                        Spacer(Modifier.size(56.dp))
                    }
                }
            }
            item {
                BetoCard(background = BetoColors.SkyLight, borderColor = BetoColors.Sky) {
                    Text("Total vendido", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                    Text(report.total.money(), style = PriceDisplay, color = BetoColors.Primary)
                    Text(
                        if (report.sales.size == 1) "1 venta" else "${report.sales.size} ventas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BetoColors.Sky)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Pagado a proveedores", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                            Text(
                                if (report.supplierPayments > 0.0) "−" + report.supplierPayments.money() else report.supplierPayments.money(),
                                style = PriceItem,
                                color = BetoColors.Error
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Dinero del día", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                            Text(
                                report.cash.money(),
                                style = PriceItem,
                                color = if (report.cash < 0.0) BetoColors.Error else BetoColors.TextPrimary
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = BetoColors.Sky)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Ganancia", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                            Text(report.profit.money(), style = PriceItem, color = BetoColors.Success)
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Margen", style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                            Text("${report.marginPercent.toInt()}%", style = PriceItem, color = BetoColors.TextPrimary)
                        }
                    }
                }
            }
            if (report.sales.isEmpty()) {
                item { EmptyState("No hay ventas este día", "Cuando registres una venta, aparecerá aquí.") }
            }
            items(report.sales, key = { it.sale.id }) { data ->
                val open = expanded == data.sale.id
                BetoCard(onClick = { expanded = if (open) null else data.sale.id }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Venta #${data.sale.id}", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                            Text(
                                "${data.sale.timestamp.timeText()} • ${data.items.size} producto(s)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = BetoColors.TextSecondary
                            )
                        }
                        Text(data.sale.total.money(), style = PriceItem, color = BetoColors.Primary)
                    }
                    if (open) {
                        Spacer(Modifier.height(10.dp))
                        HorizontalDivider(color = BetoColors.Border)
                        Spacer(Modifier.height(10.dp))
                        data.items.forEach { item ->
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.productName, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        "${item.quantity.qty()} ${item.unit.short} × ${item.unitPrice.money()}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = BetoColors.TextSecondary
                                    )
                                }
                                Text(item.subtotal.money(), style = MaterialTheme.typography.labelLarge)
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                        Text(
                            "Pagó con ${data.sale.paidWith.money()} • Cambio ${data.sale.change.money()}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BetoColors.TextSecondary
                        )
                        Spacer(Modifier.height(10.dp))
                        BigButton(
                            "Enviar ticket por WhatsApp",
                            { openWhatsApp(context, "", saleTicketMessage(data)) },
                            icon = Icons.AutoMirrored.Filled.Send,
                            style = BtnStyle.Secondary,
                            compact = true
                        )
                    }
                }
            }
        }
    }
}

