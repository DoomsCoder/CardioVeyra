package com.example.cardioveyra.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Destination : NavKey {

    val showSystemBars: Boolean
        get() = true

    @Serializable
    data object Cardio : Destination
    @Serializable
    data object Stats : Destination
    @Serializable
    data object Settings : Destination
    @Serializable
    data object Onboarding : Destination {
        override val showSystemBars: Boolean
            get() = false
    }
    @Serializable
    data object LiveScan : Destination {
        override val showSystemBars: Boolean
            get() = false
    }
    
    companion object {
        val entries: List<Destination>
            get() = listOf(Cardio, Stats, Settings)
    }
}