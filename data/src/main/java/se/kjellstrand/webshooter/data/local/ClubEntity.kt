package se.kjellstrand.webshooter.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ClubEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "club_name") val clubName: String?,
    @ColumnInfo(name = "club_number") val clubNumber: Int
)