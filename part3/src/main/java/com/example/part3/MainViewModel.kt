package com.example.part3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

/**
 *
 * @Author： LJH
 * @Time： 2023/11/18
 * @description：
 */
class MainViewModel :ViewModel() {

    //StateFlow 和 LiveData 具有高度一致性，因此可想而知，StateFlow 也是粘性的。
    //todo 响应式编程是一种发送者和观察者配合工作的编程模式，由发送者发出数据消息，观察者接收到了消息之后进行逻辑处理。
    // 如果在观察者还没有开始工作的情况下，发送者就已经先将消息发出来了，
    // 稍后观察者才开始工作，那么此时观察者还应该收到刚才发出的那条消息吗？
    // 不管你觉得是应该还是不应该，这都不重要。如果此时观察者还能收到消息，那么这种行为就叫做粘性。
    // 而如果此时观察者收不到之前的消息，那么这种行为就叫做非粘性。
    // 想要使用非粘性的 StateFlow 版本？那么用 SharedFlow 就可以了
    private val _stateFlow = MutableStateFlow(0) //粘性
    val stateFlow = _stateFlow.asStateFlow()
    //MutableSharedFlow 是不需要传入初始值参数的,不要求观察者在观察的那一刻就能收到消息
    private val _loginFlow = MutableSharedFlow<String>() //非粘性

    fun startLogin(){
        //SharedFlow 无法像 StateFlow 那样通过给 value 变量赋值来发送消息，而是只能像传统 Flow 那样调用 emit 函数。
        viewModelScope.launch {
            _loginFlow.emit("login success")
        }
    }

    //冷流每次被 collect 都是要重新执行的
    val timeFlow = flow<Int> {
        var time = 0
        while (true){
            emit(time)
            delay(1000)
            time++
        }
    }

    //todo stateIn 函数可以将其他的 Flow 转换成 StateFlow
    //stateIn 函数接收 3 个参数，其中第 1 个参数是作用域，传入 viewModelScope 即可。第 3 个参数是初始值，计时器的初始值传入 0 即可
    //第 2 个参数指定了一个 5 秒的超时时长，那么只要在 5 秒钟内横竖屏切换完成了，Flow 就不会停止工作。
    //切到后台之后，如果 5 秒钟之内再回到前台，那么 Flow 也不会停止工作
    val stateFlow2 = timeFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        0)

//    fun startTimer(){
//        val timer = Timer()
//        timer.scheduleAtFixedRate(object : TimerTask(){
//            override fun run() {
//                _stateFlow.value += 1
//            }
//        },0,1000L)
//    }
}