package com.example.helloflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val mainViewModel by viewModels<MainViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val textView = findViewById<TextView>(R.id.text_view)
        val btn = findViewById<Button>(R.id.button)

        btn.setOnClickListener {
            lifecycleScope.launch {
//                mainViewModel.timeFlow.collect{time ->
//                    textView.text = time.toString()
//
//                }
                //todo 下面的语句执行不到，因为collect调用后相当于进入了一个死循环，除非在不同子协程上执行
//                mainViewModel.timeFlow.collect{
//
//                }

                /*
                    下面是正确写法，开启子协程单独调用collect。
                    调用collect函数就相当于把水龙头接到水管上并打开，这样从水源发送过来的任何数据，
                    我们在水龙头这边都可以接收到，然后再把接收到的数据更新到TextView上面即可。
                 */
                launch {
//                    mainViewModel.timeFlow.collect{time ->
//                        textView.text = time.toString() //依次显示1，2，3，4...
                        /*
                            模拟流速不均问题，在水源处我们是每秒种发送一条数据，结果在水龙头这里要3秒钟才能处理一条数据。
                            水龙头处理数据速度过慢，导致管道中存在大量的积压数据，并且积压的数据会一个个继续传递给水龙头，即使这些数据已经过期了。

                         */
//                        delay(3000)
//                    }
                    /*
                        解决思路：只要有更新的数据过来，如果上次的数据还没有处理完，那么我们就直接把它取消掉，立刻去处理最新的数据即可。
                            在Flow当中实现这样的功能，只需要借助collectLatest函数就能做到，
                            collectLatest函数只接收处理最新的数据。
                            如果有新数据到来了而前一个数据还没有处理完，则会将前一个数据剩余的处理逻辑全部取消。
                     */
                    mainViewModel.timeFlow.collectLatest {
                        textView.text = it.toString()
                        delay(3000)
                    }

                }
                launch {
//                    mainViewModel.timeFlow2.collect{time ->
//
//                    }
                }
            }
        }
    }
}