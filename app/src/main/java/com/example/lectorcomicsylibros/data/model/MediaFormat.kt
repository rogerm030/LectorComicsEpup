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
            val mimeLower = mimeType?.lowercase()
            return when {
                nameLower.endsWith(".cbz") || nameLower.endsWith(".zip") ||
                        mimeLower == "application/x-cbz" || mimeLower == "application/vnd.comicbook+zip" || mimeLower == "application/zip" -> CBZ
                nameLower.endsWith(".cbr") || nameLower.endsWith(".rar") ||
                        mimeLower == "application/x-cbr" || mimeLower == "application/vnd.comicbook-rar" || mimeLower == "application/x-rar-compressed" -> CBR
                nameLower.endsWith(".epub") || mimeLower == "application/epub+zip" -> EPUB
                nameLower.endsWith(".pdf") || mimeLower == "application/pdf" -> PDF
                else -> UNKNOWN
            }
        }
    }
}
