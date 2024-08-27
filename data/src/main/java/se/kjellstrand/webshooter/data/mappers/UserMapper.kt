package se.kjellstrand.webshooter.data.mappers

import se.kjellstrand.webshooter.data.User
import se.kjellstrand.webshooter.data.local.UserEntity
import se.kjellstrand.webshooter.data.remote.UserDto


fun UserDto.toUser(): User {
    return User(firstName, lastName, pistolShooterCardNumber)
}

fun List<UserDto>.mapUserDtoToEntity(): List<UserEntity> {
    return mapIndexed { index, dto ->
        UserEntity(
            uid = index,  // Assuming 'uid' is generated based on the index, modify if needed
            firstName = dto.firstName,
            lastName = dto.lastName,
            pistolShooterCardNumber = dto.pistolShooterCardNumber
        )
    }
}
