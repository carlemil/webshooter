package se.kjellstrand.webshooter.data.remote

import kotlinx.coroutines.flow.Flow

class UserRemoteDataSource {

    fun getAll(): Flow<List<UserDto>> {
        throw NotImplementedError()
    }

}
