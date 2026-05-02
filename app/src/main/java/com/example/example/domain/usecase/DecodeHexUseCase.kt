package com.example.example.domain.usecase

import com.example.example.domain.HexDecoder
import javax.inject.Inject

class DecodeHexUseCase @Inject constructor() : HexDecoder {

    override fun decode(hex: String): String = invoke(hex)

    operator fun invoke(hex: String): String =
        hex.chunked(2).map { it.toInt(16).toChar() }.joinToString("")
}
