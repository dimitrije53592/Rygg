package com.dimitrijeilic.rygg.feature.settings.domain

import com.dimitrijeilic.rygg.core.ui.utils.RouteShareLinks

data class OpenSourceLibrary(
    val id: String,
    val name: String,
    val version: String?,
    val licenseNames: List<String>,
    val licenseText: String?,
    val url: String?
)

data class OpenSourceLicenses(
    val fontLicenseText: String,
    val libraries: List<OpenSourceLibrary>
)

// Pages served from the same Firebase Hosting site as share links (public/ in this repo).
object LegalLinks {
    const val PRIVACY_POLICY = "${RouteShareLinks.BASE}/privacy.html"
    const val TERMS = "${RouteShareLinks.BASE}/terms.html"
    const val OPENSTREETMAP_COPYRIGHT = "https://www.openstreetmap.org/copyright"
    const val MAPTILER_COPYRIGHT = "https://www.maptiler.com/copyright/"
}
