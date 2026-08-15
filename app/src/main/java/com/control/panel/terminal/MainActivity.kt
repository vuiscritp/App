package com.control.panel.terminal

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var tvOutput: TextView
    private lateinit var etInput: EditText
    private lateinit var scrollView: ScrollView
    private val handler = Handler(Looper.getMainLooper())

    private var isFlashOn = false
    private var cameraId: String? = null
    private var cameraManager: CameraManager? = null

    companion object {
        private const val REQUEST_PERMISSIONS = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvOutput = findViewById(R.id.tvOutput)
        etInput = findViewById(R.id.etInput)
        scrollView = findViewById(R.id.scrollView)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            cameraId = cameraManager?.cameraIdList?.get(0)
        } catch (e: Exception) {
            // ignore
        }

        printWelcome()
        requestAllPermissions()

        etInput.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEND ||
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)
            ) {
                val cmd = etInput.text.toString().trim()
                if (cmd.isNotEmpty()) {
                    appendOutput("> $cmd")
                    processCommand(cmd)
                    etInput.setText("")
                }
                true
            } else false
        }
    }

    private fun printWelcome() {
        val sb = StringBuilder()
        sb.append("╔══════════════════════════════════════╗\n")
        sb.append("║     CONTROL PANEL TERMINAL v1.0      ║\n")
        sb.append("║     Black Theme • Own Device Only    ║\n")
        sb.append("╚══════════════════════════════════════╝\n\n")
        sb.append("Type 'android-help' to see available commands.\n")
        sb.append("Type 'android-status' to check services & permissions.\n\n")
        tvOutput.text = sb.toString()
    }

    private fun appendOutput(text: String) {
        handler.post {
            tvOutput.append("$text\n")
            scrollView.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
        }
    }

    private fun processCommand(input: String) {
        val cmd = input.lowercase(Locale.getDefault()).trim()

        when {
            cmd == "android-help" || cmd == "help" -> showHelp()
            cmd == "android-info" || cmd == "info" -> showDeviceInfo()
            cmd == "android-list-apps" || cmd == "list-apps" -> listApps()
            cmd == "android-status" || cmd == "status" -> showStatus()
            cmd == "android-clear" || cmd == "clear" || cmd == "cls" -> clearScreen()
            cmd == "android-flash-on" || cmd == "flash-on" -> setFlash(true)
            cmd == "android-flash-off" || cmd == "flash-off" -> setFlash(false)
            cmd == "android-wifi-on" || cmd == "wifi-on" -> setWifi(true)
            cmd == "android-wifi-off" || cmd == "wifi-off" -> setWifi(false)
            cmd == "android-kill-all" || cmd == "kill-all" -> killAllApps()
            cmd.startsWith("android-app-") || cmd.startsWith("app-") -> {
                val name = cmd.substringAfterLast("-")
                showAppInfo(name)
            }
            cmd.startsWith("android-kill-") || cmd.startsWith("kill-") -> {
                val name = cmd.substringAfterLast("-")
                killApp(name)
            }
            cmd.startsWith("android-stop-") || cmd.startsWith("stop-") -> {
                val name = cmd.substringAfterLast("-")
                stopApp(name)
            }
            cmd.startsWith("android-open-") || cmd.startsWith("open-") -> {
                val name = cmd.substringAfterLast("-")
                openApp(name)
            }
            cmd.startsWith("android-cache-") || cmd.startsWith("cache-") -> {
                val name = cmd.substringAfterLast("-")
                clearAppCache(name)
            }
            cmd.startsWith("notification-") -> {
                val text = input.substringAfter("notification-").trim()
                sendNotification(text)
            }
            cmd == "android-accessibility" || cmd == "accessibility" -> openAccessibilitySettings()
            cmd == "android-usage" || cmd == "usage" -> openUsageAccessSettings()
            cmd == "android-overlay" || cmd == "overlay" -> openOverlaySettings()
            cmd == "android-notification-access" -> openNotificationListenerSettings()
            else -> appendOutput("[!] Unknown command. Type 'android-help'")
        }
    }

    private fun showHelp() {
        val help = """
            |══════════════════════════════════════
            | AVAILABLE COMMANDS
            |══════════════════════════════════════
            | android-help              Show this help
            | android-info              Device information
            | android-list-apps         List installed apps
            | android-app-<name>        App details
            | android-flash-on/off      Torch control
            | android-wifi-on/off       WiFi toggle
            | android-kill-<name>       Kill app process
            | android-kill-all          Kill background apps
            | android-stop-<name>       Force stop app
            | android-open-<name>       Launch app
            | android-cache-<name>      Clear app cache
            | notification-<text>       Post notification
            | android-status            Check permissions & services
            | android-accessibility     Open Accessibility settings
            | android-usage             Open Usage Access settings
            | android-overlay           Open Overlay settings
            | android-clear             Clear terminal
            |══════════════════════════════════════
            | Note: Many actions need permissions
            | or special access enabled manually.
            |══════════════════════════════════════
        """.trimMargin()
        appendOutput(help)
    }

    private fun showDeviceInfo() {
        val sb = StringBuilder()
        sb.append("═══ DEVICE INFO ═══\n")
        sb.append("Manufacturer : ${Build.MANUFACTURER}\n")
        sb.append("Brand        : ${Build.BRAND}\n")
        sb.append("Model        : ${Build.MODEL}\n")
        sb.append("Device       : ${Build.DEVICE}\n")
        sb.append("Product      : ${Build.PRODUCT}\n")
        sb.append("Android      : ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
        sb.append("Incremental  : ${Build.VERSION.INCREMENTAL}\n")
        sb.append("Board        : ${Build.BOARD}\n")
        sb.append("Hardware     : ${Build.HARDWARE}\n")
        sb.append("Fingerprint  : ${Build.FINGERPRINT}\n")
        sb.append("Time         : ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())}\n")
        appendOutput(sb.toString())
    }

    private fun listApps() {
        appendOutput("Scanning installed apps...")
        Thread {
            val pm = packageManager
            val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }

            val sb = StringBuilder()
            sb.append("═══ USER APPS (${apps.size}) ═══\n")
            apps.forEachIndexed { index, app ->
                val label = app.loadLabel(pm)
                sb.append("${index + 1}. $label\n    ${app.packageName}\n")
            }
            appendOutput(sb.toString())
        }.start()
    }

    private fun showAppInfo(keyword: String) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val found = apps.filter {
            it.loadLabel(pm).toString().lowercase().contains(keyword) ||
                    it.packageName.lowercase().contains(keyword)
        }

        if (found.isEmpty()) {
            appendOutput("[!] No app found matching '$keyword'")
            return
        }

        val sb = StringBuilder()
        found.forEach { app ->
            sb.append("═══ ${app.loadLabel(pm)} ═══\n")
            sb.append("Package : ${app.packageName}\n")
            sb.append("UID     : ${app.uid}\n")
            sb.append("Source  : ${app.sourceDir}\n")
            sb.append("Enabled : ${app.enabled}\n")
            sb.append("System  : ${app.flags and ApplicationInfo.FLAG_SYSTEM != 0}\n\n")
        }
        appendOutput(sb.toString())
    }

    private fun setFlash(on: Boolean) {
        try {
            if (cameraId == null) {
                appendOutput("[!] No camera / flashlight available")
                return
            }
            cameraManager?.setTorchMode(cameraId!!, on)
            isFlashOn = on
            appendOutput(if (on) "[+] Flashlight ON" else "[+] Flashlight OFF")
        } catch (e: Exception) {
            appendOutput("[!] Flash error: ${e.message}")
        }
    }

    private fun setWifi(enable: Boolean) {
        try {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                appendOutput("[!] Android 10+ restricts direct WiFi toggle.")
                appendOutput("    Opening system WiFi settings...")
                startActivity(Intent(Settings.Panel.ACTION_WIFI))
            } else {
                @Suppress("DEPRECATION")
                wifiManager.isWifiEnabled = enable
                appendOutput(if (enable) "[+] WiFi enabled" else "[+] WiFi disabled")
            }
        } catch (e: Exception) {
            appendOutput("[!] WiFi error: ${e.message}")
        }
    }

    private fun killApp(keyword: String) {
        val pm = packageManager
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val apps = pm.getInstalledApplications(0)
        val targets = apps.filter {
            it.loadLabel(pm).toString().lowercase().contains(keyword) ||
                    it.packageName.lowercase().contains(keyword)
        }

        if (targets.isEmpty()) {
            appendOutput("[!] No matching app for '$keyword'")
            return
        }

        targets.forEach { app ->
            try {
                am.killBackgroundProcesses(app.packageName)
                appendOutput("[+] Sent kill to: ${app.loadLabel(pm)} (${app.packageName})")
            } catch (e: Exception) {
                appendOutput("[!] Failed: ${app.packageName} - ${e.message}")
            }
        }
    }

    private fun killAllApps() {
        appendOutput("Killing background processes...")
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
            .filter { it.flags and ApplicationInfo.FLAG_SYSTEM == 0 }

        var count = 0
        apps.forEach { app ->
            if (app.packageName != packageName) {
                try {
                    am.killBackgroundProcesses(app.packageName)
                    count++
                } catch (_: Exception) {}
            }
        }
        appendOutput("[+] Kill signal sent to $count apps")
    }

    private fun stopApp(keyword: String) {
        // Force-stop normally requires system/signature permission.
        // We fall back to killBackgroundProcesses + open App Info so user can force stop.
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val target = apps.firstOrNull {
            it.loadLabel(pm).toString().lowercase().contains(keyword) ||
                    it.packageName.lowercase().contains(keyword)
        }

        if (target == null) {
            appendOutput("[!] App not found: $keyword")
            return
        }

        try {
            val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            am.killBackgroundProcesses(target.packageName)
            appendOutput("[+] Background processes killed for ${target.loadLabel(pm)}")
            appendOutput("    Opening App Info so you can Force Stop if needed...")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = android.net.Uri.parse("package:${target.packageName}")
            }
            startActivity(intent)
        } catch (e: Exception) {
            appendOutput("[!] Error: ${e.message}")
        }
    }

    private fun openApp(keyword: String) {
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val target = apps.firstOrNull {
            it.loadLabel(pm).toString().lowercase().contains(keyword) ||
                    it.packageName.lowercase().contains(keyword)
        }

        if (target == null) {
            appendOutput("[!] App not found: $keyword")
            return
        }

        val launch = pm.getLaunchIntentForPackage(target.packageName)
        if (launch != null) {
            startActivity(launch)
            appendOutput("[+] Opened: ${target.loadLabel(pm)}")
        } else {
            appendOutput("[!] Cannot launch ${target.packageName}")
        }
    }

    private fun clearAppCache(keyword: String) {
        // Clearing cache of other apps requires privileged permission on modern Android.
        // We open the App Info page for the user.
        val pm = packageManager
        val apps = pm.getInstalledApplications(0)
        val target = apps.firstOrNull {
            it.loadLabel(pm).toString().lowercase().contains(keyword) ||
                    it.packageName.lowercase().contains(keyword)
        }

        if (target == null) {
            appendOutput("[!] App not found: $keyword")
            return
        }

        appendOutput("[*] Opening App Info for ${target.loadLabel(pm)}")
        appendOutput("    You can clear cache manually there.")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${target.packageName}")
        }
        startActivity(intent)
    }

    private fun sendNotification(text: String) {
        if (text.isBlank()) {
            appendOutput("[!] Usage: notification-<your text>")
            return
        }
        try {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            val channelId = "control_panel_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId, "Control Panel", android.app.NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channel)
            }
            val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
                .setContentTitle("Control Panel")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(Random().nextInt(10000), notification)
            appendOutput("[+] Notification posted: $text")
        } catch (e: Exception) {
            appendOutput("[!] Notification error: ${e.message}")
        }
    }

    private fun showStatus() {
        val sb = StringBuilder()
        sb.append("═══ STATUS ═══\n")
        sb.append("Accessibility : ${if (isAccessibilityEnabled()) "ENABLED" else "DISABLED (need enable)"}\n")
        sb.append("Usage Access  : Check manually\n")
        sb.append("Overlay       : ${if (Settings.canDrawOverlays(this)) "GRANTED" else "NOT GRANTED"}\n")
        sb.append("Flashlight    : ${if (isFlashOn) "ON" else "OFF"}\n")
        sb.append("Package       : $packageName\n")
        appendOutput(sb.toString())
        appendOutput("Use android-accessibility / android-usage / android-overlay to open settings.")
    }

    private fun isAccessibilityEnabled(): Boolean {
        val enabled = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: return false
        return enabled.contains(packageName)
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        appendOutput("[*] Opened Accessibility settings. Enable 'Control Panel'")
    }

    private fun openUsageAccessSettings() {
        startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        appendOutput("[*] Opened Usage Access settings")
    }

    private fun openOverlaySettings() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, android.net.Uri.parse("package:$packageName"))
        startActivity(intent)
        appendOutput("[*] Opened Overlay permission settings")
    }

    private fun openNotificationListenerSettings() {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        appendOutput("[*] Opened Notification Listener settings")
    }

    private fun clearScreen() {
        tvOutput.text = ""
        printWelcome()
    }

    private fun requestAllPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.VIBRATE,
            Manifest.permission.WAKE_LOCK
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        }

        val needRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (needRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, needRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            val granted = grantResults.count { it == PackageManager.PERMISSION_GRANTED }
            appendOutput("[*] Permissions granted: $granted / ${permissions.size}")
        }
    }
}
