package com.kennyc.api_enphase.intercepor

import okhttp3.Interceptor
import okhttp3.Response

class IdInterceptor(private val appId: String, private val userId: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.newBuilder()
            .addQueryParameter("key", appId)
            .addQueryParameter("user_id", userId)
            .build()

        return request.newBuilder().url(url).build().let {
            chain.proceed(it)
        }
    }
}