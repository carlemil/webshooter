package se.kjellstrand.webshooter.data.local

import androidx.room.Database
import androidx.room.RoomDatabase


@Database(entities = [ UserEntity::class, ClubEntity::class], version = 1)
abstract class ShooterDatabase : RoomDatabase() {

    abstract val userDao: UserDao
}