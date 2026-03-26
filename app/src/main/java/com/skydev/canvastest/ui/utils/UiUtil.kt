package com.skydev.canvastest.ui.utils

import androidx.compose.ui.input.pointer.PointerType

fun PointerType.isForAll() : Boolean {
    return this == PointerType.Stylus || this == PointerType.Touch || this == PointerType.Mouse
}

fun PointerType.isForStylus() : Boolean {
    return this == PointerType.Stylus
}