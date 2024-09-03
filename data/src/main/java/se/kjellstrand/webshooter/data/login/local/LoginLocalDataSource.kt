package se.kjellstrand.webshooter.data.login.local

class LoginLocalDataSource(
    private val loginDao: LoginDao
) {

    fun get(): LoginEntity? {
        return loginDao.getByUid(0)
    }

    fun put(all: LoginEntity) {
        loginDao.insert(all)
    }
}
