package com.example.ptkeys

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.PointF

fun tapAt(s: AccessibilityService, x: Float, y: Float) {
    val p = Path()
    p.moveTo(x, y)
    val st = GestureDescription.StrokeDescription(p, 0L, 60L)
    s.dispatchGesture(GestureDescription.Builder().addStroke(st).build(), null, null)
}

fun swipeAt(s: AccessibilityService, x1: Float, y1: Float, x2: Float, y2: Float) {
    val p = Path()
    p.moveTo(x1, y1)
    p.lineTo(x2, y2)
    val st = GestureDescription.StrokeDescription(p, 0L, 200L)
    s.dispatchGesture(GestureDescription.Builder().addStroke(st).build(), null, null)
}

class Joystick(
    private val service: AccessibilityService,
    private val cx: Float,
    private val cy: Float,
    private val r: Float
) {
    private var stroke: GestureDescription.StrokeDescription? = null
    private var lastPoint: PointF? = null
    private var busy = false
    private var dirty = false
    private var dx = 0f
    private var dy = 0f

    fun set(x: Float, y: Float) {
        dx = x
        dy = y
        update()
    }

    fun reset() {
        stroke = null
        lastPoint = null
        busy = false
        dirty = false
    }

    private fun update() {
        if (busy) {
            dirty = true
            return
        }
        val len = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
        val prev = lastPoint
        val fx = prev?.x ?: cx
        val fy = prev?.y ?: cy
        val path = Path()
        path.moveTo(fx, fy)
        val cont = len != 0f
        if (!cont) {
            if (stroke == null) return
            path.lineTo(fx, fy)
            lastPoint = null
        } else {
            val tx = cx + dx / len * r
            val ty = cy + dy / len * r
            path.lineTo(tx, ty)
            lastPoint = PointF(tx, ty)
        }
        val old = stroke
        val s = if (old != null) {
            old.continueStroke(path, 0L, 80L, cont)
        } else {
            GestureDescription.StrokeDescription(path, 0L, 80L, cont)
        }
        stroke = if (cont) s else null
        busy = true
        val cb = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(g: GestureDescription?) {
                busy = false
                if (dirty) {
                    dirty = false
                    update()
                }
            }

            override fun onCancelled(g: GestureDescription?) {
                busy = false
                stroke = null
                lastPoint = null
            }
        }
        val ok = service.dispatchGesture(GestureDescription.Builder().addStroke(s).build(), cb, null)
        if (!ok) {
            busy = false
            stroke = null
            lastPoint = null
        }
    }
}
