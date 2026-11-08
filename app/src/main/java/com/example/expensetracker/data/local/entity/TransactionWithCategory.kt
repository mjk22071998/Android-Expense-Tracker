package com.example.expensetracker.data.local.entity

data class TransactionWithCategory(
    val id: String,
    val ledgerId: String,
    val categoryId: String?,
    val categoryIcon: String,
    val categoryName: String,
    val amount: Double,
    val type: String,
    val note: String,
    val date: Long,
    val createdAt: Long,
    val updatedAt: Long,
    val isDeleted: Boolean
) {
    /**
     * Maps the joined relation model back into the core Transaction entity.
     */
    fun toTransaction(): Transaction = Transaction(
        id = id,
        ledgerId = ledgerId,
        categoryId = categoryId,
        amount = amount,
        type = type,
        note = note,
        date = date,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isDeleted = isDeleted
    )
}
