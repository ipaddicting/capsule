package io.iotconnects.capsule

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.equals.shouldBeEqual

class STableSchemaTests : FeatureSpec({
    feature("STable schema generating") {
        scenario("DDL of super table creating & dropping") {
            val schema = STableSchema(Meter::class)

            val create = schema.create()
            create shouldBeEqual
                """
                |CREATE STABLE IF NOT EXISTS meters (
                |    ts TIMESTAMP,
                |    current FLOAT,
                |    phase FLOAT,
                |    voltage FLOAT
                |) TAGS (
                |    group_id INT,
                |    location TINYINT UNSIGNED,
                |    remarks NCHAR(128)
                |);
                """.trimMargin()

            val drop = schema.drop()
            drop shouldBeEqual "DROP STABLE IF EXISTS meters;"
        }

        scenario("Broken sensor without @Entity") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(BrokenSensor::class) }
            exception.message?.shouldBeEqual("No '@Entity' was declared in the entity class BrokenSensor!")
        }

        scenario("Wrong sensor without @Table") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(NonTableSensor::class) }
            exception.message?.shouldBeEqual("No '@Table' was declared in the entity class NonTableSensor!")
        }

        scenario("Wrong configured sensor without the proper @Id") {
            val exception = shouldThrow<CapsuleExceptions> { STableSchema(WrongIdSensor::class) }
            exception.message?.shouldBeEqual(
                "The '@Id' must be of type 'Timestamp' or 'Instant' in the entity class WrongIdSensor!",
            )
        }
    }
})