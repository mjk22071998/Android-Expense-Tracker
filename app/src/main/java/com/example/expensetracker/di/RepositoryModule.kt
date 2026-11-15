package com.example.expensetracker.di

import com.example.expensetracker.data.repository.CategoryRepository
import com.example.expensetracker.data.repository.CategoryRepositoryImpl
import com.example.expensetracker.data.repository.LedgerRepository
import com.example.expensetracker.data.repository.LedgerRepositoryImpl
import com.example.expensetracker.data.repository.SupabaseAuthRepositoryImpl
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.data.repository.TransactionRepositoryImpl
import com.example.expensetracker.domain.repository.AuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
@Suppress("unused")
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: SupabaseAuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindLedgerRepository(
        impl: LedgerRepositoryImpl
    ): LedgerRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}