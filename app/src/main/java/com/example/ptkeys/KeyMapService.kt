package com.example.ptkeys

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class KeyMapService : AccessibilityService() {

    private lateinit var profile: JSONObject
    private lateinit var joystick: Joystick
    private var inGame = false
    private val pressed = HashSet<Int>()

    override fun onServiceConnected() {
        super.onServiceConnected()
        val text = assets.open("profiles/play_together.json").bufferedReader().use { it.readText() }
        profile = JSONObject(text)
        val j = profile.getJSONObject("joystick")
        joystick = Joystick(this, num(j, "x") * sw(), num(j, "y") * sh(), num(j, "radius") * sw())
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg.startsWith("com.android.systemui")) return
        inGame = (pkg == profile.getString("package"))
        if (!inGame) {
            pressed.clear()
            joystick.reset()
        }
    }

    override fun onInterrupt() {
    }

    private fun code(name: String): Int = KeyEvent.keyCodeFromString("KEYCODE_" + name)
    private fun sw(): Float = resources.displayMetrics.widthPixels.toFloat()
    private fun sh(): Float = resources.displayMetrics.heightPixels.toFloat()
    private fun num(o: JSONObject, k: String): Float = o.getDouble(k).toFloat()
    private fun key(o: JSONObject, n: String): Int = code(o.getString(n))

    override fun onKeyEvent(ev: KeyEvent): Boolean {
        if (!inGame) return false
        val k = ev.keyCode
        val isDown = ev.action == KeyEvent.ACTION_DOWN && ev.repeatCount == 0

        val joy = profile.getJSONObject("joystick")
        val jk = intArrayOf(key(joy, "up"), key(joy, "down"), key(joy, "left"), key(joy, "right"))
        if (jk.contains(k)) {
            if (ev.action == KeyEvent.ACTION_DOWN) pressed.add(k)
            if (ev.action == KeyEvent.ACTION_UP) pressed.remove(k)
            var dx = 0f
            var dy = 0f
            if (pressed.contains(jk[0])) dy -= 1f
            if (pressed.contains(jk[1])) dy += 1f
            if (pressed.contains(jk[2])) dx -= 1f
            if (pressed.contains(jk[3])) dx += 1f
            joystick.set(dx, dy)
            return true
        }

        val taps = profile.getJSONArray("taps")
        for (i in 0 until taps.length()) {
            val t = taps.getJSONObject(i)
            if (key(t, "key") == k) {
                if (isDown) tapAt(this, num(t, "x") * sw(), num(t, "y") * sh())
                return true
            }
        }

        val cam = profile.getJSONObject("camera")
        val names = arrayOf("up", "down", "left", "right")
        val dxs = floatArrayOf(0f, 0f, -1f, 1f)
        val dys = floatArrayOf(-1f, 1f, 0f, 0f)
        for (i in 0 until 4) {
            if (key(cam, names[i]) == k) {
                if (isDown) {
                    val cx = num(cam, "x") * sw()
                    val cy = num(cam, "y") * sh()
                    val d = num(cam, "distance") * sw()
                    swipeAt(this, cx, cy, cx + dxs[i] * d, cy + dys[i] * d)
                }
                return true
            }
        }
        return false
    }
}
