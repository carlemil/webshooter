package se.kjellstrand.webshooter.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import se.kjellstrand.webshooter.data.ShooterDatabase
import se.kjellstrand.webshooter.data.login.local.LoginDao
import javax.inject.Singleton

//@InstallIn(SingletonComponent::class)
//@Module
class DatabaseModule {

    //@Provides
   // @Singleton
   // fun providesDatabase(@ApplicationContext appContext: Context): ShooterDatabase {
        fun providesDatabase( appContext: Context): ShooterDatabase {
        return Room.databaseBuilder(
            appContext,
            ShooterDatabase::class.java,
            "shooter_db.db"
        ).build()
    }

    //@Provides
    fun provideLoginDao(database: ShooterDatabase): LoginDao {
        return database.loginDao()
    }
}