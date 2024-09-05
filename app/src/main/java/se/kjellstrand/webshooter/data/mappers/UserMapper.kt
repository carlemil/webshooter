package se.kjellstrand.webshooter.data.mappers

import se.kjellstrand.webshooter.data.user.local.UserEntity
import se.kjellstrand.webshooter.data.user.remote.UserResponse

fun List<UserResponse>.mapUserResponseToEntity(): List<UserEntity> {
    return mapIndexed { index, dto ->
        UserEntity(
            uid = index,  // Assuming 'uid' is generated based on the index, modify if needed
            firstName = dto.firstName,
            lastName = dto.lastName,
            pistolShooterCardNumber = dto.pistolShooterCardNumber
        )
    }
}
