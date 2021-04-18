package com.moyi.liu.audiofeedback.domain.message

import com.moyi.liu.audiofeedback.domain.model.Message

interface MessageStore {
    fun getMessage(message: Message): String?
}