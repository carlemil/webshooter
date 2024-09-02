package se.kjellstrand.webshooter.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.local.UserEntity
import se.kjellstrand.webshooter.data.local.UserLocalDataSource
import se.kjellstrand.webshooter.data.mappers.mapUserDtoToEntity
import se.kjellstrand.webshooter.data.remote.UserRemoteDataSource

open class UserRepository(
    private val userRemoteDataSource: UserRemoteDataSource, // network
    private val userLocalDataSource: UserLocalDataSource // database
) {

    fun getAll(): Flow<Resource<List<UserEntity>, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            val result = try{
                userRemoteDataSource.getAllUsers(0, "")
            } catch (e: IOException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.IOError))
                return@flow
            } catch (e: HttpException) {
                e.printStackTrace()
                emit(Resource.Error(UserError.HttpError))
                return@flow
            } catch (e: Exception) {
                e.printStackTrace()
                emit(Resource.Error(UserError.UnknownError))
                return@flow
            }
            result.let { userLocalDataSource.put(result.mapUserDtoToEntity()) }

            emit(Resource.Success(userLocalDataSource.getAll()))

            emit(Resource.Loading(false))
        }
    }

    enum class UserError : Error {
        IOError,
        HttpError,
        UnknownError
    }
}
