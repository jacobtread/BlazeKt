package com.jacobtread.blaze.processor

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jacobtread.blaze.annotations.PacketHandler
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
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
        logger.info("Processing with BlazeProcessor")

        val functions = resolver.getSymbolsWithAnnotation(PacketHandler::class.qualifiedName!!)
            .filterIsInstance<KSFunctionDeclaration>()

        val classRouteFunctions = HashMap<KSType, ArrayList<KSFunctionDeclaration>>()

        functions.forEach {
            val extensionName = it.extensionReceiver?.resolve()
            val typeName = if (extensionName != null) {
                extensionName
            } else {
                val functionClass = it.closestClassDeclaration()
                functionClass?.asType(emptyList())

            }
            if (typeName != null) {
                classRouteFunctions.getOrPut(typeName) { ArrayList() }
                    .add(it)
            }
        }

        classRouteFunctions.forEach { (clazz, functions) ->
            val classType = clazz.toTypeName()
            val className = clazz.toClassName()
            val simpleClassName = className.simpleName
            logger.info("Processing $simpleClassName")

            val fileSpec = FileSpec
                .builder(className.packageName, "${simpleClassName}RouterExt")
                .addImport("com.jacobtread.blaze", "respond")

            val routeMappings = TreeMap<Int, TreeMap<Int, String>>()

            for (function in functions) {
                val annotation = function.getAnnotationsByType(PacketHandler::class)
                    .firstOrNull() ?: continue
                val extensionName = function.extensionReceiver?.resolve()
                if (extensionName != null) {
                    // Import the extension function
                    fileSpec.addImport(function.packageName.asString(), function.simpleName.asString())
                }

                val parameters = function.parameters
                if (parameters.isEmpty()) {
                    logger.error("Function with PacketHandler annotation has an invalid number of parameters expected 1", function)
                    continue
                }
                val component = annotation.component
                val command = annotation.command
                val functionName = function.simpleName.asString()
                val values = routeMappings.getOrPut(component) { TreeMap { o1, o2 -> o1.compareTo(o2) } }
                values[command] = functionName
            }

            // String builder for building the code string
            val codeBuilder = StringBuilder("when(msg.component) {\n") // Build starts with opening component when statement
            // Iterate over the function mappings outer components
            routeMappings.forEach { (component, fmap) ->
                codeBuilder.append("  ") // Ident for when statement
                    .append(component)
                    .appendLine(" -> when (msg.command) {") // Start next when statement for commands

                fmap.forEach { (command, functionName) ->
                    logger.info("Processing $className -> ($component, $command) -> $functionName")
                    // Append function call for the command
                    codeBuilder.append("    ")
                        .append(command)
                        .append(" -> ")
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

            val routeFunc = FunSpec.builder("routePacket")
                .receiver(classType)
                .addParameter("channel", ClassName("io.netty.channel", "Channel"))
                .addParameter("msg", ClassName("com.jacobtread.blaze.packet", "Packet"))
                .addCode(CodeBlock.of(codeBuilder.toString()))
                .build()
            logger.info("Writing output for $className")

            fileSpec.addFunction(routeFunc)
            val file = fileSpec.build()
            file.writeTo(codeGenerator, true)
        }
        return emptyList()
    }
}
