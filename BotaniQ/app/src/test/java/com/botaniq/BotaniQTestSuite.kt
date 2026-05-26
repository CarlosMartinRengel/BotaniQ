package com.botaniq

import org.junit.runner.RunWith
import org.junit.runners.Suite

/**
 * Suite de pruebas general de BotaniQ.
 * Ejecuta todas las clases de pruebas unitarias definidas en [Suite.SuiteClasses].
 */
@RunWith(Suite::class)
@Suite.SuiteClasses(
    WateringCalculatorTest::class,
    DateUtilTest::class,
    CameraUITest::class,
    PlantFormStateTest::class
    // Añade aquí cualquier otra clase de test que vayas creando
)
class BotaniQTestSuite
