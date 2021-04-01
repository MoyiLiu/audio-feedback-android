package com.moyi.liu.audiofeedback.transformer

import java.text.DecimalFormat

//round the number up to two decimals to avoid floating point
fun Float.roundToTwoDecimals(): Float =
    DecimalFormat("#.##").format(this).toFloat()