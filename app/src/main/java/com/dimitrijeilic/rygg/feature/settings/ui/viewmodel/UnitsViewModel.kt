package com.dimitrijeilic.rygg.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dimitrijeilic.rygg.core.units.MeasurementSystem
import com.dimitrijeilic.rygg.feature.settings.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class UnitsViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {
    val measurementSystem: StateFlow<MeasurementSystem> = settingsRepository.measurementSystem
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MeasurementSystem.METRIC
        )
}
