package se.kjellstrand.webshooter.data.mappers

import se.kjellstrand.webshooter.data.login.local.LoginEntity
import se.kjellstrand.webshooter.data.login.remote.LoginResponse

fun LoginResponse.toLoginEntity(): LoginEntity {
    return LoginEntity(0, token_type, expires_in, access_token, refresh_token)
}
