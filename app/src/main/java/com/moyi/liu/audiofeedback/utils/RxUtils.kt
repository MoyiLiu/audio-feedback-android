package com.moyi.liu.audiofeedback.utils

import io.reactivex.rxjava3.disposables.Disposable

fun Disposable?.safeDispose() {
    if (this?.isDisposed == false) dispose()
}