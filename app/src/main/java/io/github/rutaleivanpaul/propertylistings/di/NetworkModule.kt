package io.github.rutaleivanpaul.propertylistings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.rutaleivanpaul.propertylistings.data.remote.api.PropertyApi
import io.github.rutaleivanpaul.propertylistings.data.remote.api.RatesApi
import io.github.rutaleivanpaul.propertylistings.data.remote.api.StatsApi
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.create
import javax.inject.Singleton

/**
 * Provides the application-wide networking and serialization singletons plus the Retrofit service
 * interfaces. Repository bindings live in [RepositoryModule].
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** All three endpoints are files on the same gist host, used as the Retrofit base URL. */
    private const val BASE_URL = "https://gist.githubusercontent.com/"

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // Tolerant boundary: the real payloads carry many fields we ignore, and we never want a
        // single unexpected/absent value to fail the whole parse.
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
            )
            .build()

    @OptIn(ExperimentalSerializationApi::class)
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun providePropertyApi(retrofit: Retrofit): PropertyApi = retrofit.create()

    @Provides
    @Singleton
    fun provideRatesApi(retrofit: Retrofit): RatesApi = retrofit.create()

    @Provides
    @Singleton
    fun provideStatsApi(retrofit: Retrofit): StatsApi = retrofit.create()
}
