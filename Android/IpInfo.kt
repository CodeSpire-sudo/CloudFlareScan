package com.example.cfscanner

data class IpInfo(
    val ip: String,
    val port: Int,
    val delay: Long,      // 延迟（毫秒）
    val colo: String,     // 地区码，如未知则为 "N/A"
    var speed: Double = 0.0  // 测速结果（KB/s），0表示未测速
)