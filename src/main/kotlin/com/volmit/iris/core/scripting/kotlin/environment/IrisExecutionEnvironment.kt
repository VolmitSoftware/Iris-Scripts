package com.volmit.iris.core.scripting.kotlin.environment

import com.volmit.iris.core.loader.IrisRegistrant
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.base.EngineScript
import com.volmit.iris.core.scripting.kotlin.base.MobSpawningScript
import com.volmit.iris.core.scripting.kotlin.base.PostMobSpawningScript
import com.volmit.iris.core.scripting.kotlin.base.PreprocessorsScript
import com.volmit.iris.core.scripting.kotlin.runner.Script
import com.volmit.iris.engine.framework.Engine
import org.bukkit.Location
import org.bukkit.entity.Entity
import java.io.File
import kotlin.reflect.KClass
import kotlin.script.experimental.api.valueOrThrow

data class IrisExecutionEnvironment(
    private val engine: Engine
) : IrisSimpleExecutionEnvironment(), ExecutionEnvironment.Engine {
    override fun getEngine() = engine

    override fun buildProject(project: File) {
        TODO("Not yet implemented")
    }

    override fun execute(script: String) =
        execute(script, EngineScript::class.java, emptyMap(), engine)

    override fun evaluate(script: String) =
        evaluate(script, EngineScript::class.java, emptyMap(), engine)

    override fun spawnMob(script: String, location: Location) =
        evaluate(script, MobSpawningScript::class.java, emptyMap(), engine, location)

    override fun postSpawnMob(script: String, location: Location, mob: Entity) =
        execute(script, PostMobSpawningScript::class.java, emptyMap(), engine, location, mob)

    override fun preprocessObject(script: String, `object`: IrisRegistrant) =
        execute(script, PreprocessorsScript::class.java, emptyMap(), engine, `object`)

    private fun classPath() = runner.classPath(
        EngineScript::class,
        MobSpawningScript::class,
        PostMobSpawningScript::class,
        PreprocessorsScript::class
    )

    override fun compile0(script: String, type: KClass<*>): Script {
        val loaded = engine.data.scriptLoader.load(script)
        return compileCache.get(script)
            .computeIfAbsent(type) { _ -> runner.compileText(type, loaded.source, script) }
            .valueOrThrow()
    }
}