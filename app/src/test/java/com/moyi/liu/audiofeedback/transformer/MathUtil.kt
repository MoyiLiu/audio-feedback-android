package com.moyi.liu.audiofeedback.transformer

import java.text.DecimalFormat

//round the number up to integer to avoid floating point
fun Float.roundToTwoDecimals(): Float =
    DecimalFormat("#.##").format(this).toFloat()