package com.example.model

enum class SidebarDestination(val route: String, val label: String) {
    HOME("home", "Beranda"),
    SEARCH("search", "Pencarian"),
    VOICE_SEARCH("voice_search", "Pencarian Suara"),
    CATEGORIES("categories", "Kategori"),
    FAVORITES("favorites", "Favorit"),
    HISTORY("history", "Riwayat"),
    PLAYLISTS("playlists", "Playlist"),
    SETTINGS("settings", "Pengaturan")
}
