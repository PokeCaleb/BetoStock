package com.betostock.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.betostock.data.OrderLine
import com.betostock.data.PurchaseOrderItem
import com.betostock.data.SaleWithItems
import com.betostock.data.subtotal
import com.betostock.data.Supplier
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val STORE_NAME = "Abarrotes Don Beto"
const val OWNER_NAME = "Don Beto"

private val mx = Locale("es", "MX")

fun Double.money(): String = "\$" + String.format(mx, "%,.2f", this)

/** 3.0 -> "3", 1.5 -> "1.5" */
fun Double.qty(): String =
    if (this % 1.0 == 0.0) this.toLong().toString()
    else String.format(mx, "%.2f", this).trimEnd('0').trimEnd('.')

/** Convierte texto del teclado ("12,50" o "12.50") en número; null si no es válido. */
fun String.toAmount(): Double? = trim().replace(',', '.').toDoubleOrNull()

// ---------- Fechas ----------

fun LocalDate.startMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun LocalDate.endMillis(): Long = plusDays(1).startMillis() - 1

private fun Long.format(pattern: String): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern(pattern, mx))

fun Long.timeText(): String = format("h:mm a")
fun Long.dateTimeText(): String = format("d MMM, h:mm a")

fun LocalDate.longText(): String =
    format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", mx)).replaceFirstChar { it.uppercase() }

// ---------- Días de visita de proveedores ----------

val DAY_CODES = listOf("L", "M", "X", "J", "V", "S", "D")
val DAY_NAMES = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

fun DayOfWeek.code(): String = DAY_CODES[ordinal]

fun Supplier.visitsToday(): Boolean = visitDays.split(",").contains(LocalDate.now().dayOfWeek.code())

fun Supplier.visitDaysText(): String =
    DAY_CODES.indices.filter { visitDays.split(",").contains(DAY_CODES[it]) }
        .joinToString(", ") { DAY_NAMES[it] }

// ---------- WhatsApp y teléfono ----------

private fun normalizePhone(phone: String): String {
    val digits = phone.filter { it.isDigit() }
    // Número de 10 dígitos: se asume México (+52)
    return if (digits.length == 10) "52$digits" else digits
}

fun openWhatsApp(context: Context, phone: String, message: String) {
    val number = normalizePhone(phone)
    val uri = Uri.parse("https://wa.me/$number?text=" + Uri.encode(message))
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_LONG).show()
    }
}

fun dialPhone(context: Context, phone: String) {
    val digits = phone.filter { it.isDigit() }
    if (digits.isEmpty()) return
    try {
        context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits")))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No se pudo abrir el teléfono", Toast.LENGTH_LONG).show()
    }
}

private fun orderMessage(supplier: Supplier, itemLines: List<String>): String = buildString {
    val greeting = if (supplier.contactName.isNotBlank()) "Hola ${supplier.contactName}" else "Hola"
    appendLine("$greeting, le habla $OWNER_NAME de $STORE_NAME.")
    appendLine("Quiero hacer el siguiente pedido:")
    appendLine()
    itemLines.forEach { appendLine(it) }
    appendLine()
    append("Gracias.")
}

fun orderMessageFromDraft(supplier: Supplier, lines: List<OrderLine>): String =
    orderMessage(supplier, lines.map { "• ${it.quantity.qty()} ${it.product.unit.short} - ${it.product.name}" })

fun orderMessageFromItems(supplier: Supplier, items: List<PurchaseOrderItem>): String =
    orderMessage(supplier, items.map { "• ${it.quantity.qty()} ${it.unit.short} - ${it.productName}" })

/** Texto del ticket de venta para mandarlo por WhatsApp al cliente. */
fun saleTicketMessage(data: SaleWithItems): String = buildString {
    val sale = data.sale
    appendLine("*$STORE_NAME*")
    appendLine("Ticket #${sale.id}")
    appendLine(sale.timestamp.dateTimeText())
    appendLine("--------------------")
    data.items.forEach { item ->
        appendLine("${item.quantity.qty()} ${item.unit.short} ${item.productName}")
        appendLine("    ${item.quantity.qty()} x ${item.unitPrice.money()} = ${item.subtotal.money()}")
    }
    appendLine("--------------------")
    appendLine("*TOTAL: ${sale.total.money()}*")
    appendLine("Pagó con: ${sale.paidWith.money()}")
    appendLine("Cambio: ${sale.change.money()}")
    appendLine()
    append("¡Gracias por su compra!")
}
