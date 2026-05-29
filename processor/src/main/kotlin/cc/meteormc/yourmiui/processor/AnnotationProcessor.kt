package cc.meteormc.yourmiui.processor

import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.EntryClass
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
import cc.meteormc.yourmiui.api.annotation.RequiredScope
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.MAP
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.typeNameOf

class AnnotationProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val featureClasses = mutableListOf<KSClassDeclaration>()
    private val entryClasses = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        featureClasses.addAll(
            resolver.getSymbolsWithAnnotation(FeatureRegister::class.java.name)
                .filterIsInstance<KSClassDeclaration>()
                .filter { clazz ->
                    clazz.superTypes.map {
                        it.resolve().declaration
                    }.any {
                        it.qualifiedName?.asString() == FeatureHooker::class.qualifiedName
                    }
                }
        )

        entryClasses.addAll(
            resolver.getSymbolsWithAnnotation(EntryClass::class.java.name)
                .filterIsInstance<KSClassDeclaration>()
        )
        return emptyList()
    }

    override fun finish() {
        val globalFeatures = mutableListOf<KSClassDeclaration>()
        val featuresMap = mutableMapOf<String, MutableList<KSClassDeclaration>>()
        for (clazz in featureClasses) {
            val logBuilder = StringBuilder()
            logBuilder.append("Found feature: ${clazz.simpleName.getShortName()}")

            val scope = clazz.annotations.filter {
                it.annotationType.resolve().declaration.qualifiedName?.asString() == RequiredScope::class.qualifiedName
            }.mapNotNull {
                it.arguments.firstOrNull()?.value as? String?
            }.toList()

            if (scope.isEmpty()) {
                logBuilder.append(", global feature")
                globalFeatures.add(clazz)
                continue
            } else {
                logBuilder.append(", scope: ${scope.joinToString()}")
                scope.forEach {
                    featuresMap.getOrPut(it) { mutableListOf() }.add(clazz)
                }
            }

            logger.info(logBuilder.toString())
        }
        featuresMap.values.forEach { it.addAll(globalFeatures) }

        val entries = mutableListOf<String>()
        for (clazz in entryClasses) {
            logger.info("Found entry: ${clazz.simpleName.getShortName()}")
            clazz.qualifiedName?.let { entries.add(it.asString()) }
        }

        generateFeatureRegistry(featuresMap)
        generateScopeList(featuresMap.keys)
        generateEntryList(entries)
    }

    private fun generateFeatureRegistry(featuresMap: Map<String, Collection<KSClassDeclaration>>) {
        val builder = CodeBlock.Builder()
        builder.add("mapOf(«\n")
        for ((scope, features) in featuresMap.entries) {
            builder.beginControlFlow("%S to buildList<FeatureHooker>", scope)
            features.forEach { builder.add("add(%T)\n", it.toClassName()) }
            builder.endControlFlow()
            builder.add(",\n")
        }
        builder.add("»)")

        val featuresType = MAP.parameterizedBy(
            typeNameOf<String>(),
            LIST.parameterizedBy(typeNameOf<FeatureHooker>())
        )
        val property = PropertySpec.builder("features", featuresType)
            .initializer(builder.build())
            .build()
        val body = TypeSpec.objectBuilder("FeatureRegistry")
            .addProperty(property)
            .build()

        FileSpec.builder("cc.meteormc.yourmiui", "FeatureRegistry")
            .indent(" ".repeat(4))
            .addType(body)
            .build()
            .writeTo(
                generator,
                Dependencies(true, *featureClasses.mapNotNull { it.containingFile }.toTypedArray())
            )
    }

    private fun generateScopeList(scopes: Collection<String>) {
        logger.info("generateScopeList: $scopes")
        // todo: for lsposed api
//        generator.createNewFileByPath(
//            Dependencies.ALL_FILES,
//            "META-INF/xposed/scope",
//            "list"
//        ).writer().use {
//            it.write(scopes.joinToString("\n"))
//        }

        generator.createNewFileByPath(
            Dependencies.ALL_FILES,
            "res/values/array",
            "xml"
        ).writer().use {
            it.write(
                """
                <resources>
                    <string-array name="module_scopes">

                """.trimIndent()
            )

            scopes.forEach { scope ->
                it.write("    <item>$scope</item>\n")
            }

            it.write(
                """
                    </string-array>
                </resources>
                """.trimIndent()
            )
        }
    }

    private fun generateEntryList(entries: Collection<String>) {
        logger.info("generateEntryList: $entries")
        setOf(
            "assets/xposed_init" to ""
            // todo: for lsposed api
//            "META-INF/xposed/java_init" to "list"
        ).map {
            generator.createNewFileByPath(
                Dependencies.ALL_FILES,
                it.first,
                it.second
            )
        }.forEach { file ->
            file.writer().use {
                it.write(entries.joinToString("\n"))
            }
        }
    }
}