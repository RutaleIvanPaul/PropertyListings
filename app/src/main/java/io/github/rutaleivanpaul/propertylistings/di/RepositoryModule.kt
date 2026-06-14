package io.github.rutaleivanpaul.propertylistings.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.rutaleivanpaul.propertylistings.data.repository.PropertyRepositoryImpl
import io.github.rutaleivanpaul.propertylistings.data.repository.RatesRepositoryImpl
import io.github.rutaleivanpaul.propertylistings.domain.repository.PropertyRepository
import io.github.rutaleivanpaul.propertylistings.domain.repository.RatesRepository
import javax.inject.Singleton

/**
 * Binds the repository interfaces to their networked implementations.
 *
 * Uses `@Binds` (not `@Provides`) because the implementations have `@Inject` constructors and no
 * construction logic is needed — Hilt simply supplies the concretion wherever the abstraction is
 * requested, keeping the dependency rule pointing inward (presentation → domain abstractions).
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPropertyRepository(impl: PropertyRepositoryImpl): PropertyRepository

    @Binds
    @Singleton
    abstract fun bindRatesRepository(impl: RatesRepositoryImpl): RatesRepository
}
