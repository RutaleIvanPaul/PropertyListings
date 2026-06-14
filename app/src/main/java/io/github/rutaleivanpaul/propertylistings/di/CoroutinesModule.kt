package io.github.rutaleivanpaul.propertylistings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.rutaleivanpaul.propertylistings.domain.time.TimeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * Provides coroutine infrastructure and the time source.
 *
 * Centralised here so the dispatcher and the application scope are injected (not hard-coded with
 * `Dispatchers.IO` / `GlobalScope` inside classes), keeping those classes testable with
 * substituted test dispatchers.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * A long-lived scope for fire-and-forget telemetry. A [SupervisorJob] ensures one failed report
     * never cancels the scope, and the work runs off the main thread.
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(@IoDispatcher dispatcher: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatcher)

    /** Real wall clock; substituted by a fake in tests that reason about elapsed time. */
    @Provides
    @Singleton
    fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }
}
