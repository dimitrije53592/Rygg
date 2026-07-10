package com.example.rygg.core.ui.utils

fun String.capitalize(): String = this.lowercase().replaceFirstChar { it.uppercase() }
