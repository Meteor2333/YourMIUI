package cc.meteormc.yourmiui.processor

import cc.meteormc.yourmiui.api.FeatureHooker
import cc.meteormc.yourmiui.api.annotation.FeatureRegister
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
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.typeNameOf

class FeatureItemProcessor(
    private val generator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {
    private val symbols = mutableListOf<KSClassDeclaration>()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val registerClass = FeatureRegister::class.java.name
        symbols.addAll(resolver.getSymbolsWithAnnotation(registerClass).filterIsInstance<KSClassDeclaration>())
        return emptyList()
    }

    override fun finish() {
        val builder = CodeBlock.Builder()

        builder.add("listOf(«\n")
        for (clazz in symbols) {
            val declarations = clazz.superTypes.map { it.resolve().declaration }
            if (declarations.none { it.qualifiedName?.asString() == FeatureHooker::class.qualifiedName }) {
                continue
            }

            builder.add("%T,\n", clazz.toClassName())
        }
        builder.add("»)")

        val property = PropertySpec.builder("hookers", LIST.parameterizedBy(typeNameOf<FeatureHooker>()))
            .initializer(builder.build())
            .build()
        val type = TypeSpec.objectBuilder("FeatureRegistry")
            .addProperty(property)
            .build()

        FileSpec.builder("cc.meteormc.yourmiui", "FeatureRegistry")
            .addType(type)
            .build()
            .writeTo(
                generator,
                Dependencies(true, *Array(symbols.size) {
                    symbols[it].containingFile!!
                })
            )
    }
}