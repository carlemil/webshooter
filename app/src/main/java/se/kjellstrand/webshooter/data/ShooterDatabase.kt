package se.kjellstrand.webshooter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import se.kjellstrand.webshooter.data.login.local.LoginDao
import se.kjellstrand.webshooter.data.login.local.LoginEntity

@Database(entities = [ LoginEntity::class], version = 1, exportSchema = false)
abstract class ShooterDatabase : RoomDatabase() {
    abstract fun loginDao(): LoginDao
}