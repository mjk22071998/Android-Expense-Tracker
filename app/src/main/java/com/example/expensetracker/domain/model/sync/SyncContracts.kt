package com.example.expensetracker.domain.model.sync

/**
 * Sync status for offline-first replication queue (Roadmap v0.3).
 */
enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE
}

/**
 * Common contract for entities supporting Last-Write-Wins (LWW) conflict resolution and sync tracking.
 */
interface SyncableEntity {
    val id: String
    val updatedAt: Long
}
