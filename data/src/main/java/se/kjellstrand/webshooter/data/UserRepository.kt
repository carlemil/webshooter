package se.kjellstrand.webshooter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import se.kjellstrand.webshooter.data.local.UserEntity
import se.kjellstrand.webshooter.data.local.UserLocalDataSource
import se.kjellstrand.webshooter.data.mappers.mapUserDtoToEntity
import se.kjellstrand.webshooter.data.remote.UserRemoteDataSource
import se.kjellstrand.webshooter.util.Resource

open class UserRepository(
    private val userRemoteDataSource: UserRemoteDataSource, // network
    private val userLocalDataSource: UserLocalDataSource // database
) {

    fun getAll(): Flow<List<UserEntity>> {
        return flow {
            when (val result = userRemoteDataSource.getAll()) {
                is Resource.Error -> {
                    result.data?.let { userLocalDataSource.put(it.mapUserDtoToEntity()) }
                }
                is Resource.Success -> {
                    emit(userLocalDataSource.getAll())
                }
                is Resource.Loading -> TODO()
            }
//            if (users is List<*> && users.all { it is UserEntity }) {
//                userLocalDataSource.put(users as List<UserEntity>)
//        }
//
//            val localUsers = userLocalDataSource.getAll()
//            emit(localUsers) // Emit the collected list from the Flow

        }
    }
}