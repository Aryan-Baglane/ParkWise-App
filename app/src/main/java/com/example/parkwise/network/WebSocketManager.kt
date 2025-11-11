package com.example.parkwise.network



import android.util.Log
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val url: String = "wss://api.yourdomain.com/updates"
) : WebSocketListener() {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var ws: WebSocket? = null
    private val listeners = mutableSetOf<(Map<String, Any>) -> Unit>()

    fun connect() {
        val req = Request.Builder().url(url).build()
        ws = client.newWebSocket(req, this)
    }

    fun disconnect() {
        ws?.close(1000, "bye")
    }

    fun addListener(fn: (Map<String, Any>) -> Unit) {
        listeners.add(fn)
    }

    fun removeListener(fn: (Map<String, Any>) -> Unit) {
        listeners.remove(fn)
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        try {
            val json = JSONObject(text)
            val map = json.keys().asSequence().associateWith { json[it] }
            listeners.forEach { it(map) }
        } catch (e: Exception) {
            Log.e("WebSocket", "parse error", e)
        }
    }

    override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
        onMessage(webSocket, bytes.utf8())
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.d("WebSocket", "connected")
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e("WebSocket", "failure", t)
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(code, reason)
    }
}
