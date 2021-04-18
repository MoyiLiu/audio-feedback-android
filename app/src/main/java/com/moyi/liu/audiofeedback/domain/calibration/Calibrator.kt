package com.moyi.liu.audiofeedback.domain.calibration

import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

interface Calibrator {
    fun countDownAndPrepareSensor(
        countDownSeconds: Long,
        onTick: ((Long) -> Unit)? = null
    ): Completable

    fun startCalibration(
        countDownSeconds: Long,
        onTick: ((Long) -> Unit)? = null
    ): Maybe<CalibrationResult>
}