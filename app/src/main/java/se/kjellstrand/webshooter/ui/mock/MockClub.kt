package se.kjellstrand.webshooter.ui.mock

import se.kjellstrand.webshooter.data.club.remote.ClubData
import se.kjellstrand.webshooter.data.club.remote.ClubMember

class MockClub {
    val clubData = ClubData(
        id = 73,
        clubsNr = "06-073",
        name = "Kullens Pistolklubb",
        email = "info@kullens-pk.se",
        phone = "042-123456",
        addressStreet = "Skyttevägen 12",
        addressStreet2 = null,
        addressZipcode = "263 52",
        addressCity = "Mölle",
        addressCountry = "Sverige",
        bankgiro = "5432-1098",
        postgiro = null,
        swish = "1234567890",
        logoUrl = null,
        admins = listOf(
            ClubMember(userId = 1, name = "Erik", lastname = "Svensson", fullname = "Erik Svensson", email = "erik@kullens-pk.se", shootingCardNumber = "10001", status = "Active"),
            ClubMember(userId = 2, name = "Anna", lastname = "Johansson", fullname = "Anna Johansson", email = "anna@kullens-pk.se", shootingCardNumber = "10002", status = "Active"),
            ClubMember(userId = 3, name = "Lars", lastname = "Nilsson", fullname = "Lars Nilsson", email = "lars@kullens-pk.se", shootingCardNumber = "10003", status = "Active"),
            ClubMember(userId = 4, name = "Maria", lastname = "Karlsson", fullname = "Maria Karlsson", email = "maria@kullens-pk.se", shootingCardNumber = "10004", status = "Active"),
            ClubMember(userId = 5, name = "Olof", lastname = "Bergström", fullname = "Olof Bergström", email = "olof@kullens-pk.se", shootingCardNumber = "10005", status = "Active")
        ),
        users = listOf(
            ClubMember(userId = 10, name = "Gustav", lastname = "Lindgren", fullname = "Gustav Lindgren", email = "gustav@example.se", shootingCardNumber = "20001", status = "Active"),
            ClubMember(userId = 11, name = "Sofia", lastname = "Andersson", fullname = "Sofia Andersson", email = "sofia@example.se", shootingCardNumber = "20002", status = "Active"),
            ClubMember(userId = 12, name = "Oscar", lastname = "Pettersson", fullname = "Oscar Pettersson", email = "oscar@example.se", shootingCardNumber = "20003", status = "Active"),
            ClubMember(userId = 13, name = "Ebba", lastname = "Magnusson", fullname = "Ebba Magnusson", email = "ebba@example.se", shootingCardNumber = "20004", status = "Active"),
            ClubMember(userId = 14, name = "Hugo", lastname = "Eriksson", fullname = "Hugo Eriksson", email = "hugo@example.se", shootingCardNumber = "20005", status = "Active"),
            ClubMember(userId = 15, name = "Wilma", lastname = "Olsson", fullname = "Wilma Olsson", email = "wilma@example.se", shootingCardNumber = "20006", status = "Inactive")
        )
    )
}
