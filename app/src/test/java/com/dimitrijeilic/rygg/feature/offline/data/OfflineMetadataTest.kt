package com.dimitrijeilic.rygg.feature.offline.data

import com.dimitrijeilic.rygg.feature.offline.domain.RegionBounds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OfflineMetadataTest {
    @Test
    fun roundTrips() {
        val metadata = OfflineMetadata(
            name = "Jungfrau",
            bounds = RegionBounds(south = 46.5, west = 7.9, north = 46.7, east = 8.1),
            createdAt = 1L,
            updatedAt = 2L,
            sourceRouteId = 42L
        )

        assertEquals(metadata, OfflineMetadata.decode(metadata.encode()))
    }

    @Test
    fun decode_toleratesEmptyAndForeignData() {
        assertNull(OfflineMetadata.decode(null))
        assertNull(OfflineMetadata.decode(ByteArray(0)))
        assertNull(OfflineMetadata.decode("not json".toByteArray()))
    }
}
