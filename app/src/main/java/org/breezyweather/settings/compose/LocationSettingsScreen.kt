/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.settings.compose

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import org.breezyweather.R
import org.breezyweather.common.extensions.openApplicationDetailsSettings
import org.breezyweather.common.preference.EditTextPreference
import org.breezyweather.common.preference.ListPreference
import org.breezyweather.common.source.ConfigurableSource
import org.breezyweather.common.source.LocationSource
import org.breezyweather.common.utils.helpers.SnackbarHelper
import org.breezyweather.settings.SettingsManager
import org.breezyweather.settings.preference.bottomInsetItem
import org.breezyweather.settings.preference.clickablePreferenceItem
import org.breezyweather.settings.preference.composables.EditTextPreferenceView
import org.breezyweather.settings.preference.composables.ListPreferenceView
import org.breezyweather.settings.preference.composables.PreferenceScreen
import org.breezyweather.settings.preference.composables.PreferenceView
import org.breezyweather.settings.preference.composables.SectionFooter
import org.breezyweather.settings.preference.composables.SectionHeader
import org.breezyweather.settings.preference.editTextPreferenceItem
import org.breezyweather.settings.preference.listPreferenceItem
import org.breezyweather.settings.preference.sectionFooterItem
import org.breezyweather.settings.preference.sectionHeaderItem

@Composable
fun LocationSettingsScreen(
    context: Activity,
    locationSources: List<LocationSource>,
    accessCoarseLocationPermissionState: PermissionState,
    accessFineLocationPermissionState: PermissionState,
    accessBackgroundLocationPermissionState: PermissionState,
    paddingValues: PaddingValues,
) = PreferenceScreen(paddingValues = paddingValues) {
    sectionHeaderItem(R.string.settings_location_section_general)
    listPreferenceItem(R.string.settings_location_service) { id ->
        ListPreferenceView(
            title = context.getString(id),
            selectedKey = SettingsManager.getInstance(context).locationSource,
            valueArray = locationSources.map { it.id }.toTypedArray(),
            nameArray = locationSources.map { it.name }.toTypedArray(),
            summary = { _, value -> locationSources.firstOrNull { it.id == value }?.name },
            onValueChanged = { sourceId ->
                SettingsManager.getInstance(context).locationSource = sourceId
            }
        )
    }
    sectionFooterItem(R.string.settings_location_section_general)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        sectionHeaderItem(R.string.location_service_native)
        clickablePreferenceItem(R.string.settings_location_access_switch_title) { id ->
            PreferenceView(
                titleId = id,
                summaryId = if (accessCoarseLocationPermissionState.status == PermissionStatus.Granted) {
                    R.string.settings_location_access_switch_summaryOn
                } else {
                    R.string.settings_location_access_switch_summaryOff
                },
                onClick = {
                    if (accessCoarseLocationPermissionState.status != PermissionStatus.Granted) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            accessCoarseLocationPermissionState.launchPermissionRequest()
                        } else {
                            context.openApplicationDetailsSettings()
                        }
                    } else {
                        SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                    }
                }
            )
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            clickablePreferenceItem(R.string.settings_location_access_background_title) { id ->
                PreferenceView(
                    titleId = id,
                    summaryId = if (accessBackgroundLocationPermissionState.status == PermissionStatus.Granted) {
                        R.string.settings_location_access_background_summaryOn
                    } else {
                        R.string.settings_location_access_background_summaryOff
                    },
                    enabled = accessCoarseLocationPermissionState.status == PermissionStatus.Granted,
                    onClick = {
                        if (accessBackgroundLocationPermissionState.status != PermissionStatus.Granted) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                                accessBackgroundLocationPermissionState.launchPermissionRequest()
                            } else {
                                context.openApplicationDetailsSettings()
                            }
                        } else {
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                        }
                    }
                )
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clickablePreferenceItem(R.string.settings_location_access_precise_title) { id ->
                PreferenceView(
                    titleId = id,
                    summaryId = if (accessFineLocationPermissionState.status == PermissionStatus.Granted) {
                        R.string.settings_location_access_precise_summaryOn
                    } else {
                        R.string.settings_location_access_precise_summaryOff
                    },
                    enabled = accessCoarseLocationPermissionState.status == PermissionStatus.Granted,
                    onClick = {
                        if (accessFineLocationPermissionState.status != PermissionStatus.Granted) {
                            if (ActivityCompat.shouldShowRequestPermissionRationale(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                accessFineLocationPermissionState.launchPermissionRequest()
                            } else {
                                context.openApplicationDetailsSettings()
                            }
                        } else {
                            SnackbarHelper.showSnackbar(context.getString(R.string.settings_location_access_permission_already_granted))
                        }
                    }
                )
            }
        }
        sectionFooterItem(R.string.location_service_native)
    }

    // TODO: Duplicate code from weather sources
    locationSources.filterIsInstance<ConfigurableSource>().forEach { preferenceSource ->
        item(key = "header_${preferenceSource.id}") {
            SectionHeader(title = preferenceSource.name)
        }
        preferenceSource.getPreferences(context).forEach { preference ->
            when (preference) {
                is ListPreference -> {
                    listPreferenceItem(preference.titleId) { id ->
                        ListPreferenceView(
                            titleId = id,
                            selectedKey = preference.selectedKey,
                            valueArrayId = preference.valueArrayId,
                            nameArrayId = preference.nameArrayId,
                            onValueChanged = preference.onValueChanged,
                        )
                    }
                }
                is EditTextPreference -> {
                    editTextPreferenceItem(preference.titleId) { id ->
                        EditTextPreferenceView(
                            titleId = id,
                            summary = preference.summary,
                            content = preference.content,
                            onValueChanged = preference.onValueChanged
                        )
                    }
                }
            }
        }
        item(key = "footer_${preferenceSource.id}") {
            SectionFooter()
        }
    }

    bottomInsetItem()
}