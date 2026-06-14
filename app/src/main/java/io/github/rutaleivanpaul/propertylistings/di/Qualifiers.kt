package io.github.rutaleivanpaul.propertylistings.di

import javax.inject.Qualifier

/** Marks the IO [kotlinx.coroutines.CoroutineDispatcher] used for network and mapping work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/**
 * Marks the application-lifetime [kotlinx.coroutines.CoroutineScope] used for fire-and-forget work
 * (telemetry) that must outlive any single screen or request.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
