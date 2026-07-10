package com.example.rygg.feature.auth.domain

import androidx.annotation.DrawableRes
import com.example.rygg.R

enum class Discipline(
    @DrawableRes val iconRes: Int
) {
    HIKE(R.drawable.ic_hike),
    RIDE(R.drawable.ic_bike),
    SKI(R.drawable.ic_ski)
}
