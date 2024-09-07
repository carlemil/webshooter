package se.kjellstrand.webshooter.data.login.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginLocalDataSource @Inject constructor(private val loginDao: LoginDao) {

    fun get(): LoginEntity? {
        return loginDao.getByUid(0)
    }

    fun put(all: LoginEntity) {
        loginDao.insert(all)
    }
}
