package com.iso.data.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


private const val currentUrl = "https://purs-demo-bucket-test.s3.us-west-2.amazonaws.com/"


fun createNetworkClient() = retrofitClient (
    okHttpClient()
)

private fun retrofitClient(httpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(currentUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}


private fun getLogInterceptor() = HttpLoggingInterceptor().apply { level =
    HttpLoggingInterceptor.Level.BODY
}

private fun okHttpClient() = OkHttpClient.Builder()
    .addInterceptor(getLogInterceptor()).apply {
        setTimeOutToOkHttpClient(this)
            .addInterceptor(headersInterceptor())
    }.build()


fun headersInterceptor() = Interceptor { chain ->
    val url = chain.request().url.newBuilder().build()
    chain.proceed(chain.request().newBuilder().url(url).build())
}


private fun setTimeOutToOkHttpClient(okHttpClientBuilder: OkHttpClient.Builder) =
    okHttpClientBuilder.apply {
        readTimeout(30L, TimeUnit.SECONDS)
        connectTimeout(30L, TimeUnit.SECONDS)
        writeTimeout(30L, TimeUnit.SECONDS)
    }