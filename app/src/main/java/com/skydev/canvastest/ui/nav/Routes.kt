package com.skydev.canvastest.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes {

    @Serializable
    object Timeline : AppRoutes

    @Serializable
    data class NoteTaking(
        val id: String? = null
    ) : AppRoutes


}