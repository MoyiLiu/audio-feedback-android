package com.moyi.liu.audiofeedback.domain.calibration

import com.moyi.liu.audiofeedback.domain.model.CalibrationResult
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Maybe

interface Calibrator {
    fun countDownAndPrepareSensor(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)? = null
    ): Completable

    fun startCalibration(
        countDownSeconds: Int,
        onTick: ((Int) -> Unit)? = null
    ): Maybe<CalibrationResult>
}