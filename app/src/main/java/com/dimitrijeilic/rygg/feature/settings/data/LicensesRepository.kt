package com.dimitrijeilic.rygg.feature.settings.data

import android.content.Context
import com.dimitrijeilic.rygg.feature.settings.domain.OpenSourceLibrary
import com.dimitrijeilic.rygg.feature.settings.domain.OpenSourceLicenses
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.util.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

// Library list is generated at build time by the AboutLibraries Gradle plugin, so it always
// matches the shipped dependencies; the bundled Manrope font licence ships as an asset.
class LicensesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun observeLicenses(): Flow<OpenSourceLicenses> = flow {
        val libraries = Libs.Builder().withContext(context).build().libraries
            .map { it.toDomain() }
            .sortedBy { it.name.lowercase() }
        val fontLicense = context.assets.open(FONT_LICENSE_ASSET).bufferedReader().use { it.readText() }
        emit(OpenSourceLicenses(fontLicenseText = fontLicense, libraries = libraries))
    }.flowOn(Dispatchers.IO)

    private companion object {
        const val FONT_LICENSE_ASSET = "licenses/manrope-OFL.txt"
    }
}

private fun Library.toDomain(): OpenSourceLibrary = OpenSourceLibrary(
    id = uniqueId,
    name = name,
    version = artifactVersion,
    licenseNames = licenses.map { it.name },
    licenseText = licenses.firstNotNullOfOrNull { it.licenseContent?.takeIf(String::isNotBlank) },
    url = website ?: licenses.firstNotNullOfOrNull { it.url }
)
