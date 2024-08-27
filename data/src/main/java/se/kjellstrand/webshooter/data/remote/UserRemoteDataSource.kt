package se.kjellstrand.webshooter.data.remote

import se.kjellstrand.webshooter.util.Resource


class UserRemoteDataSource {

    fun getAll(): Resource<List<UserDto>> {
        return Resource.Error( NotImplementedError().toString())
    }

}
