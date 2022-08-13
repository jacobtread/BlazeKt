@file:JvmName("BlazeProcessorProvider")

package com.jacobtread.blaze.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * BlazeProcessorProvider Provides an instance of the BlazeProcessor
 * based on the environment provided
 *
 * @constructor Create empty BlazeProcessorProvider
 */
class BlazeProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return BlazeProcessor(
            environment.codeGenerator,
            environment.logger
        )
    }
}