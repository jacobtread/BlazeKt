package com.jacobtread.blaze.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jacobtread.blaze.annotations.PacketHandler
import com.jacobtread.blaze.annotations.PacketProcessor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import java.util.*

/**
 * BlazeProcessor Symbol processor for generating routing for blaze packets
 *
 * @property codeGenerator The KSP provided code generator used to generate the new source file
 * @property logger The KSP provided logger used for logging errors while processing
 * @constructor Create empty BlazeProcessor
 */
class BlazeProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    /**
     * process Processes the annotations in the code source and
     * generates the routing functions from them
     *
     * @param resolver The KSP resolver
     * @return Always an empty list
     */
    @OptIn(KspExperimental::class)
    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.info("Processing with BlazeProccessor")
        val classes = resolver.getSymbolsWithAnnotation(PacketProcessor::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()
        classes.forEach { clazz ->
            // Class type and name
            val classType = clazz.asType(emptyList()).toTypeName()
            val className = clazz.simpleName.asString()
            logger.info("Processing $className")
            // Using tree maps to order the components and commands
            val functionMappings = TreeMap<Int, TreeMap<Int, String>> { o1, o2 -> o1.compareTo(o2) }
            for (function in clazz.getAllFunctions()) {
                val annotation = function.getAnnotationsByType(PacketHandler::class)
                    .firstOrNull() ?: continue
                val parameters = function.parameters
                if (parameters.isEmpty()) {
                    logger.error("Function with PacketHandler annotation has an invalid number of parameters expected 1", function)
                    continue
                }
                val component = annotation.component
                val command = annotation.command
                val functionName = function.simpleName.asString()
                val values = functionMappings.getOrPut(component) { TreeMap { o1, o2 -> o1.compareTo(o2) } }
                values[command] = functionName
            }

            // String builder for building the code string
            val codeBuilder = StringBuilder("when(msg.component) {\n") // Build starts with opening component when statement
            // Iterate over the function mappings outer components
            functionMappings.forEach { (component, fmap) ->
                codeBuilder.append("  ") // Ident for when statement
                    .append(component)
                    .appendLine(" -> when (msg.command) {") // Start next when statement for commands

                fmap.forEach { (command, functionName) ->
                    logger.info("Processing $className -> ($component, $command) -> $functionName")
                    // Append function call for the command
                    codeBuilder.append("    ")
                        .append(command)
                        .append(" -> processor.")
                        .append(functionName)
                        .appendLine("(msg)")
                }

                // Append the fallback empty send for other commands
                codeBuilder.appendLine("    else -> channel.write(msg.respond())")
                // Close when statement
                codeBuilder.appendLine("  }")
            }
            // Append the fallback empty send for other components
            codeBuilder.appendLine("  else -> channel.write(msg.respond())")
            // Close when statement
            codeBuilder.appendLine("}")

            /**
             * routeFunc Creates a function that is used for routing this
             * is named route${TheProcesssorClassName} and takes the processor,
             * channel, and packet as its arguments
             */
            val routeFunc = FunSpec.builder("route${className}")
                .addParameter("processor", classType)
                .addParameter("channel", ClassName("io.netty.channel", "Channel"))
                .addParameter("msg", ClassName("com.jacobtread.kme.blaze.packet", "Packet"))
                .addCode(CodeBlock.of(codeBuilder.toString()))
                .build()
            logger.info("Writing output for $className")

            // File specification
            val file = FileSpec
                .builder(clazz.packageName.asString(), "${className}RouterFunction")
                // Import for respond extension function
                .addImport("com.jacobtread.kme.blaze", "respond")
                .addFunction(routeFunc)
                .build()
            // Write the created file
            file.writeTo(codeGenerator, true)
        }
        return emptyList()
    }
}
