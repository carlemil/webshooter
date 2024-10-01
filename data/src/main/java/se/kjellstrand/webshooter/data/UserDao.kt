package se.kjellstrand.webshooter.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    fun getAll(): Flow<List<User>>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): Flow<List<User>>

    @Query("SELECT * FROM user WHERE pistol_shooter_card_number LIKE :number LIMIT 1")
    fun findByPSCNumber(number: Int): Flow<User>

    @Insert
    fun insertAll(vararg users: User)

    @Delete
    fun delete(user: User)
}