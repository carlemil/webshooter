package se.kjellstrand.webshooter.data.login.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class LoginEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "token_type") val tokenType: String?,
    @ColumnInfo(name = "expires_in") val expiresIn: Int,
    @ColumnInfo(name = "access_token") val accessToken: String?,
    @ColumnInfo(name = "refresh_token") val refreshToken: String?
)
