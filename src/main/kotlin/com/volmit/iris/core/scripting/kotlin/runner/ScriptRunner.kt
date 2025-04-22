package com.volmit.iris.core.scripting.kotlin.runner

import de.crazydev22.kts.configureMavenDepsOnAnnotations
import de.crazydev22.kts.map
import de.crazydev22.kts.walker
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.DependsOn
import kotlin.script.experimental.dependencies.Repository
import kotlin.script.experimental.host.createCompilationConfigurationFromTemplate
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.host.withDefaultsFrom
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.dependenciesFromClassContext
import kotlin.script.experimental.jvm.impl.toClassPathOrEmpty
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class ScriptRunner(
    private val host: BasicJvmScriptingHost
) {
    constructor() : this(BasicJvmScriptingHost())

    private val owner = walker.callerClass
    private val configs = ConcurrentHashMap<KClass<*>, ScriptCompilationConfiguration>()
    private val hostConfig = host.baseHostConfiguration.withDefaultsFrom(defaultJvmScriptingHostConfiguration)

    fun classPath(vararg types: KClass<*>) = types.map { createConfig(it) }
        .map { it[ScriptCompilationConfiguration.dependencies] }
        .flatMap { it.toClassPathOrEmpty() }
        .distinct()

    fun compileFile(type: KClass<*>, file: File) = compile(type, file.toScriptSource())

    fun compileText(type: KClass<*>, raw: String, name: String? = null) = compile(type, raw.toScriptSource(name))

    fun clearConfigurations() = configs.clear()

    private fun compile(
        type: KClass<*>,
        code: SourceCode
    ): ResultWithDiagnostics<Script> = host.runInCoroutineContext {
        host.compiler(code, configs.computeIfAbsent(type, ::createConfig))
            .map { CachedScript(it, host, hostConfig) }
    }

    private fun createConfig(type: KClass<*>) = createCompilationConfigurationFromTemplate(
        KotlinType(type),
        hostConfig,
        type
    ) {
        jvm {
            dependenciesFromClassContext(type, wholeClasspath = true)
            dependenciesFromClassContext(owner.kotlin, wholeClasspath = true)
        }

        refineConfiguration {
            onAnnotations(DependsOn::class, Repository::class, handler = ::configureMavenDepsOnAnnotations)
        }
    }
}
