# PlayTogether Keys

Aplikasi Android untuk memakai **keyboard** di game **Play Together** tanpa ADB, root, atau debugging.
Memakai Accessibility Service untuk menyuntikkan ketukan dan geseran layar.

## Cara pakai
1. Buka proyek di Android Studio, lalu Run (atau Build > Build APK).
2. Pasang APK, buka aplikasi, tekan tombol pengaturan, aktifkan **PlayTogether Keys**.
3. Sambungkan keyboard (OTG/Bluetooth), buka Play Together.

## Kontrol bawaan
| Tombol | Fungsi |
|---|---|
| W A S D | Joystick jalan |
| E / SPACE / F | Tombol aksi |
| T / M / I / ESC | Chat / Peta / Tas / Menu |
| Panah | Geser kamera |
| Klik kiri mouse | Ketukan biasa (bawaan Android) |

## Menyesuaikan posisi
Edit `app/src/main/assets/profiles/play_together.json`. Koordinat berupa pecahan layar (0.0 sampai 1.0),
jadi tidak tergantung resolusi. Posisi bawaan adalah perkiraan; sesuaikan dengan tata letak tombol di HP kamu.

## Catatan
- Tombol hanya aktif saat Play Together di layar depan.
- Penggunaan input otomatis bisa melanggar aturan game; gunakan dengan risiko sendiri.
