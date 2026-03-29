package com.skydev.canvastest.ui.nav

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoutes {

    @Serializable
    object Timeline : AppRoutes

    @Serializable
    object NoteTaking : AppRoutes


}