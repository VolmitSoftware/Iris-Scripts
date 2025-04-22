package com.volmit.iris.core.scripting.kotlin.environment

import com.volmit.iris.Iris
import com.volmit.iris.core.IrisSettings
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.base.SimpleScript
import com.volmit.iris.core.scripting.kotlin.runner.Script
import com.volmit.iris.core.scripting.kotlin.runner.ScriptRunner
import com.volmit.iris.util.collection.KMap
import com.volmit.iris.util.data.KCache
import com.volmit.iris.util.format.C
import com.volmit.iris.core.scripting.kotlin.runner.valueOrNull
import kotlin.reflect.KClass
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.valueOrThrow

open class IrisSimpleExecutionEnvironment : ExecutionEnvironment.Simple {
    protected val compileCache = KCache<String, KMap<KClass<*>, ResultWithDiagnostics<Script>>>({ _ -> KMap() }, IrisSettings.get().performance.cacheSize.toLong())
    protected val runner = ScriptRunner()

    override fun execute(
        script: String
    ) = execute(script, SimpleScript::class.java, emptyMap())

    override fun execute(
        script: String,
        type: Class<*>,
        vars: Map<String, Any?>?,
        vararg args: Any?
    ) {
        Iris.debug("Execute Script (void) " + C.DARK_GREEN + script)
        evaluate0(script, type.kotlin, vars ?: emptyMap(), *args)
    }

    override fun evaluate(
        script: String
    ): Any? = evaluate(script, SimpleScript::class.java, emptyMap())

    override fun evaluate(
        script: String,
        type: Class<*>,
        vars: Map<String, Any?>?,
        vararg args: Any?
    ): Any? {
        Iris.debug("Execute Script (for result) " + C.DARK_GREEN + script)
        return evaluate0(script, type.kotlin, vars ?: emptyMap(), *args)
    }

    override fun close() {
        compileCache.invalidate()
        runner.clearConfigurations()
    }

    protected open fun compile0(script: String, type: KClass<*>) =
        compileCache.get(script)
        .computeIfAbsent(type) { _ -> runner.compileText(type, script) }
        .valueOrThrow()

    private fun evaluate0(name: String, type: KClass<*>, properties: Map<String, Any?>, vararg args: Any?): Any? {
        val current = Thread.currentThread()
        val loader = current.contextClassLoader
        current.contextClassLoader = this.javaClass.classLoader
        try {
            return compile0(name, type)
                .evaluate(properties, *args)
                .valueOrThrow()
                .valueOrNull()
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        current.contextClassLoader = loader

        return null
    }
}