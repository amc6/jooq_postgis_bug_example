package com.sharespace.persistence

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PersisterTest : DBTest() {

    private val persister = Persister(dbSessionManager)

    @Test
    fun testCreateExample() {
        val location = Location(1.0, 2.0)
        val record = persister.createExample(location)
        assertEquals(location, record.location.convertToLocation())
    }

    @Test
    fun testGetById() {
        val location = Location(1.0, 2.0)
        val record = persister.createExample(location)
        val fetchedRecord = assertNotNull(persister.getExampleById(record.id))
        assertEquals(location, fetchedRecord.location.convertToLocation())
        assertEquals(record, fetchedRecord)
    }

    @Test
    fun testGetByLocation() {
        val location = Location(1.0, 2.0)
        val record = persister.createExample(location)
        val fetchedRecord = assertNotNull(persister.getExampleByLocation(location))
        assertEquals(location, fetchedRecord.location.convertToLocation())
        assertEquals(record, fetchedRecord)
    }

    @Test
    fun testGetInRadius() {
        val record = persister.createExample(Location(1.0, 2.0))
        val fetchedRecord = assertNotNull(persister.getExampleInRadius(Location(1.0001, 2.0), 1000.0))
        assertEquals(record, fetchedRecord)
    }

    @Test
    fun testGetInRadiusAlternate() {
        val record = persister.createExample(Location(1.0, 2.0))
        val fetchedRecord = assertNotNull(persister.getExampleInRadiusAlternate(Location(1.0001, 2.0), 1000.0))
        assertEquals(record, fetchedRecord)
    }
}