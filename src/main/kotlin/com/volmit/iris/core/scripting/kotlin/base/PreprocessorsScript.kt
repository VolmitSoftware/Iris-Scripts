package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.core.loader.IrisRegistrant
import com.volmit.iris.engine.framework.Engine
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(fileExtension = "proc.kts")
abstract class PreprocessorsScript(
    engine: Engine,
    preprocessorObject: IrisRegistrant
) : EngineScript(engine)
