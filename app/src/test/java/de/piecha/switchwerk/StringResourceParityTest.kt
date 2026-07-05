package de.piecha.switchwerk

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.assertEquals
import org.junit.Test
import org.w3c.dom.Element

class StringResourceParityTest {
    private val resourceFiles = mapOf(
        "default" to File("src/main/res/values/strings.xml"),
        "English" to File("src/main/res/values-en/strings.xml"),
        "German" to File("src/main/res/values-de/strings.xml"),
    )

    @Test
    fun localizedStringResourcesMatchDefaultKeysAttributesAndPlaceholders() {
        val resources = resourceFiles.mapValues { (_, file) -> parseResources(file) }
        val defaults = requireNotNull(resources["default"])

        resources.filterKeys { it != "default" }.forEach { (language, localized) ->
            assertEquals("Resource keys differ for $language", defaults.keys, localized.keys)

            defaults.forEach { (key, defaultResource) ->
                val localizedResource = requireNotNull(localized[key])
                assertEquals(
                    "Attributes differ for $language resource $key",
                    defaultResource.attributes,
                    localizedResource.attributes,
                )
                assertEquals(
                    "Format placeholders differ for $language resource $key",
                    defaultResource.placeholders,
                    localizedResource.placeholders,
                )
            }
        }
    }

    private fun parseResources(file: File): Map<ResourceKey, Resource> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val elements = document.documentElement.childNodes

        return buildMap {
            for (index in 0 until elements.length) {
                val element = elements.item(index) as? Element ?: continue
                val name = element.getAttribute("name").takeIf(String::isNotBlank) ?: continue
                val key = ResourceKey(element.tagName, name)
                check(put(key, element.toResource()) == null) {
                    "Duplicate resource $key in ${file.path}"
                }
            }
        }
    }

    private fun Element.toResource(): Resource {
        val relevantAttributes = attributes.let { attributes ->
            buildMap {
                for (index in 0 until attributes.length) {
                    val attribute = attributes.item(index)
                    if (attribute.nodeName != "name") {
                        put(attribute.nodeName, attribute.nodeValue)
                    }
                }
            }
        }
        val placeholders = FORMAT_PLACEHOLDER.findAll(textContent).map { it.value }.sorted().toList()
        return Resource(relevantAttributes, placeholders)
    }

    private data class ResourceKey(val type: String, val name: String)

    private data class Resource(
        val attributes: Map<String, String>,
        val placeholders: List<String>,
    )

    private companion object {
        val FORMAT_PLACEHOLDER = Regex("%(?:\\d+\\$)?(?:\\.\\d+)?[a-zA-Z%]")
    }
}
