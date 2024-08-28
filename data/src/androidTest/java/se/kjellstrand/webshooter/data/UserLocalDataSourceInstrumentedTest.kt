package se.kjellstrand.webshooter.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import junit.framework.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import se.kjellstrand.webshooter.data.local.AppDatabase
import se.kjellstrand.webshooter.data.local.UserDao
import se.kjellstrand.webshooter.data.local.UserEntity
import se.kjellstrand.webshooter.data.local.UserLocalDataSource

@RunWith(AndroidJUnit4::class)
class UserLocalDataSourceInstrumentedTest {

    private lateinit var userDao: UserDao
    private lateinit var db: AppDatabase
    private lateinit var userLocalDataSource: UserLocalDataSource

    @Before
    fun setUp() {
        // Create an in-memory database
        db = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().context,
            AppDatabase::class.java
        ).build()

        // Initialize UserDao and UserLocalDataSource
        userDao = db.userDao()
        userLocalDataSource = UserLocalDataSource(userDao)
    }

    @After
    fun tearDown() {
        db.close() // Close the database after tests
    }

    @Test
    fun testPutAndGetUsers() {
        // Given an empty database
        val users = listOf(
            UserEntity(1, "John", "Doe", 123456789),
            UserEntity(2, "Jane", "Doe", 234567890)
        )

        // When inserting users
        userLocalDataSource.put(users)

        // Then the database should contain the inserted users
        val result = userLocalDataSource.getAll()
        assertEquals(users, result)
    }
}
