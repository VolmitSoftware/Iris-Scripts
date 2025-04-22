package com.volmit.iris.core.scripting.kotlin.base

import com.volmit.iris.engine.framework.Engine
import org.bukkit.Location
import org.bukkit.entity.Entity
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(fileExtension = "postspawn.kts")
abstract class PostMobSpawningScript(
    engine: Engine,
    location: Location,
    val entity: Entity
) : MobSpawningScript(engine, location)
