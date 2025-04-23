package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.core.scripting.func.BiomeLookup
import com.volmit.iris.engine.IrisComplex
import com.volmit.iris.engine.framework.Engine
import com.volmit.iris.engine.`object`.IrisDimension
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.isStandalone
import kotlin.script.experimental.api.providedProperties

@KotlinScript(fileExtension = "engine.kts", compilationConfiguration = EngineScriptDefinition::class)
abstract class EngineScript

object EngineScriptDefinition : ScriptCompilationConfiguration({
    isStandalone(false)
    defaultImports(
        "com.volmit.iris.Iris.info",
        "com.volmit.iris.Iris.debug",
        "com.volmit.iris.Iris.warn",
        "com.volmit.iris.Iris.error"
    )

    providedProperties(
        "engine" to Engine::class,
        "data" to IrisData::class,
        "complex" to IrisComplex::class,
        "seed" to Long::class,
        "dimension" to IrisDimension::class,
        "biome" to BiomeLookup::class,
    )
}) {
    private fun readResolve(): Any = EngineScriptDefinition
}