package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.Iris
import com.volmit.iris.core.loader.IrisData
import com.volmit.iris.engine.IrisComplex
import com.volmit.iris.engine.framework.Engine
import com.volmit.iris.engine.`object`.IrisBiome
import com.volmit.iris.engine.`object`.IrisDimension
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(fileExtension = "engine.kts")
abstract class EngineScript(
    val engine: Engine
) {
    val data: IrisData get() = engine.data
    val complex: IrisComplex get() = engine.complex
    val seed: Long get() = engine.seedManager.seed
    val dimension: IrisDimension get() = engine.dimension

    fun getBiomeAt(x: Int, z: Int): IrisBiome = engine.getSurfaceBiome(x, z)

    fun info(log: String, vararg args: Any?) = Iris.info(log, *args)
    fun debug(log: String, vararg args: Any?) = Iris.debug(log.format(*args))
    fun warn(log: String, vararg args: Any?) = Iris.warn(log, *args)
    fun error(log: String, vararg args: Any?) = Iris.error(log, *args)
}