package com.betostock.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.betostock.data.Category
import com.betostock.data.Product
import com.betostock.data.SaleUnit
import com.betostock.data.isLow
import com.betostock.data.isOut
import com.betostock.ui.components.BetoChip
import com.betostock.ui.components.StatusBadge
import com.betostock.ui.components.Tone
import com.betostock.util.qty

val SaleUnit.word: String
    get() = when (this) {
        SaleUnit.PIECE -> "pieza"
        SaleUnit.KILO -> "kilo"
        SaleUnit.LITER -> "litro"
    }

/** Filtros por categoría: "Todos" + una ficha por categoría. */
@Composable
fun CategoryChips(selected: Category?, onSelect: (Category?) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BetoChip("Todos", selected == null, { onSelect(null) })
        Category.entries.forEach { c ->
            BetoChip(c.label, selected == c, { onSelect(c) })
        }
    }
}

/** Estado del inventario de un producto, siempre con texto e ícono. */
@Composable
fun StockBadge(product: Product, modifier: Modifier = Modifier) {
    val amount = "${product.stock.qty()} ${product.unit.short}"
    when {
        product.isOut -> StatusBadge("Agotado (0 ${product.unit.short})", Tone.Error, Icons.Filled.Cancel, modifier)
        product.isLow -> StatusBadge("¡Poco stock! ($amount)", Tone.Warn, Icons.Filled.Warning, modifier)
        else -> StatusBadge("En existencia ($amount)", Tone.Ok, Icons.Filled.CheckCircle, modifier)
    }
}
