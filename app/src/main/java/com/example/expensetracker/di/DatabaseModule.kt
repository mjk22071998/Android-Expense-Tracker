package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import com.example.expensetracker.data.local.dao.CategoryDao
import com.example.expensetracker.data.local.dao.LedgerDao
import com.example.expensetracker.data.local.dao.TransactionDao
import com.example.expensetracker.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }

    @Provides
    @Singleton
    fun providesTransactionDao(appDatabase: AppDatabase): TransactionDao{
        return appDatabase.transactionDao()
    }

    @Provides
    @Singleton
    fun providesCategoryDao(appDatabase: AppDatabase): CategoryDao{
        return appDatabase.categoryDao()
    }

    @Provides
    @Singleton
    fun providesLedgerDao(appDatabase: AppDatabase): LedgerDao{
        return appDatabase.ledgerDao()
    }
}