package se.kjellstrand.webshooter.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserDaoInstrumentedTest {
    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = database.userDao()
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("se.kjellstrand.data", appContext.packageName)
    }

    @Test
    fun insertUsers_andGetAllUsers() = runBlocking {
        val user1 =
            User(uid = 1, firstName = "John", lastName = "Doe", pistolShooterCardNumber = 12345)
        val user2 =
            User(uid = 2, firstName = "Jane", lastName = "Doe", pistolShooterCardNumber = 67890)
        userDao.insertAll(user1, user2)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            userDao.getAll().collect {
                assertThat(it).containsExactly(user1, user2)
                latch.countDown()
            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun loadUsersByIds() = runBlocking {
        val user1 =
            User(uid = 1, firstName = "John", lastName = "Doe", pistolShooterCardNumber = 12345)
        val user2 =
            User(uid = 2, firstName = "Jane", lastName = "Doe", pistolShooterCardNumber = 67890)
        userDao.insertAll(user1, user2)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            userDao.loadAllByIds(intArrayOf(1, 2)).collect {
                assertThat(it).containsExactly(user1, user2)
                latch.countDown()
            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun findUserByPSCNumber() = runBlocking {
        val user1 =
            User(uid = 1, firstName = "John", lastName = "Doe", pistolShooterCardNumber = 12345)
        val user2 =
            User(uid = 2, firstName = "Jane", lastName = "Doe", pistolShooterCardNumber = 67890)
        userDao.insertAll(user1, user2)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            userDao.findByPSCNumber(12345).collect {
                assertThat(it).isEqualTo(user1)
                latch.countDown()
            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun deleteUser() = runBlocking {
        val user =
            User(uid = 1, firstName = "John", lastName = "Doe", pistolShooterCardNumber = 12345)
        userDao.insertAll(user)

        userDao.delete(user)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            userDao.getAll().collect {
                assertThat(it).doesNotContain(user)
                latch.countDown()
            }
        }
        latch.await()
        job.cancelAndJoin()
    }

    @After
    fun closeDatabase() {
        database.close()
    }
}