package com.dimitrijeilic.rygg.feature.library.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FileTransferTest {
    @Test
    fun exportBaseName_keepsLettersAndDropsIllegalCharacters() {
        assertEquals("Đerdap Kazan", exportBaseName("Đerdap: Kazan?"))
    }

    @Test
    fun exportBaseName_fallsBackWhenNothingIsLeft() {
        assertEquals("route", exportBaseName("///"))
    }
}
