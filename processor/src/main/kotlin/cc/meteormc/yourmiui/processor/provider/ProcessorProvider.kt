package cc.meteormc.yourmiui.processor.provider

import cc.meteormc.yourmiui.processor.FeatureItemProcessor
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

class ProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return FeatureItemProcessor(environment.codeGenerator, environment.logger)
    }
}