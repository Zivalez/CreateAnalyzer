# Create Analyzer — Lite + Config (NeoForge 1.21.1)

Sudah include **pluginManagement** di `settings.gradle` agar plugin **net.neoforged.gradle.userdev** bisa di-resolve.

## Build via IntelliJ (tanpa install Gradle)
1) Settings → Gradle → Distribution: **Specified version** (8.8/8.9), Gradle JVM: **JDK 21**.
2) Reload Gradle Project.
3) Gradle Tool Window → Tasks → build → **build**.
4) Ambil JAR di `build/libs/` lalu taruh ke folder `mods/`.

Kalau error, pastikan koneksi ke `https://maven.neoforged.net/releases` tidak diblokir.
