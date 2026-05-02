package com.example.example.di

import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

class JsonFormattingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val body = original.body ?: return chain.proceed(original)

        val buffer = Buffer()
        body.writeTo(buffer)
        val json = buffer.readUtf8()
        val formatted = json.replace("\":\"", "\": \"")

        val newBody = formatted.toRequestBody("application/json".toMediaType())
        val newRequest = original.newBuilder().method(original.method, newBody).build()
        return chain.proceed(newRequest)
    }
}
