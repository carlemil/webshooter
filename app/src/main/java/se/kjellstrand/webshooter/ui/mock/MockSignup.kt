package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.common.WeaponClass
import se.kjellstrand.webshooter.data.common.ClassnameGeneral

class MockSignup {
    val weaponClasses = listOf(
        WeaponClass(id = 11, weaponGroupsID = 1, classname = "A1", championship = 1, classnameGeneral = ClassnameGeneral.A, pivot = null),
        WeaponClass(id = 15, weaponGroupsID = 2, classname = "B1", championship = 1, classnameGeneral = ClassnameGeneral.B, pivot = null),
        WeaponClass(id = 23, weaponGroupsID = 3, classname = "C1", championship = 1, classnameGeneral = ClassnameGeneral.C, pivot = null),
        WeaponClass(id = 27, weaponGroupsID = 4, classname = "R1", championship = 1, classnameGeneral = ClassnameGeneral.R, pivot = null),
        WeaponClass(id = 29, weaponGroupsID = 6, classname = "CVÄ", championship = 1, classnameGeneral = ClassnameGeneral.Cvä, pivot = null)
    )
}
