package com.nuvio.tv.ui.screens.home

import android.content.Context
import com.nuvio.tv.domain.model.Collection
import com.nuvio.tv.domain.model.PosterShape
import java.io.IOException
import java.util.Locale

internal class TitaniumCollectionArtwork private constructor(
    private val coversByRow: Map<String, Map<String, Cover>>
) {
    private data class Cover(val imageUrl: String, val focusUrl: String?, val hideTitle: Boolean)

    fun applyTo(collection: Collection): Collection {
        val title = normalized(collection.title).removePrefix("titanium x ")
        val covers = coversByRow[rowAliases[title] ?: title] ?: return collection
        return collection.copy(folders = collection.folders.map { folder ->
            val artwork = covers[normalized(folder.title)] ?: return@map folder
            folder.copy(
                coverImageUrl = artwork.imageUrl,
                focusGifUrl = artwork.focusUrl,
                focusGifEnabled = artwork.focusUrl != null && folder.focusGifEnabled,
                hideTitle = artwork.hideTitle,
                tileShape = PosterShape.LANDSCAPE
            )
        })
    }

    companion object {
        private val nonAlphanumeric = Regex("[^a-z0-9]+")
        private val rowAliases = mapOf(
            "genre" to "genres",
            "studios" to "studios franchises",
            "franchises" to "boxsets collections",
            "film collections" to "boxsets collections",
            "boxsets" to "boxsets collections",
            "by decade" to "decades",
            "legendary directors" to "directors",
            "streaming platforms" to "streaming",
            "streaming services" to "streaming",
            "new popular" to "new movies",
            "trending popular" to "new movies"
        )

        private fun normalized(title: String): String =
            title.lowercase(Locale.ROOT).replace(nonAlphanumeric, " ").trim()

        fun load(context: Context): TitaniumCollectionArtwork {
            val covers = mutableMapOf<String, MutableMap<String, Cover>>()
            try {
                context.assets.open("titanium-collection-art.tsv").bufferedReader().useLines { lines ->
                    lines.forEach { line ->
                        val fields = line.split('\t')
                        if (fields.size == 5) {
                            covers.getOrPut(fields[0]) { mutableMapOf() }[fields[1]] = Cover(
                                imageUrl = fields[2],
                                focusUrl = fields[3].ifBlank { null },
                                hideTitle = fields[4] == "1"
                            )
                        }
                    }
                }
            } catch (_: IOException) {
                return TitaniumCollectionArtwork(emptyMap())
            }
            return TitaniumCollectionArtwork(covers)
        }
    }
}
