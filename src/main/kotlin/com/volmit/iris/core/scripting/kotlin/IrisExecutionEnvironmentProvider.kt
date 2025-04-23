package com.volmit.iris.core.scripting.kotlin

import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.core.scripting.ExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.environment.IrisExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.environment.IrisPackExecutionEnvironment
import com.volmit.iris.core.scripting.kotlin.environment.IrisSimpleExecutionEnvironment
import com.volmit.iris.engine.framework.Engine

class IrisExecutionEnvironmentProvider : ExecutionEnvironment.Provider {
    override fun createEngine(engine: Engine) = IrisExecutionEnvironment(engine)
    override fun createPack(data: IrisData) = IrisPackExecutionEnvironment(data)
    override fun createSimple() = IrisSimpleExecutionEnvironment()
}
