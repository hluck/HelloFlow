package com.example.part2

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.fold
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.reduce
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 *
 * @Author： LJH
 * @Time： 2023/11/15
 * @description：操作符函数进阶
 */

fun main() {
    runBlocking {
        //todo 通过flowOf函数构造了一个flow对象，里面依次发送了1, 2, 3, 4, 5这几个值。
        val flow = flowOf(1,2,3,4,5,6)
        flow.filter { //todo filter用来过滤掉一些数据的
            it%2 == 0
        }.onEach { //todo onEach 遍历每一条数据的
            println(it)
            //todo map就是用于将一个值映射成另一个值，具体映射的规则我们则可以在map函数中自行定义
        }.map {
            it*it
        }.collect{
//            println(it)
        }

        println()

        //debounce
        flow{
            emit(1)
            emit(2)
            kotlinx.coroutines.delay(600)
            emit(3)
            kotlinx.coroutines.delay(100)
            emit(4)
            kotlinx.coroutines.delay(100)
            emit(5)
        }
            .debounce(500)//todo 两条数据之间的间隔超过500毫秒才能发送成功
            .collect{
                println(it) //2,5   todo 5由于是最后一条数据,因此可以发送成功
            }

        //samp  le 可以从flow的数据流当中按照一定的时间间隔来采样某一条数据。

        flow{
//            while (true){
                emit("发送一条弹幕")
//            }
            //每秒钟只取一条弹幕
        }.sample(1000) //sample是采样的意思，也就是说，它可以从flow的数据流当中按照一定的时间间隔来采样某一条数据。
            .flowOn(Dispatchers.IO)
            .collect{
                println(it)
            }

        //reduce
        val result = flow{
            for (i in 0 .. 100){
                emit(i)
            }
        }.reduce{ //reduce函数是一个终端操作符函数，它的后面不可以再接其他操作符函数了，而是只能获取最终的运行结果。
            acc,value -> acc+value //acc是累加值，value是流中的每一个元素
        }
        println(result)//5050

        //fold
        val result2 = flow {
            for (i in 1..100){
                emit(i)
            }
            //fold函数和reduce函数基本上是完全类似的，它也是一个终端操作符函数。
        }.fold(100){//主要的区别在于，fold函数需要传入一个初始值，这个初始值会作为首个累积值被传递到fold的函数体当中
            acc,value -> acc+value //acc
        }
        println(result2) //5150

        //使用fold演示字符串拼接示例
        val result3 = flow{
            for (i in 'A'..'Z'){
                emit(i)
            }
        }.fold("Alphabet:"){acc, value -> acc+value }
        println(result3)
        println()

        //flatMap 将两个flow中的数据进行映射、合并、压平成一个flow，最后再进行输出
        flowOf(1,2,3)
            .flatMapConcat {
                flowOf("a$it", "b$it", "c$it", "d$it")
            }.collect{
                print(it) //a1b1c1d1a2b2c2d2a3b3c3d3
            }

        println()
        //用flatMapConcat函数将sendGetTokenRequest和sendGetUserInfoRequest串连成一条链式执行的任务
        sendGetTokenRequest()
            .flatMapConcat { token ->
                sendGetUserInfoRequest(token)
            }.flowOn(Dispatchers.IO)
            .collect{ userInfo ->
                println(userInfo) //userInfo
            }


        flowOf(300,200,100)
            .flatMapConcat {//即使第一个数据被delay了300毫秒，后面的数据也没有优先执行权。
                flow {
                    kotlinx.coroutines.delay(it.toLong())
                    emit("a$it")
                    emit("b$it")
                }
            }.collect{
                print("$it,") //a300,b300,a200,b200,a100,b100,
            }
        println()

        //flatMapMerge
        //可以并发着去处理数据的，而并不保证顺序。那么哪条数据被delay的时间更短，它就可以更优先地得到处理。
        flowOf(300,200,100)
            .flatMapMerge {
                flow{
                    kotlinx.coroutines.delay(it.toLong())
                    emit("a$it")
                    emit("b$it")
                }
            }.collect{
                print("$it ")
            }
        println()

        //flatMapLatest 和其他两个 flatMap 函数都是类似的，也是把两个 flow 合并、压平成一个 flow。
        //如果有新数据到来了而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。
        //flow1 中的数据传递到 flow2 中会立刻进行处理，但如果 flow1 中的下一个数据要发送了，
        //    而 flow2 中上一个数据还没处理完，则会直接将剩余逻辑取消掉，开始处理最新的数据。
        flow{
            emit(1)
            kotlinx.coroutines.delay(150)
            emit(2)
            kotlinx.coroutines.delay(50)
            emit(3)
        }.flatMapLatest {
            flow {
                kotlinx.coroutines.delay(100)
                emit("$it")
            }
        }.collect{
            print("$it ")
        }
        println()

        //zip
        //使用 zip 连接的两个 flow，它们之间是并行的运行关系。
        // 这点和 flatMap 差别很大，因为 flatMap 的运行方式是一个 flow 中的数据流向另外一个 flow，是串行的关系。
        val flow1 = flowOf("a","b","c")
        val flow2 = flowOf(1,2,3,4,5)
        flow1.zip(flow2){a,b ->  //这里使用 zip 函数连接了两个 flow，并且在 zip 的函数体中将两个 flow 中的数据进行了拼接。
            a+b
        }.collect{
            print("$it ") //a1 b2 c3 zip 函数的规则是，只要其中一个 flow 中的数据全部处理结束就会终止运行，剩余未处理的数据将不会得到处理。因此，flow2 中的 4 和 5 这两个数据会被舍弃掉。
        }
        println()

        val start = System.currentTimeMillis()
        val flow11 = flow{
            kotlinx.coroutines.delay(3000)
            emit("a")
        }

        val flow22 = flow{
            kotlinx.coroutines.delay(2000)
            emit(1)
        }

        flow11.zip(flow22){a,b ->
            a+b
        }.collect{
            val end = System.currentTimeMillis()
            //由此可以证明 flow1 和 flow2 之间是并行的关系，最终的总耗时取决于运行耗时更久的那个 flow
            println("Time cost: ${end - start}")//结果是 3036 毫秒
        }
        println()

        sendRealtimeWeatherRequest()
            .zip(sendSevenDaysWeatherRequest()){realWeather,sevenDayWheather ->
                Weather(realWeather,sevenDayWheather)
            }.zip(sendWeatherBackgroundImageRequest()){weather, bgImg ->
                weather.bgImg = bgImg
                weather
            }.collect{
                println(it.bgImg)//bgImg
            }
        println()


        //buffer 解决 Flow 流速不均匀的问题(Flow 上游发送数据的速度和 Flow 下游处理数据的速度不匹配)
        //buffer 函数和 collectLatest 函数，以及 conflate 函数处理的问题都是类似的，那就是解决 Flow 流速不均匀的问题。
        flow {
            emit(1)
            kotlinx.coroutines.delay(1000)
            emit(2)
            kotlinx.coroutines.delay(1000)
            emit(3)
        }.onEach {
            println("$it is ready")
        }.buffer() //buffer 函数会让 flow 函数和 collect 函数运行在不同的协程当中，这样 flow 中的数据发送就不会受 collect 函数的影响
            .collect{
            delay(1000)
            println("$it is handled")
        }
        println()


        //conflate 在某些场景下，我们可能并不需要保留所有的数据。
        //如果使用 buffer 函数来提升运行效率就完全不合理，它会缓存太多完全没有必要保留的数据
        //它的特性是，只接收处理最新的数据，如果有新数据到来了而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。
        flow {
            var count = 0
            while (true){
                emit(count)
                delay(1000)
                count ++
            }
        }.conflate() //当前正在处理的数据无论如何都应该处理完，然后准备去处理下一条数据时，直接处理最新的数据
//            .collectLatest{//collectLatest当有新数据到来时而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。
            .collect{
            println("start handle $it")
            delay(2000)
            println("finish handle $it")
        }


    }



}

/**
 *  模拟需求：使用flatMapConcat
 *      我们想要获取用户的数据，但是获取用户数据必须要有token授权信息才行，
 *      因此我们得先发起一个请求去获取token信息，然后再发起另一个请求去获取用户数据。
 */

fun sendGetTokenRequest():Flow<String> = flow {
    emit("token")
}

fun sendGetUserInfoRequest(token:String):Flow<String> = flow {
    emit("userInfo")
}


/**
 * 模拟需求：使用zip完全扁平式地处理，没有任何层级嵌套
 *      开发一个天气预报应用，需要去一个接口请求当前实时的天气信息，还需要去另一个接口请求未来 7 天的天气信息。
 * 这两个接口之间并没有先后依赖关系，但是却需要两个接口同时返回数据之后再将天气信息展示给用户。
 * 这里我们还需要再去另外一个接口请求天气信息的背景图，3 个请求同时并发处理
 */
fun sendRealtimeWeatherRequest():Flow<String> = flow {
    emit("realWeather")
}
fun sendSevenDaysWeatherRequest():Flow<String> = flow {
    emit("sevenDaysWeather")
}

fun sendWeatherBackgroundImageRequest():Flow<String> = flow {
    emit("bgImg")
}

data class Weather(val realTimeWeather: String, val sevenDaysWeather: String,var bgImg:String = "")