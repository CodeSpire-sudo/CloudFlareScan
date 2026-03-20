package com.example.cfscanner

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.SecureRandom
import java.security.cert.X509Certificate

class SpeedTester(
    private val ipList: List<IpInfo>,
    private val onProgress: (current: Int, total: Int) -> Unit,
    private val onResult: (IpInfo) -> Unit,
    private val onLog: (String) -> Unit,
    private val onComplete: () -> Unit
) {
    // 单线程执行器，保证串行测速
    private val executor = Executors.newSingleThreadExecutor()
    private val isCancelled = AtomicBoolean(false)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val testHost = "speed.cloudflare.com"
    private val downloadBytes = 50 * 1024 * 1024 // 请求50MB

    private val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
        override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
    })

    private val sslContext: SSLContext by lazy {
        SSLContext.getInstance("TLS").apply {
            init(null, trustAllCerts, SecureRandom())
        }
    }

    fun start() {
        if (ipList.isEmpty()) {
            mainHandler.post { onComplete() }
            return
        }

        val total = ipList.size
        var completed = 0

        executor.submit {
            for (info in ipList) {
                if (isCancelled.get()) break

                // 发送开始测速日志
                mainHandler.post {
                    onLog("[${completed + 1}/$total] 正在测速 ${info.ip} (端口: ${info.port})")
                }

                val speed = measureSpeed(info.ip, info.port)
                info.speed = speed

                mainHandler.post {
                    onResult(info)
                    completed++
                    onProgress(completed, total)
                }
            }
            mainHandler.post {
                if (!isCancelled.get()) {
                    onComplete()
                }
            }
        }
    }

    fun stop() {
        isCancelled.set(true)
        executor.shutdownNow()
    }

    private fun measureSpeed(ip: String, port: Int): Double {
        val path = "/__down?bytes=$downloadBytes"
        val request = buildString {
            append("GET $path HTTP/1.1\r\n")
            append("Host: $testHost\r\n")
            append("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36\r\n")
            append("Accept: */*\r\n")
            append("Connection: close\r\n\r\n")
        }.toByteArray()

        val connectTimeout = 3000
        val maxTestDuration = 3000L // 总测速时长3秒
        var socket: Socket? = null

        try {
            val rawSocket = Socket()
            rawSocket.connect(InetSocketAddress(ip, port), connectTimeout)
            rawSocket.soTimeout = 10000 // 防止单次读取卡死

            socket = if (port == 443) {
                val sslFactory = sslContext.socketFactory as SSLSocketFactory
                val sslSocket = sslFactory.createSocket(rawSocket, testHost, port, true) as SSLSocket
                sslSocket.startHandshake()
                sslSocket
            } else {
                rawSocket
            }

            socket.getOutputStream().write(request)
            socket.getOutputStream().flush()

            val input = socket.getInputStream()
            val buffer = ByteArray(16384)
            var totalBytes = 0L
            var headerEnd = false
            val headerBuffer = StringBuilder()
            val startTime = System.currentTimeMillis()

            while (System.currentTimeMillis() - startTime < maxTestDuration) {
                val bytesRead = input.read(buffer)
                if (bytesRead == -1) break

                if (!headerEnd) {
                    val chunk = String(buffer, 0, bytesRead)
                    headerBuffer.append(chunk)
                    val idx = headerBuffer.indexOf("\r\n\r\n")
                    if (idx != -1) {
                        headerEnd = true
                        val bodyStart = idx + 4
                        if (bodyStart < headerBuffer.length) {
                            totalBytes += (headerBuffer.substring(bodyStart).toByteArray().size)
                        }
                    }
                } else {
                    totalBytes += bytesRead
                }
            }

            socket.close()
            val duration = System.currentTimeMillis() - startTime

            if (duration > 0 && totalBytes > 0) {
                val speedMB = (totalBytes.toDouble() / duration) * 1000 / (1024 * 1024)
                val speedFormatted = "%.2f".format(speedMB)
                // 成功时不发送 onLog，避免重复（MainActivity 的 onResult 会显示带地区的消息）
                Log.d("SpeedTester", "测速成功: $ip, 速度: $speedFormatted MB/s")
                return speedMB
            } else {
                mainHandler.post { onLog("测速失败，未收到有效数据") }
            }
        } catch (e: SocketTimeoutException) {
            mainHandler.post { onLog("连接超时") }
        } catch (e: Exception) {
            mainHandler.post { onLog("测速异常: ${e.message}") }
        } finally {
            try { socket?.close() } catch (_: Exception) {}
        }
        return 0.0
    }
}