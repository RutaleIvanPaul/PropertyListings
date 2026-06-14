package io.github.rutaleivanpaul.propertylistings.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

/**
 * Provides the application-wide networking and serialization singletons.
 *
 * Only the transport infrastructure (JSON parser, HTTP client, Retrofit) is wired here. The
 * concrete API service interfaces, repositories and mappers are introduced in the data-layer
 * milestone; this module exists so the Hilt graph is complete and compiles from the skeleton.
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
}
