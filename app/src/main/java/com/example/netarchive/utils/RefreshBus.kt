package com.example.netarchive.utils

import kotlinx.coroutines.flow.MutableSharedFlow


object RefreshBus {
    val refreshFlow = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
}