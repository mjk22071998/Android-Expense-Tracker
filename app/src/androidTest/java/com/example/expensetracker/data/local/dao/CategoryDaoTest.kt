package com.example.expensetracker.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.expensetracker.Constants
import com.example.expensetracker.data.local.database.AppDatabase
import com.example.expensetracker.data.local.entity.Category
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryDao: CategoryDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        categoryDao = db.categoryDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    private fun sampleCategory(
        id: String = "cat_food",
        name: String = "Food",
        icon: String = "restaurant",
        transactionType: String = Constants.TransactionType.EXPENSE,
        isDefault: Boolean = true
    ) = Category(
        id = id,
        name = name,
        icon = icon,
        transactionType = transactionType,
        isDefault = isDefault
    )

    @Test
    fun insertAndReadBackAreConsistent() = runTest {
        categoryDao.insert(sampleCategory())

        val results = categoryDao.getAllCategories().first()

        assertEquals(1, results.size)
        assertEquals("Food", results.first().name)
    }

    @Test
    fun insertingTheSameIdTwiceIgnoresTheSecondInsert() = runTest {
        // This is the exact guarantee CategorySeeder depends on to be safely
        // re-run on every single app launch without ever duplicating seeded rows.
        categoryDao.insert(sampleCategory(id = "cat_food", name = "Food"))
        categoryDao.insert(sampleCategory(id = "cat_food", name = "Groceries"))

        val results = categoryDao.getAllCategories().first()

        assertEquals(1, results.size)
        assertEquals("Food", results.first().name) // original survives, second insert silently ignored
    }

    @Test
    fun updateChangesNameAndIcon() = runTest {
        categoryDao.insert(sampleCategory(id = "cat_custom", name = "Old Name", icon = "other", isDefault = false))

        val existing = categoryDao.getAllCategories().first().first()
        categoryDao.update(existing.copy(name = "New Name", icon = "travel"))

        val updated = categoryDao.getAllCategories().first().first()
        assertEquals("New Name", updated.name)
        assertEquals("travel", updated.icon)
    }

    @Test
    fun deletingADefaultCategoryHasNoEffect() = runTest {
        categoryDao.insert(sampleCategory(id = "cat_food", isDefault = true))

        categoryDao.delete("cat_food")

        val results = categoryDao.getAllCategories().first()
        assertEquals(1, results.size) // still there — WHERE isDefault = 0 protected it
    }

    @Test
    fun deletingACustomCategoryActuallyRemovesIt() = runTest {
        categoryDao.insert(sampleCategory(id = "cat_custom", isDefault = false))

        categoryDao.delete("cat_custom")

        val results = categoryDao.getAllCategories().first()
        assertTrue(results.isEmpty())
    }

    @Test
    fun getNonDefaultCategoriesExcludesSeededOnes() = runTest {
        categoryDao.insert(sampleCategory(id = "cat_food", isDefault = true))
        categoryDao.insert(sampleCategory(id = "cat_custom", name = "Freelance", isDefault = false))

        val nonDefault = categoryDao.getNonDefaultCategories()

        assertEquals(1, nonDefault.size)
        assertEquals("Freelance", nonDefault.first().name)
    }
}