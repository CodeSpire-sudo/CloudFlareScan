package com.example.cfscanner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.io.File
import java.math.BigInteger
import java.net.InetAddress
import java.security.SecureRandom
import java.util.*

class MainActivity : AppCompatActivity() {

    // 视图控件
    private lateinit var btnIpv4Scan: Button
    private lateinit var btnIpv6Scan: Button
    private lateinit var btnStopTask: Button
    private lateinit var btnRegionSpeed: Button
    private lateinit var btnFullSpeed: Button
    private lateinit var btnExport: Button
    private lateinit var spinnerPort: Spinner
    private lateinit var etRegionCode: EditText
    private lateinit var etSpeedCount: EditText
    private lateinit var tvStatus: TextView
    private lateinit var tvSpeed: TextView
    private lateinit var tvStats: TextView
    private lateinit var scrollStats: ScrollView
    private lateinit var listSpeedResults: ListView
    private lateinit var progressBar: ProgressBar

    // 数据
    private val scanResults = mutableListOf<IpInfo>()
    private val speedResults = mutableListOf<IpInfo>()

    // 地区码中文映射表
    private val coloNameMap = mutableMapOf<String, String>()

    private var scanner: Scanner? = null
    private var speedTester: SpeedTester? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    // 存储从文件加载的IP段
    private var ipv4Ranges: List<String> = emptyList()
    private var ipv6Ranges: List<String> = emptyList()

    // 当前扫描类型
    private var currentScanType = "IPv4" // "IPv4" or "IPv6"
    private var currentTotalIps = 0

    // 测速速度记录
    private var scanStartTime = 0L
    private var lastScanCount = 0

    private val random = SecureRandom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 初始化视图
        btnIpv4Scan = findViewById(R.id.btn_ipv4_scan)
        btnIpv6Scan = findViewById(R.id.btn_ipv6_scan)
        btnStopTask = findViewById(R.id.btn_stop_task)
        btnRegionSpeed = findViewById(R.id.btn_region_speed)
        btnFullSpeed = findViewById(R.id.btn_full_speed)
        btnExport = findViewById(R.id.btn_export)
        spinnerPort = findViewById(R.id.spinner_port)
        etRegionCode = findViewById(R.id.et_region_code)
        etSpeedCount = findViewById(R.id.et_speed_count)
        tvStatus = findViewById(R.id.tv_status)
        tvSpeed = findViewById(R.id.tv_speed)
        tvStats = findViewById(R.id.tv_stats)
        scrollStats = findViewById(R.id.scroll_stats)
        listSpeedResults = findViewById(R.id.list_speed_results)
        progressBar = findViewById(R.id.progress_bar)

        // 设置链接（小琳解说）
        val tvLink = findViewById<TextView>(R.id.tv_link)
        val spannableString = SpannableString("小琳解说")
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, 0)
        tvLink.text = spannableString
        tvLink.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/xiaolin-007/CloudFlareScan"))
            startActivity(intent)
        }

        // 加载地区码映射表
        loadColoMap()

        // 加载IP段文件
        loadIpRangesFromAssets()

        // 设置监听器
        setupListeners()

        // 地区码自动大写
        setupRegionCodeAutoCapitalize()
    }

    // 从 assets/colo.txt 加载地区码中文映射
    private fun loadColoMap() {
        try {
            val lines = assets.open("colo.txt").bufferedReader().readLines()
            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty() || trimmed.startsWith("//")) continue

                val parts = trimmed.split(":", limit = 2)
                if (parts.size == 2) {
                    val code = parts[0].trim().uppercase()
                    val name = parts[1].trim()
                    if (code.isNotEmpty() && name.isNotEmpty()) {
                        coloNameMap[code] = name
                    }
                }
            }
            Log.d("MainActivity", "coloNameMap loaded, size=${coloNameMap.size}")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "加载地区码文件失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadIpRangesFromAssets() {
        try {
            ipv4Ranges = assets.open("ipv4.txt").bufferedReader().readLines()
                .filter { it.isNotBlank() }
                .map { it.trim() }
            ipv6Ranges = assets.open("ipv6.txt").bufferedReader().readLines()
                .filter { it.isNotBlank() }
                .map { it.trim() }
            Log.d("MainActivity", "IPv4 ranges: ${ipv4Ranges.size}, IPv6 ranges: ${ipv6Ranges.size}")
        } catch (e: Exception) {
            Toast.makeText(this, "加载IP段文件失败: ${e.message}", Toast.LENGTH_LONG).show()
            ipv4Ranges = emptyList()
            ipv6Ranges = emptyList()
        }
    }

    private fun setupListeners() {
        btnIpv4Scan.setOnClickListener { startScan("IPv4") }
        btnIpv6Scan.setOnClickListener { startScan("IPv6") }
        btnStopTask.setOnClickListener { stopCurrentTask() }
        btnRegionSpeed.setOnClickListener { startRegionSpeedTest() }
        btnFullSpeed.setOnClickListener { startFullSpeedTest() }
        btnExport.setOnClickListener { exportResults() }
    }

    private fun setupRegionCodeAutoCapitalize() {
        etRegionCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                s?.let {
                    val upper = it.toString().uppercase(Locale.getDefault())
                    if (upper != it.toString()) {
                        etRegionCode.removeTextChangedListener(this)
                        etRegionCode.setText(upper)
                        etRegionCode.setSelection(upper.length)
                        etRegionCode.addTextChangedListener(this)
                    }
                }
            }
        })
    }

    // 获取有效的测速数量（1-50）
    private fun getValidSpeedCount(): Int {
        val countStr = etSpeedCount.text.toString()
        val count = countStr.toIntOrNull() ?: 10
        val validCount = count.coerceIn(1, 50)
        if (validCount != count) {
            etSpeedCount.setText(validCount.toString())
            Toast.makeText(this, "测速数量已调整为 $validCount", Toast.LENGTH_SHORT).show()
        }
        return validCount
    }

    // 停止当前任务
    private fun stopCurrentTask() {
        scanner?.stop()
        speedTester?.stop()
        scanner = null
        speedTester = null
        btnIpv4Scan.isEnabled = true
        btnIpv6Scan.isEnabled = true
        btnStopTask.isEnabled = false
        progressBar.visibility = View.GONE
        tvStatus.text = "状态: 已停止"
    }

    // 开始扫描（IPv4或IPv6）
    private fun startScan(type: String) {
        currentScanType = type
        val ranges = if (type == "IPv4") ipv4Ranges else ipv6Ranges
        if (ranges.isEmpty()) {
            Toast.makeText(this, "没有可扫描的IP段，请检查assets文件", Toast.LENGTH_SHORT).show()
            return
        }

        // 清空之前的数据
        scanResults.clear()
        speedResults.clear()
        tvStats.text = ""
        updateSpeedListDisplay()

        val port = spinnerPort.selectedItem.toString().toInt()

        // 生成随机IP列表
        val statusBuilder = StringBuilder()
        statusBuilder.append("正在开始${type}扫描.....\n")
        statusBuilder.append("正在从Cloudflare ${type} IP段生成随机IP... (端口: $port)\n")
        tvStats.text = statusBuilder.toString()

        val ipList = generateRandomIps(ranges, type)
        val total = ipList.size
        statusBuilder.append("已生成 $total 个随机${type} IP\n")
        statusBuilder.append("开始延迟测试 $total 个${type} IP......\n")
        tvStats.text = statusBuilder.toString()

        currentTotalIps = total

        btnIpv4Scan.isEnabled = false
        btnIpv6Scan.isEnabled = false
        btnStopTask.isEnabled = true
        btnRegionSpeed.isEnabled = false
        btnFullSpeed.isEnabled = false
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        tvStatus.text = "状态: 扫描中..."
        tvSpeed.text = "速度: 0 IP/秒"
        scanStartTime = System.currentTimeMillis()
        lastScanCount = 0

        scanner = Scanner(
            ipList = ipList,
            port = port,
            onProgress = { current, total ->
                progressBar.max = total
                progressBar.progress = current
                tvStatus.text = "状态: 扫描中 $current/$total"
                val elapsed = (System.currentTimeMillis() - scanStartTime) / 1000.0
                if (elapsed >= 1.0) {
                    val speed = (current - lastScanCount) / elapsed
                    tvSpeed.text = String.format("速度: %.1f IP/秒", speed)
                    lastScanCount = current
                    scanStartTime = System.currentTimeMillis()
                }
            },
            onResult = { info ->
                scanResults.add(info)
            },
            onComplete = {
                tvStatus.text = "状态: 扫描完成，共 ${scanResults.size} 个可用IP"
                tvSpeed.text = "速度: 0 IP/秒"
                btnIpv4Scan.isEnabled = true
                btnIpv6Scan.isEnabled = true
                btnStopTask.isEnabled = false
                btnRegionSpeed.isEnabled = scanResults.isNotEmpty()
                btnFullSpeed.isEnabled = scanResults.isNotEmpty()
                progressBar.visibility = View.GONE
                scanner = null
                showFinalStats(type)

                // 追加提示并滚动到底部
                tvStats.append("\nIP扫描已结束，请使用完全测速或地区测速功能")
                scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        )
        scanner?.start()
    }

    // 生成随机IP列表
    private fun generateRandomIps(ranges: List<String>, type: String): List<String> {
        val ips = mutableListOf<String>()
        if (type == "IPv4") {
            for (cidr in ranges) {
                try {
                    val ip = generateRandomIPv4FromCidr(cidr)
                    ips.add(ip)
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error generating IPv4 from $cidr: ${e.message}")
                }
            }
        } else {
            for (cidr in ranges) {
                try {
                    // 每个 /48 段生成 20 个随机 IPv6 地址
                    val ipList = generateRandomIPv6FromCidr(cidr, 20)
                    ips.addAll(ipList)
                    Log.d("MainActivity", "CIDR $cidr generated ${ipList.size} IPv6 addresses")
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error generating IPv6 from $cidr: ${e.message}")
                }
            }
            Log.d("MainActivity", "Total IPv6 addresses generated: ${ips.size}")
        }
        return ips
    }

    // 从/24 CIDR生成一个随机IPv4
    private fun generateRandomIPv4FromCidr(cidr: String): String {
        val parts = cidr.split("/")
        val baseIp = parts[0]
        val prefix = parts[1].toInt()
        if (prefix != 24) throw IllegalArgumentException("Only /24 supported for IPv4")
        val ipLong = ipToLong(baseIp)
        val network = ipLong and 0xFFFFFF00 // 取前24位
        val host = random.nextInt(256)
        return longToIp(network + host)
    }

    // IPv6生成函数（基于BigInteger，支持任何前缀）
    private fun generateRandomIPv6FromCidr(cidr: String, count: Int): List<String> {
        val parts = cidr.split("/")
        if (parts.size != 2) return emptyList()
        val prefix = parts[1].toInt()
        if (prefix > 48) return emptyList() // 我们只支持/48及更小的前缀，因为要留出后80位随机

        // 将地址部分转换为BigInteger
        val addrBytes = InetAddress.getByName(parts[0]).address
        val addrInt = BigInteger(1, addrBytes) // 无符号大整数

        // 创建掩码
        val mask = BigInteger.ONE.shiftLeft(128 - prefix).subtract(BigInteger.ONE).not()
        val network = addrInt.and(mask) // 网络部分

        val ips = mutableListOf<String>()
        repeat(count) {
            // 生成随机主机部分
            val hostBits = 128 - prefix
            val host = if (hostBits > 0) {
                BigInteger(hostBits, java.util.Random())
            } else {
                BigInteger.ZERO
            }
            val fullAddr = network.or(host)
            val ip = bigIntegerToIpv6(fullAddr)
            ips.add(ip)
        }
        return ips
    }

    // 将BigInteger转换为IPv6字符串（无压缩格式）
    private fun bigIntegerToIpv6(addr: BigInteger): String {
        val bytes = addr.toByteArray()
        // 如果字节数组不足16字节，前面补0
        val fullBytes = ByteArray(16)
        val start = 16 - bytes.size
        System.arraycopy(bytes, 0, fullBytes, start, bytes.size)
        // 每2个字节一组，转换为十六进制，并用冒号连接
        return (0 until 16 step 2).joinToString(":") { i ->
            ((fullBytes[i].toInt() and 0xFF) shl 8 or (fullBytes[i + 1].toInt() and 0xFF)).toString(16)
        }
    }

    private fun ipToLong(ip: String): Long {
        val parts = ip.split(".").map { it.toLong() }
        return (parts[0] shl 24) or (parts[1] shl 16) or (parts[2] shl 8) or parts[3]
    }

    private fun longToIp(ipLong: Long): String {
        return "${(ipLong shr 24) and 0xFF}.${(ipLong shr 16) and 0xFF}.${(ipLong shr 8) and 0xFF}.${ipLong and 0xFF}"
    }

    // 显示最终统计（总可用IP包含所有Ping成功的IP，地区统计只显示有效地区码的IP）
    private fun showFinalStats(type: String) {
        val validResults = scanResults.filter { it.colo != "N/A" }
        val countMap = mutableMapOf<String, Int>()
        for (info in validResults) {
            val colo = info.colo
            countMap[colo] = countMap.getOrDefault(colo, 0) + 1
        }

        val sb = StringBuilder()
        sb.append("=========================\n")
        sb.append("扫描完成！统计信息：\n")
        sb.append("总可用${type}地址: ${scanResults.size} 个 (端口: ${spinnerPort.selectedItem})\n")
        if (validResults.isNotEmpty()) {
            sb.append("有效地区: ${validResults.size} 个\n")
            sb.append("地区统计（共 ${countMap.size} 个不同地区）：\n")
            val sorted = countMap.entries.sortedByDescending { it.value }
            for ((colo, count) in sorted) {
                val chineseName = coloNameMap[colo] ?: ""
                if (chineseName.isNotEmpty()) {
                    sb.append("  $colo ($chineseName): ${count}个IP\n")
                } else {
                    sb.append("  $colo: ${count}个IP\n")
                }
            }
        } else {
            sb.append("（未获取到任何地区码）\n")
        }
        tvStats.text = sb.toString()

        Log.d("MainActivity", "统计信息:\n$sb")
    }

    // 地区测速
    private fun startRegionSpeedTest() {
        val region = etRegionCode.text.toString().trim()
        if (region.isEmpty()) {
            Toast.makeText(this, "请输入地区码", Toast.LENGTH_SHORT).show()
            return
        }

        val count = getValidSpeedCount()
        val filtered = scanResults.filter { it.colo.equals(region, ignoreCase = true) }
            .sortedBy { it.delay }
            .take(count)

        if (filtered.isEmpty()) {
            Toast.makeText(this, "该地区没有可用IP", Toast.LENGTH_SHORT).show()
            return
        }

        // 添加初始状态信息
        val chineseName = coloNameMap[region] ?: region
        tvStats.append("\n开始地区测速：$region ($chineseName) (端口: ${spinnerPort.selectedItem})")
        tvStats.append("\n地区测速：将对 ${filtered.size} 个IP进行测速 (最大测速数: $count)")
        scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }

        startSpeedTest(filtered, "地区测速")
    }

    // 完全测速
    private fun startFullSpeedTest() {
        val count = getValidSpeedCount()
        val filtered = scanResults.sortedBy { it.delay }.take(count)

        if (filtered.isEmpty()) {
            Toast.makeText(this, "没有可用IP进行测速", Toast.LENGTH_SHORT).show()
            return
        }

        // 添加初始状态信息
        tvStats.append("\n开始完全测速 (端口: ${spinnerPort.selectedItem})")
        tvStats.append("\n完全测速：将对 ${filtered.size} 个IP进行测速 (最大测速数: $count)")
        scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }

        startSpeedTest(filtered, "完全测速")
    }

    // 通用测速启动（串行，完成后一次性显示结果）
    private fun startSpeedTest(ipList: List<IpInfo>, testType: String) {
        speedResults.clear()
        // 清空表格（speedResults为空，显示空白）
        updateSpeedListDisplay()

        btnRegionSpeed.isEnabled = false
        btnFullSpeed.isEnabled = false
        btnStopTask.isEnabled = true
        progressBar.visibility = View.VISIBLE
        progressBar.progress = 0
        tvStatus.text = "状态: 测速中 ($testType)..."

        speedTester = SpeedTester(
            ipList = ipList,
            onProgress = { current, total ->
                progressBar.max = total
                progressBar.progress = current
                tvStatus.text = "状态: 测速中 $current/$total"
            },
            onResult = { info ->
                // 实时显示测速结果（含地区）
                val chineseName = coloNameMap[info.colo] ?: info.colo
                tvStats.append("\n测速结果: ${"%.2f".format(info.speed)} MB/s   $chineseName")
                scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }

                // 只收集数据，不更新表格
                speedResults.add(info)
            },
            onLog = { logMsg ->
                runOnUiThread {
                    tvStats.append("\n$logMsg")
                    scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }
                }
            },
            onComplete = {
                // 所有IP测速完成后，排序并一次性显示
                speedResults.sortByDescending { it.speed }
                updateSpeedListDisplay()

                tvStatus.text = "状态: 测速完成"
                btnRegionSpeed.isEnabled = true
                btnFullSpeed.isEnabled = true
                btnStopTask.isEnabled = false
                progressBar.visibility = View.GONE
                speedTester = null
                tvStats.append("\n测速完成！！")
                tvStats.append("\n成功测速 ${speedResults.size} 个IP (端口: ${spinnerPort.selectedItem})")
                scrollStats.post { scrollStats.fullScroll(ScrollView.FOCUS_DOWN) }
            }
        )
        speedTester?.start()
    }

    // 更新测速结果列表
    private fun updateSpeedListDisplay() {
        val adapter = SpeedResultAdapter(this, speedResults)
        listSpeedResults.adapter = adapter
    }

    // 导出测速结果为CSV
    private fun exportResults() {
        if (speedResults.isEmpty()) {
            Toast.makeText(this, "没有测速结果可导出", Toast.LENGTH_SHORT).show()
            return
        }

        val content = StringBuilder()
        content.append("排名,IP地址,地区,延迟(ms),下载速度(MB/s),端口,测速类型\n")
        speedResults.forEachIndexed { index, info ->
            val chineseName = coloNameMap[info.colo] ?: info.colo
            content.append("${index + 1},${info.ip},$chineseName,${info.delay},${"%.2f".format(info.speed)},${info.port},地区测速\n")
        }

        try {
            val file = File(getExternalFilesDir(null), "speed_results_${System.currentTimeMillis()}.csv")
            file.writeText(content.toString())
            Toast.makeText(this, "导出成功: ${file.absolutePath}", Toast.LENGTH_LONG).show()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this@MainActivity,
                    "$packageName.fileprovider",
                    file
                ))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(shareIntent, "分享测速结果"))
        } catch (e: Exception) {
            Toast.makeText(this, "导出失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // 内部适配器
    inner class SpeedResultAdapter(context: Context, private val items: List<IpInfo>) :
        ArrayAdapter<IpInfo>(context, 0, items) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_speed_result, parent, false)
            val info = items[position]

            // 排名
            view.findViewById<TextView>(R.id.tv_rank).text = (position + 1).toString()

            // IP地址行
            view.findViewById<TextView>(R.id.tv_ip).text = info.ip

            // 详细信息行（地区 + 延迟 + 速度）
            val chineseName = coloNameMap[info.colo] ?: info.colo
            val infoText = "$chineseName 延迟:${info.delay}ms 速度:${"%.2f".format(info.speed)} MB/s"
            view.findViewById<TextView>(R.id.tv_info).text = infoText

            // 复制按钮
            val btnCopy = view.findViewById<MaterialButton>(R.id.btn_copy)
            btnCopy.setOnClickListener {
                copyToClipboard(info.ip)
            }

            return view
        }

        private fun copyToClipboard(text: String) {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("IP Address", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "IP 已复制: $text", Toast.LENGTH_SHORT).show()
        }
    }
}