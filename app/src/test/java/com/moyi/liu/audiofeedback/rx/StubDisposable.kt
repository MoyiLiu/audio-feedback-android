package com.moyi.liu.audiofeedback.rx

import io.reactivex.rxjava3.disposables.Disposable

class StubDisposable(private val isDisposed: Boolean = false) : Disposable {
    var isDisposeCalled = false
    override fun dispose() {
        isDisposeCalled = true
    }

    override fun isDisposed(): Boolean = isDisposed
}