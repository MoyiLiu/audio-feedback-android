package com.moyi.liu.audiofeedback.domain.model

data class PowerAccumulatorConfig(
    val intakePerSecond: Int,
    val powerCap: Float = 100f
){
    val intakeIntervalMillis = 1000L / intakePerSecond
}
