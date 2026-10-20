package com.dimitrijeilic.rygg.feature.library.domain

fun exportBaseName(routeName: String): String =
    routeName.replace(ILLEGAL_FILE_NAME_CHARS, "")
        .trim()
        .trimEnd('.')
        .take(MAX_EXPORT_NAME_LENGTH)
        .ifBlank { DEFAULT_EXPORT_NAME }

private val ILLEGAL_FILE_NAME_CHARS = Regex("[\\\\/:*?\"<>|\\p{Cntrl}]")
private const val MAX_EXPORT_NAME_LENGTH = 80
private const val DEFAULT_EXPORT_NAME = "route"
