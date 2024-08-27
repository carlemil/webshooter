package se.kjellstrand.webshooter.data.local

class UserLocalDataSource(
    private val userDao: UserDao
) {

    fun getAll(): List<UserEntity> {
        return userDao.getAll()
    }

    fun put(all: List<UserEntity>) {
        userDao.insertAll(all)
    }
}
