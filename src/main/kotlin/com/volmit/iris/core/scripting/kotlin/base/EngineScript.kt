package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.core.scripting.func.BiomeLookup
import com.volmit.iris.core.scripting.kotlin.SlimApp
import com.volmit.iris.core.scripting.kotlin.runner.configureMavenDepsOnAnnotations
import com.volmit.iris.engine.IrisComplex
import com.volmit.iris.engine.framework.Engine
import com.volmit.iris.engine.`object`.IrisDimension
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.isStandalone
import kotlin.script.experimental.api.providedProperties
import kotlin.script.experimental.api.refineConfiguration
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.jvm

@KotlinScript(fileExtension = "engine.kts", compilationConfiguration = EngineScriptDefinition::class)
abstract class EngineScript

object EngineScriptDefinition : ScriptCompilationConfiguration({
    SlimApp.load()

    isStandalone(false)
    defaultImports(
        "kotlin.script.experimental.dependencies.DependsOn",
        "kotlin.script.experimental.dependencies.Repository",
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

    jvm {
        dependenciesFromClassContext(EngineScript::class, wholeClasspath = true)
    }

    refineConfiguration {
        onAnnotations(DependsOn::class, Repository::class, handler = ::configureMavenDepsOnAnnotations)
    }
}) {

    private fun readResolve(): Any = EngineScriptDefinition
}