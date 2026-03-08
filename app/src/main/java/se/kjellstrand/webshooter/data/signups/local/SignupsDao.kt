package se.kjellstrand.webshooter.data.signups.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SignupsDao {
    @Query("SELECT * FROM signups ORDER BY competitionsId")
    suspend fun getAll(): List<SignupEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(signups: List<SignupEntryEntity>)

    @Query("DELETE FROM signups")
    suspend fun deleteAll()
}
