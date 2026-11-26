package com.example.ptkeys

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.graphics.Path
import android.graphics.PointF
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import org.json.JSONObject

class KeyMapService : AccessibilityService() {

    private lateinit var profile: JSONObject
    private var inGame = false
    private val pressed = mutableSetOf<Int>()

    // Joystick state
    private var stroke: StrokeDescription? = null
    private var last: PointF? = null
    private var busy = false
    private var dirty = false

    override fun onServiceConnected() {
        profile = JSONObject(assets.open("profiles/play_together.json").bufferedReader().readText())
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val
