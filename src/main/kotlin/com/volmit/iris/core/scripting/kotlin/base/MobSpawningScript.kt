package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.engine.framework.Engine
import org.bukkit.Location
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(fileExtension = "spawn.kts")
abstract class MobSpawningScript(
    engine: Engine,
    val location: Location
) : EngineScript(engine)