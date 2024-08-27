package se.kjellstrand.webshooter.data.mappers

import se.kjellstrand.webshooter.data.User
import se.kjellstrand.webshooter.data.remote.UserDto


fun UserDto.toUser(): User {
    return User(firstName, lastName, pistolShooterCardNumber)
}
