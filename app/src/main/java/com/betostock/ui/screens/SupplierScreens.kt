package com.betostock.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.betostock.data.Supplier
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
import com.betostock.ui.components.StatusBadge
import com.betostock.ui.components.Tone
import com.betostock.ui.theme.BetoColors
import com.betostock.ui.viewmodel.SuppliersViewModel
import com.betostock.util.DAY_CODES
import com.betostock.util.DAY_NAMES
import com.betostock.util.OWNER_NAME
import com.betostock.util.STORE_NAME
import com.betostock.util.dialPhone
import com.betostock.util.openWhatsApp
import com.betostock.util.visitDaysText
import com.betostock.util.visitsToday

// ===========================================================================
// Directorio de proveedores
// ===========================================================================

@Composable
fun SuppliersScreen(
    vm: SuppliersViewModel,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit
) {
    val suppliers by vm.suppliers.collectAsStateWithLifecycle()
    val query by vm.query.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        BetoHeader(title = "Mis proveedores", subtitle = STORE_NAME, onBack = onBack)
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BigButton("Registrar nuevo proveedor", onAdd, icon = Icons.Filled.Add) }
            item { SearchField(query, { vm.query.value = it }, "Buscar proveedor") }
            if (suppliers.isEmpty()) {
                item {
                    EmptyState(
                        if (query.isBlank()) "Aún no tienes proveedores" else "No encontré proveedores",
                        if (query.isBlank()) "Registra a quien te surte la mercancía para poder hacerle pedidos." else "Cambia lo que escribiste."
                    )
                }
            }
            items(suppliers, key = { it.id }) { supplier ->
                BetoCard(onClick = { onEdit(supplier.id) }) {
                    Text(supplier.name, style = MaterialTheme.typography.titleLarge)
                    if (supplier.contactName.isNotBlank()) {
                        Text(supplier.contactName, style = MaterialTheme.typography.bodyLarge, color = BetoColors.TextSecondary)
                    }
                    if (supplier.visitDays.isNotBlank()) {
                        Text("Visita: ${supplier.visitDaysText()}", style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
                    }
                    if (supplier.visitsToday()) {
                        Spacer(Modifier.height(8.dp))
                        StatusBadge("¡Hoy pasa el camión!", Tone.Info, Icons.Filled.LocalShipping)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        BigButton(
                            "Llamar",
                            { dialPhone(context, supplier.phone) },
                            Modifier.weight(1f),
                            icon = Icons.Filled.Call,
                            style = BtnStyle.Secondary,
                            enabled = supplier.phone.isNotBlank(),
                            compact = true
                        )
                        BigButton(
                            "WhatsApp",
                            { openWhatsApp(context, supplier.phone, "Hola, le habla $OWNER_NAME de $STORE_NAME.") },
                            Modifier.weight(1f),
                            icon = Icons.AutoMirrored.Filled.Send,
                            style = BtnStyle.Success,
                            enabled = supplier.phone.isNotBlank(),
                            compact = true
                        )
                    }
                }
            }
        }
    }
}


// ===========================================================================
// Registrar / editar proveedor
// ===========================================================================

@Composable
fun SupplierEditScreen(supplierId: Long, vm: SuppliersViewModel, onBack: () -> Unit) {
    var loaded by remember(supplierId) { mutableStateOf<Supplier?>(null) }
    var ready by remember(supplierId) { mutableStateOf(supplierId == 0L) }

    LaunchedEffect(supplierId) {
        if (supplierId != 0L) {
            val supplier = vm.get(supplierId)
            if (supplier == null) {
                onBack()
            } else {
                loaded = supplier
                ready = true
            }
        }
    }

    if (!ready) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BetoColors.Primary)
        }
    } else {
        SupplierForm(initial = loaded, vm = vm, onBack = onBack)
    }
}

@Composable
private fun SupplierForm(initial: Supplier?, vm: SuppliersViewModel, onBack: () -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var contact by remember { mutableStateOf(initial?.contactName ?: "") }
    var phone by remember { mutableStateOf(initial?.phone ?: "") }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }
    var days by remember { mutableStateOf(initial?.visitDays?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()) }
    var showErrors by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().imePadding()) {
        BetoHeader(
            title = if (initial == null) "Nuevo proveedor" else "Editar proveedor",
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
                label = "Empresa o marca",
                value = name,
                onValueChange = { name = it },
                placeholder = "Ej. Coca-Cola, Sabritas, Lala",
                isError = showErrors && name.isBlank(),
                supportingText = if (showErrors && name.isBlank()) "Escribe el nombre del proveedor" else null
            )
            LabeledField(
                label = "Nombre del repartidor",
                value = contact,
                onValueChange = { contact = it },
                placeholder = "Ej. Juan Martínez"
            )
            LabeledField(
                label = "Teléfono o WhatsApp",
                value = phone,
                onValueChange = { phone = it },
                keyboardType = KeyboardType.Phone,
                placeholder = "10 dígitos",
                supportingText = "Se usa para llamar y para enviar los pedidos por WhatsApp."
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SectionTitle("¿Qué días pasa?")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DAY_CODES.forEachIndexed { index, code ->
                        BetoChip(
                            DAY_NAMES[index],
                            code in days,
                            { days = if (code in days) days - code else days + code }
                        )
                    }
                }
            }

            LabeledField(
                label = "Notas",
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Ej. Tener listos los envases vacíos"
            )

            BigButton(
                text = "Guardar proveedor",
                icon = Icons.Filled.CheckCircle,
                onClick = {
                    if (name.isBlank()) {
                        showErrors = true
                    } else {
                        vm.save(
                            Supplier(
                                id = initial?.id ?: 0L,
                                name = name.trim(),
                                contactName = contact.trim(),
                                phone = phone.trim(),
                                visitDays = DAY_CODES.filter { it in days }.joinToString(","),
                                notes = notes.trim()
                            ),
                            onDone = onBack
                        )
                    }
                }
            )
            if (initial != null) {
                BigButton("Eliminar proveedor", { confirmDelete = true }, icon = Icons.Filled.Delete, style = BtnStyle.DangerSoft)
            }
            BigButton("Cancelar sin guardar", onBack, style = BtnStyle.Secondary, compact = true)
        }
    }

    if (confirmDelete && initial != null) {
        ConfirmDialog(
            title = "¿Eliminar proveedor?",
            message = "Se borrará «${initial.name}». Sus productos se quedarán sin proveedor y los pedidos anteriores se conservan.",
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
