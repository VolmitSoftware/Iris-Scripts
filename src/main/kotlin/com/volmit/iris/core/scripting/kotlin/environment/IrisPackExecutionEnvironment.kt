package com.volmit.iris.core.scripting.kotlin.environment

import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.base.EngineScript
import com.volmit.iris.core.scripting.kotlin.base.MobSpawningScript
import com.volmit.iris.core.scripting.kotlin.base.PostMobSpawningScript
import com.volmit.iris.core.scripting.kotlin.base.PreprocessorScript
import com.volmit.iris.core.scripting.kotlin.runner.Script
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.valueOrThrow

open class IrisPackExecutionEnvironment(
    private val data: IrisData
) : IrisSimpleExecutionEnvironment(), ExecutionEnvironment.Pack {

    override fun getData() = data

    override fun buildProject() {
        data.dataFolder.mkdirs()
        File(data.dataFolder, "build.gradle.kts")
            .writeText(buildGradle)
    }

    override fun compile(script: String, type: KClass<*>): Script {
        val loaded = data.scriptLoader.load(script)
        return compileCache.get(script)
            .computeIfAbsent(type) { _ -> runner.compileText(type, loaded.source, script) }
            .valueOrThrow()
    }

    private val buildGradle
        get() = BASE_GRADLE.replace("<classpath>",
            runner.classPath(
                EngineScript::class,
                MobSpawningScript::class,
                PostMobSpawningScript::class,
                PreprocessorScript::class)
                .sortedBy { it.absolutePath }
                .joinToString(",\n        ") { "\"${it.escapedPath}\"" })

    companion object {
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