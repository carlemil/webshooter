package se.kjellstrand.webshooter.data.club.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ClubDao {
    @Query("SELECT * FROM club LIMIT 1")
    suspend fun get(): ClubEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(club: ClubEntity)

    @Query("DELETE FROM club")
    suspend fun deleteAll()
}
