package com.memad.imagenotes.di

import com.memad.imagenotes.BuildConfig
import com.memad.imagenotes.api.ApiClient
import com.memad.imagenotes.api.ApiClient.Companion.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class ApiModule {

    @Singleton
    @Provides
    fun provideRetrofit(client: OkHttpClient.Builder): Retrofit {
        //TODO: Change base url with the server one
        return Retrofit.Builder().apply {
            baseUrl(BASE_URL)
            client(client.build())
            addConverterFactory(GsonConverterFactory.create())
        }.build()
    }

    @Singleton
    @Provides
    fun providesHttpLoggingInterceptor(): OkHttpClient.Builder {
        return OkHttpClient.Builder().apply {
            if (BuildConfig.DEBUG) {
                val logging = HttpLoggingInterceptor()
                logging.level = HttpLoggingInterceptor.Level.BODY
                this.addInterceptor(logging)
            }
        }
    }

    @Singleton
    @Provides
    fun provideApiClient(retrofit: Retrofit): ApiClient {
        return retrofit.create(ApiClient::class.java)
    }

}