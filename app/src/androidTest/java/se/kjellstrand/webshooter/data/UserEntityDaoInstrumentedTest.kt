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
import se.kjellstrand.webshooter.data.user.local.UserDao
import se.kjellstrand.webshooter.data.user.local.UserEntity
import java.util.concurrent.CountDownLatch

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
@SmallTest
class UserEntityDaoInstrumentedTest {
    private lateinit var database: ShooterDatabase
    private lateinit var userDao: UserDao

    @Before
    fun setupDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ShooterDatabase::class.java
        ).allowMainThreadQueries().build()

        userDao = database.userDao
    }

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("se.kjellstrand.data", appContext.packageName)
    }

    @Test
    fun insertUsers_andGetAllUsers() = runBlocking {
        val userEntity1 =
            UserEntity(
                uid = 1,
                firstName = "John",
                lastName = "Doe",
                pistolShooterCardNumber = 12345
            )
        val userEntity2 =
            UserEntity(
                uid = 2,
                firstName = "Jane",
                lastName = "Doe",
                pistolShooterCardNumber = 67890
            )
        userDao.insertAll(listOf(userEntity1, userEntity2))

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            assertThat(userDao.getAll()).containsExactly(userEntity1, userEntity2)
                latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun loadUsersByIds() = runBlocking {
        val userEntity1 =
            UserEntity(
                uid = 1,
                firstName = "John",
                lastName = "Doe",
                pistolShooterCardNumber = 12345
            )
        val userEntity2 =
            UserEntity(
                uid = 2,
                firstName = "Jane",
                lastName = "Doe",
                pistolShooterCardNumber = 67890
            )
        userDao.insertAll(listOf(userEntity1, userEntity2))

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            assertThat(userDao.loadAllByIds(intArrayOf(1, 2))).containsExactly(
                userEntity1,
                userEntity2
            )
                latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun findUserByPSCNumber() = runBlocking {
        val userEntity1 =
            UserEntity(
                uid = 1,
                firstName = "John",
                lastName = "Doe",
                pistolShooterCardNumber = 12345
            )
        val userEntity2 =
            UserEntity(
                uid = 2,
                firstName = "Jane",
                lastName = "Doe",
                pistolShooterCardNumber = 67890
            )
        userDao.insertAll(listOf(userEntity1, userEntity2))

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            assertThat(userDao.findByPSCNumber(12345)).isEqualTo(userEntity1)
                latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    @Test
    fun deleteUser() = runBlocking {
        val userEntity =
            UserEntity(
                uid = 1,
                firstName = "John",
                lastName = "Doe",
                pistolShooterCardNumber = 12345
            )
        userDao.insertAll(listOf(userEntity))

        userDao.delete(userEntity)

        val latch = CountDownLatch(1)
        val job = async(Dispatchers.IO) {
            assertThat(userDao.getAll()).doesNotContain(userEntity)
                latch.countDown()
        }
        latch.await()
        job.cancelAndJoin()
    }

    @After
    fun closeDatabase() {
        database.close()
    }
}