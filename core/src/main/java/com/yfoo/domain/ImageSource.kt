package com.yfoo.domain

@JvmInline
value class ImageSource(val value: String) {
    enum class Type { Url, Path }

    val type
        get() = when {
            value.startsWith("https://") || value.startsWith("http://") -> Type.Url
            else -> Type.Path
        }
}