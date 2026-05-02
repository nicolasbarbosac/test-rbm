package com.example.example.di

import com.example.example.data.repository.PaymentRepositoryImpl
import com.example.example.domain.HexDecoder
import com.example.example.domain.StringMasker
import com.example.example.domain.repository.PaymentRepository
import com.example.example.domain.usecase.DecodeHexUseCase
import com.example.example.domain.usecase.MaskFrameUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(impl: PaymentRepositoryImpl): PaymentRepository

    @Binds
    abstract fun bindStringMasker(impl: MaskFrameUseCase): StringMasker

    @Binds
    abstract fun bindHexDecoder(impl: DecodeHexUseCase): HexDecoder
}
