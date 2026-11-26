package com.example.ptkeys

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        root.addView(TextView(this).apply {
            textSize = 16f
            text = "PlayTogether Keys\n\n1. Tekan tombol di bawah\n2. Pilih \"PlayTogether Keys\" lalu aktifkan\n3. Sambungkan keyboard, buka Play Together\n\n" +
                "Kontrol: W A S D = jalan, E / SPACE / F = tombol aksi, T = chat, M = peta, I = tas, ESC = menu, " +
                "panah = geser kamera. Klik kiri mouse bekerja sebagai ketukan."
        })
        root.addView(Button(this).apply {
            text = "Buka Pengaturan Aksesibilitas"
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })
        setContentView(root)
    }
}
