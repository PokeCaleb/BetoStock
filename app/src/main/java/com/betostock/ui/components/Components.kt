package com.betostock.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.betostock.ui.theme.BetoColors
import com.betostock.util.qty

// ---------------------------------------------------------------------------
// Botones
// ---------------------------------------------------------------------------

enum class BtnStyle { Primary, Secondary, Success, Danger, DangerSoft }

/** Botón grande (mínimo 64dp; 56dp en versión compacta) pensado para dedos grandes. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: BtnStyle = BtnStyle.Primary,
    enabled: Boolean = true,
    compact: Boolean = false
) {
    val container: Color
    val content: Color
    var border: BorderStroke? = null
    when (style) {
        BtnStyle.Primary -> {
            container = BetoColors.Primary
            content = Color.White
        }
        BtnStyle.Success -> {
            container = BetoColors.Success
            content = Color.White
        }
        BtnStyle.Danger -> {
            container = BetoColors.Error
            content = Color.White
        }
        BtnStyle.Secondary -> {
            container = BetoColors.SkyLight
            content = BetoColors.Primary
            border = BorderStroke(2.dp, BetoColors.Sky)
        }
        BtnStyle.DangerSoft -> {
            container = BetoColors.ErrorContainer
            content = BetoColors.OnErrorContainer
            border = BorderStroke(2.dp, BetoColors.Error)
        }
    }
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().heightIn(min = if (compact) 56.dp else 64.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = BetoColors.Disabled,
            disabledContentColor = BetoColors.OnDisabled
        ),
        border = if (enabled) border else null,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(if (compact) 24.dp else 28.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
    }
}

@Composable
fun SquareIconButton(icon: ImageVector, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(BetoColors.SkyLight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = BetoColors.Primary, modifier = Modifier.size(28.dp))
    }
}

// ---------------------------------------------------------------------------
// Tarjetas, chips, etiquetas
// ---------------------------------------------------------------------------

@Composable
fun BetoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = BetoColors.Card,
    borderColor: Color = BetoColors.Border,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .border(1.5.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun BetoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) BetoColors.Primary else BetoColors.SkyLight)
            .border(1.5.dp, if (selected) BetoColors.Primary else BetoColors.Sky, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else BetoColors.TextPrimary
        )
    }
}

enum class Tone { Ok, Warn, Error, Info }

fun Tone.containerColor(): Color = when (this) {
    Tone.Ok -> BetoColors.SuccessContainer
    Tone.Warn -> BetoColors.WarningContainer
    Tone.Error -> BetoColors.ErrorContainer
    Tone.Info -> BetoColors.SkyLight
}

fun Tone.contentColor(): Color = when (this) {
    Tone.Ok -> BetoColors.OnSuccessContainer
    Tone.Warn -> BetoColors.OnWarningContainer
    Tone.Error -> BetoColors.OnErrorContainer
    Tone.Info -> BetoColors.PrimaryDark
}

/** Etiqueta de estado: siempre con texto (y opcionalmente ícono), nunca solo color. */
@Composable
fun StatusBadge(text: String, tone: Tone, icon: ImageVector? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(tone.containerColor())
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = tone.contentColor(), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, style = MaterialTheme.typography.labelMedium, color = tone.contentColor())
    }
}

/** Aviso grande (alerta, éxito, información). */
@Composable
fun Banner(
    tone: Tone,
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier,
    message: String? = null,
    extra: @Composable ColumnScope.() -> Unit = {}
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(tone.containerColor())
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = tone.contentColor(), modifier = Modifier.size(32.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = tone.contentColor())
                if (message != null) {
                    Text(message, style = MaterialTheme.typography.bodyMedium, color = tone.contentColor())
                }
            }
        }
        extra()
    }
}

// ---------------------------------------------------------------------------
// Encabezado
// ---------------------------------------------------------------------------

@Composable
fun BetoHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null
) {
    Column(modifier.fillMaxWidth().background(Color.White)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                SquareIconButton(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", onBack)
            } else {
                Box(
                    Modifier.size(48.dp).clip(RoundedCornerShape(14.dp)).background(BetoColors.Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BetoColors.Primary, fontWeight = FontWeight.SemiBold)
                }
                Text(title, style = MaterialTheme.typography.headlineSmall, color = BetoColors.TextPrimary)
            }
        }
        HorizontalDivider(color = BetoColors.Border)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, style = MaterialTheme.typography.titleLarge, color = BetoColors.TextPrimary, modifier = modifier)
}

@Composable
fun EmptyState(title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(message, style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary, textAlign = TextAlign.Center)
    }
}

// ---------------------------------------------------------------------------
// Cantidades
// ---------------------------------------------------------------------------

@Composable
private fun StepButton(icon: ImageVector, description: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(if (enabled) BetoColors.Primary else BetoColors.Disabled)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription = description,
            tint = if (enabled) Color.White else BetoColors.OnDisabled,
            modifier = Modifier.size(30.dp)
        )
    }
}

/** Selector de cantidad con botones – / + grandes. */
@Composable
fun QuantityStepper(
    value: Double,
    onValueChange: (Double) -> Unit,
    step: Double,
    unit: String,
    modifier: Modifier = Modifier,
    min: Double = 0.0,
    max: Double = Double.MAX_VALUE
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        StepButton(Icons.Filled.Remove, "Quitar", enabled = value > min + 1e-9) {
            onValueChange(maxOf(min, value - step))
        }
        Column(Modifier.widthIn(min = 68.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value.qty(), style = MaterialTheme.typography.titleLarge, color = BetoColors.TextPrimary)
            Text(unit, style = MaterialTheme.typography.bodyMedium, color = BetoColors.TextSecondary)
        }
        StepButton(Icons.Filled.Add, "Agregar", enabled = value < max - 1e-9) {
            onValueChange(minOf(max, value + step))
        }
    }
}

// ---------------------------------------------------------------------------
// Campos de texto
// ---------------------------------------------------------------------------

@Composable
fun betoFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BetoColors.Primary,
    unfocusedBorderColor = BetoColors.Border,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color.White,
    focusedTextColor = BetoColors.TextPrimary,
    unfocusedTextColor = BetoColors.TextPrimary,
    errorBorderColor = BetoColors.Error,
    errorContainerColor = Color.White
)

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth().heightIn(min = 64.dp),
        placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyLarge) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = BetoColors.Primary) },
        trailingIcon = if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda")
                }
            }
        } else null,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
        shape = RoundedCornerShape(16.dp),
        colors = betoFieldColors()
    )
}

@Composable
fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: String? = null,
    suffix: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    supportingText: String? = null
) {
    Column(modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = BetoColors.TextPrimary)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp),
            singleLine = true,
            isError = isError,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            placeholder = if (placeholder != null) {
                { Text(placeholder, style = MaterialTheme.typography.bodyLarge) }
            } else null,
            prefix = if (prefix != null) {
                { Text(prefix, style = MaterialTheme.typography.bodyLarge) }
            } else null,
            suffix = if (suffix != null) {
                { Text(suffix, style = MaterialTheme.typography.bodyLarge) }
            } else null,
            supportingText = if (supportingText != null) {
                { Text(supportingText, style = MaterialTheme.typography.bodyMedium) }
            } else null,
            shape = RoundedCornerShape(16.dp),
            colors = betoFieldColors()
        )
    }
}

// ---------------------------------------------------------------------------
// Diálogos
// ---------------------------------------------------------------------------

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancelar",
    danger: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        shape = RoundedCornerShape(24.dp),
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = { Text(message, style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(onClick = onConfirm, modifier = Modifier.heightIn(min = 56.dp)) {
                Text(
                    confirmText,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (danger) BetoColors.Error else BetoColors.Primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.heightIn(min = 56.dp)) {
                Text(dismissText, style = MaterialTheme.typography.labelLarge, color = BetoColors.TextSecondary)
            }
        }
    )
}
