package se.kjellstrand.webshooter.data.login.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface LoginDao {
    @Query("SELECT * FROM loginEntity WHERE uid = :uid")
    fun getByUid(uid: Int): LoginEntity?

    @Insert
    fun insert(loginEntity: LoginEntity)

    @Delete
    fun delete(loginEntity: LoginEntity)
}