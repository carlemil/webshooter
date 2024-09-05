package se.kjellstrand.webshooter.data

import androidx.room.Database
import androidx.room.RoomDatabase
import se.kjellstrand.webshooter.data.login.local.LoginDao
import se.kjellstrand.webshooter.data.login.local.LoginEntity
import se.kjellstrand.webshooter.data.user.local.UserDao
import se.kjellstrand.webshooter.data.user.local.UserEntity

@Database(entities = [ UserEntity::class, LoginEntity::class], version = 1, exportSchema = false)
abstract class ShooterDatabase : RoomDatabase() {

    abstract val userDao: UserDao
    abstract val loginDao: LoginDao
}