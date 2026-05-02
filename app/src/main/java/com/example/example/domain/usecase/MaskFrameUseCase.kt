package com.example.example.domain.usecase

import com.example.example.domain.StringMasker
import javax.inject.Inject

class MaskFrameUseCase @Inject constructor() : StringMasker {

    override fun mask(value: String): String = invoke(value)

    operator fun invoke(frame: String): String {
        if (frame.length <= 8) return frame
        val visible = 4
        val start = frame.take(visible)
        val end =frame.substring(12, 16)
        val masked = "*".repeat(6)
        return "$start$masked$end"
    }
}
