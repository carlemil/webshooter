package se.kjellstrand.webshooter.data.user

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import retrofit2.HttpException
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.UserError
import se.kjellstrand.webshooter.data.user.local.UserEntity
import se.kjellstrand.webshooter.data.user.local.UserLocalDataSource
import se.kjellstrand.webshooter.data.mappers.mapUserResponseToEntity
import se.kjellstrand.webshooter.data.user.remote.UserRemoteDataSource

open class UserRepository(
    private val userRemoteDataSource: UserRemoteDataSource, // network
    private val userLocalDataSource: UserLocalDataSource // database
) {

    fun getAll(): Flow<Resource<List<UserEntity>, UserError>> {
        return flow {
            emit(Resource.Loading(true))

            val result = try{
                userRemoteDataSource.getAllUsers(0)
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
            result.let { userLocalDataSource.put(result.mapUserResponseToEntity()) }

            emit(Resource.Success(userLocalDataSource.getAll()))

            emit(Resource.Loading(false))
        }
    }

}
