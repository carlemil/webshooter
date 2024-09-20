package se.kjellstrand.webshooter.domain

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import se.kjellstrand.webshooter.data.Resource
import se.kjellstrand.webshooter.data.competitions.CompetitionsRepository
import javax.inject.Inject

class GetCompetitionsUseCase

//@Inject
//lateinit var competitionsRepository: CompetitionsRepository
//
//// Sample usage in a non-UI coroutine, such as in a ViewModel or repository
//fun getCompetitions() {
//    runBlocking {
//        launch {
//            competitionsRepository.get().collect { resource ->
//                when (resource) {
//                    is Resource.Success -> {
//                        // Handle successful getCompetitions
//                        println("getCompetitions Success: ${resource.data}")
//                    }
//
//                    is Resource.Error -> {
//                        // Handle error
//                        println("getCompetitions Failed: ${resource.error}")
//                    }
//
//                    else -> {
//                        // Handle other states
//                        println("getCompetitions other state")
//                    }
//                }
//            }
//        }
//    }
//}
