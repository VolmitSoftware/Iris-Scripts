package com.volmit.iris.core.scripting.kotlin.runner

import kotlinx.coroutines.runBlocking
import org.dom4j.Document
import org.dom4j.Element
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.dependencies.CompoundDependenciesResolver
import kotlin.script.experimental.dependencies.FileSystemDependenciesResolver
import kotlin.script.experimental.dependencies.maven.MavenDependenciesResolver
import kotlin.script.experimental.dependencies.resolveFromScriptSourceAnnotations
import kotlin.script.experimental.jvm.updateClasspath

internal fun <T, R> ResultWithDiagnostics<T>.map(transformer: (T) -> R): ResultWithDiagnostics<R> = when (this) {
    is ResultWithDiagnostics.Success -> ResultWithDiagnostics.Success(transformer(value), reports)
    is ResultWithDiagnostics.Failure -> this
}

internal fun EvaluationResult.valueOrNull() = returnValue.valueOrNull()
internal fun ResultValue.valueOrNull(): Any? =
    when (this) {
        is ResultValue.Value -> value
        else -> null
    }


internal val resolver = CompoundDependenciesResolver(FileSystemDependenciesResolver(), MavenDependenciesResolver())
internal fun configureMavenDepsOnAnnotations(context: ScriptConfigurationRefinementContext): ResultWithDiagnostics<ScriptCompilationConfiguration> {
    val annotations = context.collectedData?.get(ScriptCollectedData.collectedAnnotations)?.takeIf { it.isNotEmpty() }
        ?: return context.compilationConfiguration.asSuccess()
    return runBlocking {
        resolver.resolveFromScriptSourceAnnotations(annotations)
    }.onSuccess {
        context.compilationConfiguration.with {
            updateClasspath(it)
        }.asSuccess()
    }
}

internal fun Collection<File>.format(projectDir: File): Collection<String> {
    val projectDir = projectDir.absolutePath
    val home = File(System.getProperty("user.home")).absolutePath
    return map { format(it, projectDir, home) }.toSet()
}

private fun format(file: File, projectDir: String, home: String): String {
    val path = file.absolutePath
    return when {
        path.startsWith(projectDir) -> "\$PROJECT_DIR$/${path.substring(projectDir.length + 1)}"
        path.startsWith(home) -> "\$USER_HOME$/${path.substring(home.length + 1)}"
        else -> path
    }
}

internal fun Document.addScriptTemplateEntries(
    section: String,
    entries: Collection<String>
): Boolean {
    val list = rootElement.selectOrCreate("component[@name='ScriptTemplatesDefinitionsFromDependenciesStorage']", "component", "name" to "ScriptTemplatesDefinitionsFromDependenciesStorage")
        .selectOrCreate("option[@name='$section']", "option", "name" to section)
        .selectOrCreate("list", "list")

    val missing = entries.filterNot(list.selectNodes("option")
        .map { it.valueOf("@value") }::contains)

    for (entry in missing) {
        list.addElement("option")
            .addAttribute("value", entry)
    }

    return missing.isNotEmpty()
}

internal fun Document.addScriptDefinitions(
    entries: Collection<String>
): Boolean {

    val list = rootElement.selectOrCreate("component[@name='KotlinScriptingSettings']", "component", "name" to "KotlinScriptingSettings")
        .selectOrCreate("option[@name='scriptDefinitions']", "option", "name" to "scriptDefinitions")
        .selectOrCreate("list", "list")

    val missing = entries.filterNot(list.selectNodes("ScriptDefinitionSetting/option[@name='definitionId']")
        .map { it.valueOf("@value") }::contains)

    for (entry in missing) {
        list.addElement("ScriptDefinitionSetting")
            .addElement("option")
            .addAttribute("name", "definitionId")
            .addAttribute("value", entry)
    }

    return missing.isNotEmpty()
}

internal fun Element.selectOrCreate(path: String, type: String, vararg attributes: Pair<String, String>) =
    selectSingleNode(path) as Element? ?:
    addElement(type).apply {
        for ((k, v) in attributes) {
            addAttribute(k, v)
        }
    }