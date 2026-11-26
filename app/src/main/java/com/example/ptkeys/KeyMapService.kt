package com.example.ptkeys

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class KeyMapService : AccessibilityService() {

    private lateinit var profile: JSONObject
    private var inGame: Boolean = false
    private val pressed = HashSet<Int>()
    private var stroke: GestureDescription.StrokeDescription? = null
    private var last: PointF? = null
    private var busy: Boolean = false
    private var dirty: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        val text = assets.open("profiles/play_together.json").bufferedReader().use { it.readText() }
        profile = JSONObject(text)
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = e.packageName?.toString() ?: return
        if (pkg.startsWith("com.android.systemui")) return
        inGame = (pkg == profile.getString("package"))
        if (!inGame) {
            pressed.clear()
            stroke = null
            last = null
        }
    }

    override fun onInterrupt() {
    }

    private fun code(name: String): Int = KeyEvent.keyCodeFromString("KEYCODE_" + name)
    private fun sw(): Float = resources.displayMetrics.widthPixels.toFloat()
    private fun sh(): Float = resources.displayMetrics.heightPixels.toFloat()
    private fun num(o: JSONObject, k: String): Float = o.getDouble(k).toFloat()

    override fun onKeyEvent(ev: KeyEvent): Boolean {
        if (!inGame) return false
        val k = ev.keyCode
        val isDown = ev.action == KeyEvent.ACTION_DOWN && ev.repeatCount == 0

        val joy = profile.getJSONObject("joystick")
        val joyNames = arrayOf("up", "left", "down", "right")
        for (n in joyNames) {
            if (code(joy.getString(n)) == k) {
                if (ev.action == KeyEvent.ACTION_DOWN) {
                    pressed.add(k)
                } else if (ev.action == KeyEvent.ACTION_UP) {
                    pressed.remove(k)
                }
                updateJoystick()
                return true
            }
        }

        val taps = profile.getJSONArray("taps")
        for (i in 0 until taps.length()) {
            val t = taps.getJSONObject(i)
            if (code(t.getString("key")) == k) {
                if (isDown) {
                    tap(num(t, "x") * sw(), num(t, "y") * sh())
                }
                return true
            }
        }

        val cam = profile.getJSONObject("camera")
        val names = arrayOf("up", "down", "left", "right")
        val dxs = floatArrayOf(0f, 0f, -1f, 1f)
        val dys = floatArrayOf(-1f, 1f, 0f, 0f)
        for (i in 0 until 4) {
            if (code(cam.getString(names[i])) == k) {
                if (isDown) {
                    val cx = num(cam, "x") * sw()
                    val cy = num(cam, "y") * sh()
                    val dist = num(cam, "distance") * sw()
                    swipe(cx, cy, cx + dxs[i] * dist, cy + dys[i] * dist)
                }
                return true
            }
        }
        return false
    }

    private fun tap(x: Float, y: Float) {
        val p = Path()
        p.moveTo(x, y)
        val s = GestureDescription.StrokeDescription(p, 0L, 60L)
        dispatchGesture(GestureDescription.Builder().addStroke(s).build(), null, null)
    }

    private fun swipe(x1: Float, y1: Float, x2: Float, y2: Float) {
        val p = Path()
        p.moveTo(x1, y1)
        p.lineTo(x2, y2)
        val s = GestureDescription.StrokeDescription(p, 0L, 200L)
        dispatchGesture(GestureDescription.Builder().addStroke(s).build(), null, null)
    }

    private fun updateJoystick() {
        if (busy) {
            dirty = true
            return
        }
        val joy = profile.getJSONObject("joystick")
        val cx = num(joy, "x") * sw()
        val cy = num(joy, "y") * sh()
        val r = num(joy, "radius") * sw()

        var dx = 0f
        var dy = 0f
        if (pressed.contains(code(joy.getString("up")))) dy -= 1f
        if (pressed.contains(code(joy.getString("down")))) dy += 1f
        if (pressed.contains(code(joy.getString("left")))) dx -= 1f
        if (pressed.contains(code(joy.getString("right")))) dx += 1f
        val len = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        val prev = last
        val fromX = if (prev != null) prev.x else cx
        val fromY = if (prev != null) prev.y else cy

        val path
