package com.moyi.liu.audiofeedback.transformer

import com.moyi.liu.audiofeedback.sensor.SensorBoundary

val StubBoundaryTransformer: BoundaryTransformer = { (front, back) ->
    SensorBoundary(front, back)
}