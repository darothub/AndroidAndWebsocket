package com.darothub.androidwithwebsocket

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.darothub.androidwithwebsocket.utils.WebSocket
import io.ably.lib.realtime.AblyRealtime
import io.ably.lib.realtime.Channel
import io.ably.lib.realtime.ConnectionState
import io.ably.lib.types.ClientOptions
import io.ably.lib.types.Message
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap

val Int.asDate: Date
    get() = Date(this.toLong() * 1000L)
fun main(){
    val json = HashMap<String, Int>()
    json["date"] = 1598435781
    val date = json["date"]?.asDate
    println(date)
}

fun hey(): () -> Int {
    return {
        0
    }
}
class MainActivity : AppCompatActivity() {
    val userId = "1";

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




//        WebSocket.createWebSocket {
//            runOnUiThread {
//                findViewById<TextView>(R.id.textView).text = it
//            }
//        }
//        WebSocket.sendMessageWithStomp(){
//            findViewById<TextView>(R.id.textView).text = it
//        }
//
//        findViewById<Button>(R.id.btn).setOnClickListener {
//            val msg = findViewById<TextView>(R.id.edt).text.toString()
//            WebSocket.sendEchoViaStomp(msg);
//        }
        val ably = AblyRealtime("ei07cg.g3KPQQ:Xj8QeTGYAe2izc8J")
        ably.connection.on {
            when(it.current){
                ConnectionState.connected-> {
                    Log.i("Main", "Connected")
                }
                ConnectionState.failed ->{
                    Log.i("Main", "Failed")
                }
                else -> {
                    Log.i("Main", "Default")
                }
            }
        }

        val channel = ably.channels.get("cladchat")
        val listener = Channel.MessageListener {
            val data = it.data;
            val j = JSONObject(data.toString())
            val chatName = j["text"].toString()
            runOnUiThread {
                findViewById<TextView>(R.id.textView).text = chatName

            }

            Log.i("Received", chatName)
        }
        channel.subscribe("3", listener)

        findViewById<Button>(R.id.btn).setOnClickListener {

            val msg = findViewById<TextView>(R.id.edt).text.toString()
            channel.publish("1", msg +"\n")
        }


    }

    private fun sendMessage(){
        WebSocket.sendMessage()

    }


}

fun Int.hekk() = this
