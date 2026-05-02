package com.example.example.data.remote

import com.google.gson.annotations.SerializedName
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class AuthorizationRequestDto(
    @SerializedName("amount") val amount: String
)

data class AuthorizationResponseDto(
    @SerializedName("receiptId") val receiptId: String?,
    @SerializedName("statusCode") val statusCode: String?,
    @SerializedName("statusDescription") val statusDescription: String?,
    @SerializedName("hexData") val hexData: String?
)

data class VoidRequestDto(
    @SerializedName("receiptId") val receiptId: String
)

interface PaymentApiService {
    @POST("api/payments/authorization")
    suspend fun authorize(
        @Header("Authorization") auth: String,
        @Body request: AuthorizationRequestDto
    ): AuthorizationResponseDto

    @POST("/api/payments/annulment")
    suspend fun voidTransaction(
        @Header("Authorization") auth: String,
        @Body request: VoidRequestDto
    ): AuthorizationResponseDto
}
