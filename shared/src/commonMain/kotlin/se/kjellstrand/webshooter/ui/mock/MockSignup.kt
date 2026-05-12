package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.ClassnameGeneral

class MockSignup {
    val weaponClasses = listOf(
        WeaponClass(id = 11, classname = "A1", classnameGeneral = ClassnameGeneral.A),
        WeaponClass(id = 15, classname = "B1", classnameGeneral = ClassnameGeneral.B),
        WeaponClass(id = 23, classname = "C1", classnameGeneral = ClassnameGeneral.C),
        WeaponClass(id = 27, classname = "R1", classnameGeneral = ClassnameGeneral.R),
        WeaponClass(id = 29, classname = "CVÄ", classnameGeneral = ClassnameGeneral.Cvä)
    )
}
