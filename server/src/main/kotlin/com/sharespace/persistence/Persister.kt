package com.sharespace.persistence

import com.sharespace.database.postgis.routines.StDwithin3
import com.sharespace.database.public_.Tables.EXAMPLE
import com.sharespace.database.public_.tables.records.ExampleRecord
import org.jooq.impl.DSL
import org.postgis.Geometry
import org.postgis.Point

class Persister(private val dbSessionManager: DBSessionManager) {

    fun createExample(location: Location): ExampleRecord {
        return dbSessionManager.session()
                .insertInto(EXAMPLE)
                .set(EXAMPLE.LOCATION, location.convertToPoint())
                .returning()
                .fetchOne()
    }

    fun getExampleById(id: Long): ExampleRecord? {
        return dbSessionManager.session()
                .selectFrom(EXAMPLE)
                .where(EXAMPLE.ID.eq(id))
                .fetchOne()
    }

    fun getExampleByLocation(location: Location): ExampleRecord? {
        return dbSessionManager.session()
                .selectFrom(EXAMPLE)
                .where(EXAMPLE.LOCATION.eq(location.convertToPoint()))
                .limit(1)
                .fetchOne()
    }

    fun getExampleInRadius(location: Location, radius: Double):ExampleRecord? {
        val routine = StDwithin3()
        routine.set__1(EXAMPLE.LOCATION)
        routine.set__2(location.convertToPoint())
        routine.set__3(radius)
        return dbSessionManager.session()
                .selectFrom(EXAMPLE)
                .where(routine.asField())
                .limit(1)
                .fetchOne()
    }

    fun getExampleInRadiusAlternate(location: Location, radius: Double):ExampleRecord? {
        return dbSessionManager.session()
                .selectFrom(EXAMPLE)
                .where(DSL.sql("ST_DWithin(location, ?::geography, ?)", location.convertToPoint().toString(), radius))
                .limit(1)
                .fetchOne()
    }
}

data class Location(val lat: Double, val lon: Double)

fun Location.convertToPoint(): Point {
    return Point(lon, lat).also { it.setSrid(4326) }
}

fun Geometry.convertToLocation(): Location {
    if (this !is Point) throw IllegalStateException()
    return Location(y, x)
}