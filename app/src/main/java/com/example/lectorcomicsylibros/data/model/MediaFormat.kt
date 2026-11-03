package com.example.lectorcomicsylibros.data.model

enum class MediaFormat(val extension: String, val displayName: String, val isComic: Boolean) {
    CBZ("cbz", "CBZ", true),
    CBR("cbr", "CBR", true),
    EPUB("epub", "EPUB", false),
    PDF("pdf", "PDF", false),
    UNKNOWN("", "Desconocido", false);

    companion object {
        fun fromFileNameOrMime(fileName: String, mimeType: String? = null): MediaFormat {
            val nameLower = fileName.lowercase()
            return when {
                nameLower.endsWith(".cbz") || mimeType == "application/x-cbz" || mimeType == "application/vnd.comicbook+zip" -> CBZ
                nameLower.endsWith(".cbr") || mimeType == "application/x-cbr" || mimeType == "application/vnd.comicbook-rar" -> CBR
                nameLower.endsWith(".epub") || mimeType == "application/epub+zip" -> EPUB
                nameLower.endsWith(".pdf") || mimeType == "application/pdf" -> PDF
                else -> UNKNOWN
            }
        }
    }
}
