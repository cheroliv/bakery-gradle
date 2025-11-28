package com.cheroliv.bakery

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * DSL extension for bakery configuration.
 */
open class BakeryExtension @Inject constructor(objects: ObjectFactory) {
    val configPath: Property<String> = objects.property(String::class.java)
}
