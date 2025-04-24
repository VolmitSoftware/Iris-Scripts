package com.volmit.iris.core.scripting.kotlin.environment

import com.volmit.iris.Iris
import com.volmit.iris.core.IrisSettings
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.base.*
import com.volmit.iris.core.scripting.kotlin.runner.Script
import com.volmit.iris.core.scripting.kotlin.runner.ScriptRunner
import com.volmit.iris.core.scripting.kotlin.runner.addScriptDefinitions
import com.volmit.iris.core.scripting.kotlin.runner.addScriptTemplateEntries
import com.volmit.iris.core.scripting.kotlin.runner.format
import com.volmit.iris.core.scripting.kotlin.runner.valueOrNull
import com.volmit.iris.util.collection.KMap
import com.volmit.iris.util.data.KCache
import com.volmit.iris.util.format.C
import org.dom4j.Document
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.valueOrThrow

open class IrisSimpleExecutionEnvironment : ExecutionEnvironment.Simple {
    protected val compileCache = KCache<String, KMap<KClass<*>, ResultWithDiagnostics<Script>>>({ _ -> KMap() }, IrisSettings.get().performance.cacheSize.toLong())
    protected val runner = ScriptRunner()

    override fun execute(
        script: String
    ) = execute(script, SimpleScript::class.java, null)

    override fun execute(
        script: String,
        type: Class<*>,
        vars: Map<String, Any?>?
    ) {
        Iris.debug("Execute Script (void) " + C.DARK_GREEN + script)
        evaluate0(script, type.kotlin, vars)
    }

    override fun evaluate(
        script: String
    ): Any? = evaluate(script, SimpleScript::class.java, null)

    override fun evaluate(
        script: String,
        type: Class<*>,
        vars: Map<String, Any?>?
    ): Any? {
        Iris.debug("Execute Script (for result) " + C.DARK_GREEN + script)
        return evaluate0(script, type.kotlin, vars)
    }

    override fun close() {
        compileCache.invalidate()
        runner.clearConfigurations()
    }

    protected open fun compile(script: String, type: KClass<*>) =
        compileCache.get(script)
        .computeIfAbsent(type) { _ -> runner.compileText(type, script) }
        .valueOrThrow()

    private fun evaluate0(name: String, type: KClass<*>, properties: Map<String, Any?>? = null): Any? {
        val current = Thread.currentThread()
        val loader = current.contextClassLoader
        current.contextClassLoader = this.javaClass.classLoader
        try {
            return compile(name, type)
                .evaluate(properties)
                .valueOrThrow()
                .valueOrNull()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        current.contextClassLoader = loader

        return null
    }

    override fun configureProject(projectDir: File, workspace: Document): Boolean {
        projectDir.mkdirs()
        val libs = runner.classPath(
            EngineScript::class,
            MobSpawningScript::class,
            PostMobSpawningScript::class,
            PreprocessorScript::class
        ).sortedBy { it.absolutePath }

        File(projectDir, "build.gradle.kts")
            .writeText(libs.buildGradle)

        val classpath = workspace.addScriptTemplateEntries("classpath", libs.format(projectDir))
        val templates = workspace.addScriptTemplateEntries("templates", scriptTemplates)
        val definitions = workspace.addScriptDefinitions(scriptTemplates)

        return classpath || templates || definitions
    }

    companion object {
        private val scriptTemplates = listOf(
            "com.volmit.iris.core.scripting.kotlin.base.EngineScript",
            "com.volmit.iris.core.scripting.kotlin.base.MobSpawningScript",
            "com.volmit.iris.core.scripting.kotlin.base.PostMobSpawningScript",
            "com.volmit.iris.core.scripting.kotlin.base.PreprocessorScript"
        )

        private val List<File>.buildGradle
            get() = BASE_GRADLE.replace("<classpath>", joinToString(",\n        ") { "\"${it.escapedPath}\"" })

        private val File.escapedPath
            get() = absolutePath.replace("\\", "\\\\").replace("\"", "\\\"")

        private val BASE_GRADLE = """
            plugins {
                kotlin("jvm") version("2.1.20")
            }

            repositories {
                mavenCentral()
            }

            val script by configurations.creating
            configurations.compileOnly { extendsFrom(script) }
            configurations.kotlinScriptDef { extendsFrom(script) }
            configurations.kotlinScriptDefExtensions { extendsFrom(script) }
            configurations.kotlinCompilerClasspath { extendsFrom(script) }
            configurations.kotlinCompilerPluginClasspath { extendsFrom(script) }

            dependencies {
                add("script", files(
                    <classpath>
                ))
            }""".trimIndent()
    }
}