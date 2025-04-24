package com.volmit.iris.core.scripting.kotlin.environment

import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.runner.Script
import kotlin.reflect.KClass
import kotlin.script.experimental.api.valueOrThrow

open class IrisPackExecutionEnvironment(
    private val data: IrisData
) : IrisSimpleExecutionEnvironment(), ExecutionEnvironment.Pack {

    override fun getData() = data

    override fun compile(script: String, type: KClass<*>): Script {
        val loaded = data.scriptLoader.load(script)
        return compileCache.get(script)
            .computeIfAbsent(type) { _ -> runner.compileText(type, loaded.source, script) }
            .valueOrThrow()
    }
}