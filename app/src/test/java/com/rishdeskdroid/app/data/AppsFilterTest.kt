package com.rishdeskdroid.app.data

import org.junit.Assert.assertEquals
import org.junit.Test

class AppsFilterTest {

    private val apps = listOf(
        AppInfo("YouTube", "com.google.youtube", "Main"),
        AppInfo("Browser", "com.example.browser", "Main"),
        AppInfo("Camera", "com.example.camera", "Main"),
        AppInfo("YouTube Music", "com.google.music", "Main"),
    )

    @Test
    fun searchMatchesLabelsCaseInsensitively() {
        val result = filterAndSortApps(
            apps = apps,
            query = "yOuTuBe",
            hiddenPackages = emptySet(),
            showHidden = false,
            favoritePackages = emptySet(),
            sort = LauncherPrefs.SORT_NAME_ASC,
        )
        assertEquals(listOf("YouTube", "YouTube Music"), result.map { it.label })
    }

    @Test
    fun searchCanMatchPackageNames() {
        val result = filterAndSortApps(
            apps = apps,
            query = "com.example",
            hiddenPackages = emptySet(),
            showHidden = false,
            favoritePackages = emptySet(),
            sort = LauncherPrefs.SORT_NAME_ASC,
        )
        assertEquals(listOf("Browser", "Camera"), result.map { it.label })
    }

    @Test
    fun hiddenAppsAreExcludedUntilHiddenViewIsSelected() {
        val hidden = setOf("com.example.camera")
        val normal = filterAndSortApps(apps, "", hidden, false, emptySet(), LauncherPrefs.SORT_NAME_ASC)
        val hiddenView = filterAndSortApps(apps, "", hidden, true, emptySet(), LauncherPrefs.SORT_NAME_ASC)
        assertEquals(listOf("Browser", "YouTube", "YouTube Music"), normal.map { it.label })
        assertEquals(listOf("Camera"), hiddenView.map { it.label })
    }

    @Test
    fun favoritesSortPlacesFavoritesFirstAndKeepsAlphabeticalOrder() {
        val result = filterAndSortApps(
            apps = apps,
            query = "",
            hiddenPackages = emptySet(),
            showHidden = false,
            favoritePackages = setOf("com.google.music", "com.example.browser"),
            sort = LauncherPrefs.SORT_FAVORITES,
        )
        assertEquals(listOf("Browser", "YouTube Music", "Camera", "YouTube"), result.map { it.label })
    }

    @Test
    fun descendingSortUsesReverseAlphabeticalOrder() {
        val result = filterAndSortApps(apps, "", emptySet(), false, emptySet(), LauncherPrefs.SORT_NAME_DESC)
        assertEquals(listOf("YouTube Music", "YouTube", "Camera", "Browser"), result.map { it.label })
    }
}
