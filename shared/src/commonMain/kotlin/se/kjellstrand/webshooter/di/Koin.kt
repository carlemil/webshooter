package se.kjellstrand.webshooter.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Boot Koin with the platform-agnostic [sharedModule] plus the host's
 * [platformModule]. Call once during app startup — from
 * `ShooterApplication.onCreate` on Android and from the SwiftUI entry
 * point on iOS.
 */
fun initKoin(platformModule: Module): KoinApplication = startKoin {
    modules(sharedModule, viewModelsModule, platformModule)
}
