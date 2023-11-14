package com.example.helloflow

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

/**
 *
 * @Author： LJH
 * @Time： 2023/11/13
 * @description：响应式编程就像是使用水龙头来接水一样。
 * 那么整个过程中最重要的部分一共有3处：水源、水管和水龙头。
 *  1.水源也就是我们的数据源，这部分是需要我们自己处理的。
 *  2.水龙头是最终的接收端，可能是要展示给用户的，这部分也需要我们自己处理。
 *  3.水管则是实现响应式编程的基建部分，这部分是由Flow封装好提供给我们的，并不需要我们自己去实现。
 */
class MainViewModel:ViewModel() {


    /*
        使用flow构建函数构建出的Flow是属于Cold Flow，也叫做冷流。
        所谓冷流就是在没有任何接受端的情况下，Flow是不会工作的。
        只有在有接受端（水龙头打开）的情况下，Flow函数体中的代码就会自动开始执行。
     */
    val timeFlow = flow<Int> {
        var time = 0 //水源
        while (true) {
            emit(time) //数据发送器，把传入的参数发送到水管中
            delay(1000)
            time++
        }
    }
}