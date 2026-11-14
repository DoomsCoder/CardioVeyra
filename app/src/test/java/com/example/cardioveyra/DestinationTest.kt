package com.example.cardioveyra

import com.example.cardioveyra.ui.navigation.Destination
import org.junit.Test
import org.junit.Assert.*

class DestinationTest {

    @Test
    fun destinationEntries_areNotNull() {
        val entries = Destination.entries
        assertNotNull(entries)
        assertEquals(3, entries.size)
        entries.forEach { destination ->
            assertNotNull("Destination entry should not be null", destination)
        }
    }
}