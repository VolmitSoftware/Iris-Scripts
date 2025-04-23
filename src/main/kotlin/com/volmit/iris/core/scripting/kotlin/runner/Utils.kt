package com.volmit.iris.core.scripting.kotlin.runner

import kotlinx.coroutines.runBlocking
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
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

private fun resolve(path: String, projectDir: String, home: String) = File(
    when {
        path.startsWith("\$PROJECT_DIR$") -> path.replaceFirst("\$PROJECT_DIR$", projectDir)
        path.startsWith("\$USER_HOME$") -> path.replaceFirst("\$USER_HOME$", home)
        else -> path
    }
)

internal fun addEntries(
    workspaceFile: File,
    section: String,
    entriesToAdd: Collection<String>
): Boolean {
    var changesMade = false
    val factory = DocumentBuilderFactory.newInstance()
    val builder = factory.newDocumentBuilder()

    val document = if (!workspaceFile.exists()) {
        workspaceFile.parentFile?.mkdirs()

        val newDocument = builder.newDocument()

        val rootElement = newDocument.createElement("project")
        rootElement.setAttribute("version", "4")
        newDocument.appendChild(rootElement)

        val transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(newDocument)
        val result = StreamResult(workspaceFile)
        transformer.transform(source, result)

        changesMade = true
        newDocument
    } else {
        builder.parse(workspaceFile)
    }

    val components = document.getElementsByTagName("component")
    var storageComponent: Element? = null

    for (i in 0 until components.length) {
        val component = components.item(i) as Element
        if (component.getAttribute("name") == "ScriptTemplatesDefinitionsFromDependenciesStorage") {
            storageComponent = component
            break
        }
    }

    if (storageComponent == null) {
        storageComponent = document.createElement("component")
        storageComponent.setAttribute("name", "ScriptTemplatesDefinitionsFromDependenciesStorage")
        document.documentElement.appendChild(storageComponent)
        changesMade = true
    }

    val options = storageComponent.getElementsByTagName("option")
    var collectionOption: Element? = null

    for (i in 0 until options.length) {
        val option = options.item(i) as Element
        if (option.getAttribute("name") == "classpath") {
            collectionOption = option
            break
        }
    }

    if (collectionOption == null) {
        collectionOption = document.createElement("option")
        collectionOption.setAttribute("name", section)
        storageComponent.appendChild(collectionOption)
        changesMade = true
    }

    val listElements = collectionOption.getElementsByTagName("list")
    var listElement: Element? = null

    if (listElements.length > 0) {
        listElement = listElements.item(0) as Element
    }

    if (listElement == null) {
        listElement = document.createElement("list")
        collectionOption.appendChild(listElement)
        changesMade = true
    }

    val existingEntries = mutableSetOf<String>()
    val optionElements = listElement.getElementsByTagName("option")

    for (i in 0 until optionElements.length) {
        val option = optionElements.item(i) as Element
        val value = option.getAttribute("value")
        existingEntries.add(value)
    }

    for (entry in entriesToAdd) {
        if (!existingEntries.contains(entry)) {
            val newOption = document.createElement("option")
            newOption.setAttribute("value", entry)
            listElement.appendChild(newOption)
            changesMade = true
        }
    }

    if (changesMade) {
        val transformer = TransformerFactory.newInstance().newTransformer()
        val source = DOMSource(document)
        val result = StreamResult(workspaceFile)
        transformer.transform(source, result)
    }

    return changesMade
}