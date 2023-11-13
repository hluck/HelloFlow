package com.example.helloflow

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 *
 * @Author： LJH
 * @Time： 2023/11/13
 * @description：
 */
class MainViewModel:ViewModel() {

    val timeFlow = flow<Int> {
        var time = 0
        while (true) {
            emit(time)
            delay(1000)
            time++
        }
    }
}