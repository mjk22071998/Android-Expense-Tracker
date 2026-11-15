package com.example.expensetracker.di

import com.example.expensetracker.data.sync.SyncManagerImpl
import com.example.expensetracker.domain.sync.SyncManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncModule {

    @Binds
    @Singleton
    abstract fun bindSyncManager(
        impl: SyncManagerImpl
    ): SyncManager
}
