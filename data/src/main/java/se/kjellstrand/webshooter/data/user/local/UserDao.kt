package se.kjellstrand.webshooter.data.user.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Query("SELECT * FROM userEntity")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM userEntity WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserEntity>

    @Query("SELECT * FROM userEntity WHERE pistol_shooter_card_number LIKE :number LIMIT 1")
    fun findByPSCNumber(number: Int): UserEntity

    @Insert
    fun insertAll(userEntities: List<UserEntity>)

    @Delete
    fun delete(userEntity: UserEntity)
}