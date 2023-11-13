package com.example.helloflow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
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
                //下面是正确写法，开启子协程单独调用collect
                launch {
                    mainViewModel.timeFlow.collect{time ->
                        textView.text = time.toString()
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