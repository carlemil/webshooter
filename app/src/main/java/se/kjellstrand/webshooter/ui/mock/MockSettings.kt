package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.settings.remote.UserProfile

class MockSettings {
    val userProfile = UserProfile(
        name = "Erik",
        lastname = "Svensson",
        email = "erik.svensson@example.se",
        shootingCardNumber = "12345",
        noShootingCardNumber = null,
        birthday = "1985-03-15",
        gender = "male",
        phone = "042-123456",
        mobile = "070-1234567",
        gradeField = "3",
        gradeTrackshooting = "2",
        apiToken = "mock_token",
        userId = 132,
        fullname = "Erik Svensson",
        clubsId = 73,
        status = "Active",
        clubs = listOf(
            se.kjellstrand.webshooter.data.settings.remote.Club(
                id = 73,
                clubsNr = 73,
                name = "Kullens Pistolklubb",
                email = "info@kullens-pk.se",
                phone = "042-123456",
                addressStreet = "Skyttevägen 12",
                addressZipcode = "263 52",
                addressCity = "Mölle",
                addressCountry = "Sverige",
                bankgiro = "5432-1098",
                postgiro = null,
                swish = "1234567890"
            )
        )
    )
}
