package com.betostock.data

/**
 * Datos de ejemplo para probar la app. Solo se cargan en compilaciones debug
 * y únicamente cuando la base de datos está vacía.
 */
suspend fun seedDemoData(repo: StoreRepository) {
    if (repo.productCount() > 0) return

    val coca = repo.saveSupplier(
        Supplier(name = "Coca-Cola FEMSA", contactName = "Juan Martínez", phone = "5512345678", visitDays = "X,S")
    )
    val lala = repo.saveSupplier(
        Supplier(name = "Leche Lala", contactName = "Don Ramón", phone = "5598765432", visitDays = "L,X,V")
    )
    val sabritas = repo.saveSupplier(
        Supplier(name = "Sabritas", contactName = "Marcos Solís", phone = "5511223344", visitDays = "L,J")
    )

    fun p(
        name: String, cat: Category, unit: SaleUnit,
        cost: Double, sale: Double, stock: Double, min: Double, supplier: Long?
    ) = Product(
        name = name, category = cat, unit = unit, costPrice = cost, salePrice = sale,
        stock = stock, minStock = min, supplierId = supplier
    )

    listOf(
        p("Coca-Cola 600 ml", Category.BEBIDAS, SaleUnit.PIECE, 13.5, 18.0, 24.0, 5.0, coca),
        p("Agua Ciel 1 L", Category.BEBIDAS, SaleUnit.PIECE, 11.25, 16.0, 3.0, 6.0, coca),
        p("Leche Lala Entera 1 L", Category.LACTEOS, SaleUnit.PIECE, 22.0, 28.5, 12.0, 4.0, lala),
        p("Sabritas Sal 45 g", Category.BOTANAS, SaleUnit.PIECE, 16.5, 22.0, 18.0, 6.0, sabritas),
        p("Huevo blanco", Category.ABARROTES, SaleUnit.KILO, 36.0, 46.0, 8.0, 3.0, null),
        p("Aceite 1-2-3 1 L", Category.ABARROTES, SaleUnit.PIECE, 34.0, 42.0, 0.0, 3.0, null),
        p("Jabón Zote rosa 400 g", Category.LIMPIEZA, SaleUnit.PIECE, 18.0, 25.0, 15.0, 4.0, null),
        p("Maseca harina 1 kg", Category.ABARROTES, SaleUnit.PIECE, 15.0, 21.0, 10.0, 4.0, null)
    ).forEach { repo.saveProduct(it) }
}
