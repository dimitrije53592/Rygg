package com.example.rygg.feature.auth.domain

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.rygg.R

enum class Discipline(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int
) {
    HIKE(R.drawable.ic_hike, R.string.discipline_hike),
    RIDE(R.drawable.ic_bike, R.string.discipline_ride),
    SKI(R.drawable.ic_ski, R.string.discipline_ski),
    PADDLE(R.drawable.ic_paddle, R.string.discipline_paddle)
}
