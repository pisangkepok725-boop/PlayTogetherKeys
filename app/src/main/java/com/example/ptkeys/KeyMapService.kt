        if (pressed.contains(code(joy.getString("up")))) dy -= 1f
        if (pressed.contains(code(joy.getString("down")))) dy += 1f
        if (pressed.contains(code(joy.getString("left")))) dx -= 1f
        if (pressed.contains(code(joy.getString("right")))) dx += 1f
        val len = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        val prev = last
        val fromX = if (prev != null) prev.x else cx
        val fromY = if (prev != null) prev.y else cy

        val path = Path()
        path.moveTo(fromX, fromY)
        val cont: Boolean
        if (len == 0f) {
            if (stroke == null) return
            path.lineTo(fromX, fromY)
            cont = false
            last = null
        } else {
            val tx = cx + dx / len * r
            val ty = cy + dy / len * r
            path.lineTo(tx, ty)
            cont = true
            last = PointF(tx, ty)
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
                    updateJoystick()
                }
            }

            override fun onCancelled(g: GestureDescription?) {
                busy = false
                stroke = null
                last = null
            }
        }
        val ok = dispatchGesture(GestureDescription.Builder().addStroke(s).build(), cb, null)
        if (!ok) {
            busy = false
            stroke = null
            last = null
        }
    }
}
