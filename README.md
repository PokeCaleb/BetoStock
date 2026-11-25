# BetoStock

App Android (Kotlin + Jetpack Compose + Room) para controlar ventas, inventario y pedidos
a proveedores de la tienda de abarrotes de Don Beto.

## Cómo abrirlo
1. Android Studio → File → Open → carpeta `BetoStock`.
2. Espera el Gradle Sync. Si Studio sugiere actualizar versiones (AGP, Kotlin, Compose BOM), acepta.
3. Ejecuta en un emulador o teléfono (minSdk 26).
   Si falta `gradle-wrapper.jar`, Android Studio usa su Gradle integrado; también puedes ejecutar `gradle wrapper`.

## Datos de ejemplo
En compilaciones **debug** se cargan productos y proveedores de ejemplo cuando la base está vacía
(`data/DemoData.kt`). Para empezar vacío, borra la llamada en `BetoStockApp.kt`.

## Pantallas (tema claro)
Inicio · Vender · Resumen/cobro · Venta guardada · Ventas del día · Productos · Agregar/editar producto ·
Pedidos · Elegir proveedor · Elegir productos · Resumen/envío por WhatsApp · Detalle y recepción de pedido ·
Mercancía recibida · Proveedores · Agregar/editar proveedor.

## Pendiente
Tema oscuro (se agregará un `darkColorScheme` en `ui/theme/Theme.kt`).
