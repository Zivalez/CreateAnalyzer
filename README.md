# Create Analyzer (Lite + Config) — Blueprint (Single File)

> **Ringkasan:** Dokumen ini adalah blueprint teknis lengkap untuk mod client-only “Create Analyzer (Lite + Config)” pada Minecraft 1.21.1 + NeoForge 21.1.x. Bisa dipakai sebagai desain, acuan implementasi, dan README dev internal.


## 1) Tujuan & Nilai
- **Client-only HUD** untuk ekosistem **Create**:
  - Metrik cepat: **RPM/speed**, **perkiraan beban/stress**, **jumlah node**.
  - Mode **ringkas** dan **detail**, konfigurasi UI dalam game.
- **Aman dipakai di server** (sisi-klien saja).
- **Tahan modpack**: bila Create tidak ada, overlay diam—tidak crash.


## 2) Target & Kompatibilitas
- **Minecraft**: 1.21.1 (Java 21).
- **Loader**: **NeoForge** 21.1.x (min 21.0.0).
- **Create**: 6.0.x (direkomendasikan 6.0.6+) — **opsional**.
- **Connector/Fabric bridges**: tidak bergantung; tetap **graceful** jika hadir.


## 3) Fitur & UX
### Overlay HUD
- **Info utama** (ketika menyorot/mengunci blok Create):
  - **Speed/RPM** & arah.
  - **Perkiraan Stress**: konsumsi vs ketersediaan (indikator hijau/kuning/merah).
  - **Nodes**: jumlah komponen jaringan (approx/real berdasarkan ketersediaan API klien Create).
- **Mode tampilan**: **Compact** (1–2 baris) dan **Expanded** (panel kecil).
- **Tema & gaya**:
  - Tema **Light/Dark/Auto**.
  - **Opacity**, **padding**, **corner radius**.
  - **Scale** global (50%–200%).
  - **Anchor & offset** (Top-Left/Right/Bottom + X/Y).
- **Interaksi**:
  - Keybind **Toggle Overlay**.
  - Keybind **Cycle Theme/Mode** (opsional).
  - **Lock target** (opsional).

### Konfigurasi
- **In‑game config screen** (YACL/Cloth jika tersedia) + file TOML.
- File: `config/createanalyzer-client.toml`.
- Kategori: `ui`, `content`, `behaviour`, `perf`.
- Simpan `configVersion` untuk migrasi.

### Performa
- Sampling berat **tidak tiap frame** (gunakan `sampleEveryTicks`).
- **Cache** hasil query per target (TTL).
- Hindari alokasi objek di loop render.


## 4) Arsitektur & Paket
```
com.zivalez.createanalyzer
├─ CreateAnalyzer                // Entrypoint @Mod
├─ config/
│  ├─ ClientConfig               // ModConfigSpec, defaults, load/save
│  └─ ConfigData                 // Snapshot/POJO dari Spec
├─ hud/
│  ├─ OverlayRenderer            // Render per-frame, baca data & gambar
│  ├─ LayoutEngine               // Hitung posisi/ukuran/wrapping
│  ├─ Theme                      // Palet warna, font scale, opacity
│  └─ Widgets                    // Bar, badge, ikon rpm, dll
├─ input/
│  └─ Keybinds                   // Registrasi key mapping & handler
├─ integration/create/
│  ├─ CreatePresent              // Cek availability mod Create
│  ├─ KineticQuery               // Ambil speed/stress dari BE/Network
│  └─ NetworkEstimator           // BFS/estimasi node+stress jika data network klien tak ada
├─ probe/
│  └─ TargetSelector             // Raycast/select BE Create di crosshair
├─ util/
│  ├─ MathUtil, TextUtil, ColorUtil
│  └─ Cache<T>                   // TTL cache sederhana
└─ platform/
   └─ NeoForgeClientBus          // Registrasi event klien & overlay hooks
```

### Alur Data
1. **TargetSelector** raycast → `BlockEntity` Create yang valid.
2. **KineticQuery**:
   - Jika **client network** Create tersedia: ambil **speed**, **available/capacity**, **konsumsi** (read‑only, klien).
   - Jika tidak: **NetworkEstimator** lakukan BFS (dibatasi config) untuk estimasi **node** dan **stress**.
3. Hasil di‑cache (mis. 10–20 tick).
4. **OverlayRenderer** render berdasarkan data + `ClientConfig`.


## 5) Event & Hook NeoForge (Client)
- Daftar pada `FMLClientSetupEvent` / client mod bus:
  - **Keybinds**, **overlay layer** (event GUI overlay NeoForge pasca HUD).
- (Opsional) `ClientReloadListener` untuk reset tema/font saat resource reload.


## 6) Implementasi Kunci
### 6.1 Entrypoint & Registrasi Config
```java
@Mod("createanalyzer")
public final class CreateAnalyzer {
  public CreateAnalyzer(IEventBus modBus) {
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    modBus.addListener(NeoForgeClientBus::onClientSetup);
  }
}
```

### 6.2 Client Bus
```java
public final class NeoForgeClientBus {
  public static void onClientSetup(final FMLClientSetupEvent e) {
    Keybinds.register();
    // Registrasi overlay via event GUI overlay client NeoForge
    MinecraftForge.EVENT_BUS.register(OverlayRenderer.class);
  }
}
```

### 6.3 ClientConfig (TOML via ModConfigSpec)
Parameter (contoh):
- `ui.theme` = `"AUTO" | "LIGHT" | "DARK"`
- `ui.scale` = `1.0`
- `ui.anchor` = `"TOP_LEFT" | "TOP_RIGHT" | "BOTTOM_LEFT" | "BOTTOM_RIGHT"`
- `ui.offsetX`, `ui.offsetY` = `int`
- `content.showRPM`, `content.showStress`, `content.showNodes` = `boolean`
- `behaviour.hideInMenus`, `behaviour.onlyWhenHoldingGoggles` (opsional)
- `perf.maxBfsNodes` (mis. 256)
- `perf.sampleEveryTicks` (mis. 5)

Kerangka:
```java
public final class ClientConfig {
  public static final ModConfigSpec SPEC;
  public static final Values CFG;

  public static final class Values {
    public final EnumValue<Theme> theme;
    public final DoubleValue scale;
    public final IntValue offsetX, offsetY;
    public final BooleanValue showRPM, showStress, showNodes;
    public final IntValue sampleEveryTicks, maxBfsNodes;
    Values(ModConfigSpec.Builder b) {
      // define(...) default & range
    }
  }
  static {
    final Pair<Values, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Values::new);
    CFG = pair.getLeft();
    SPEC = pair.getRight();
  }
}
```

### 6.4 Integrasi Create
- **Deteksi**: `ModList.get().isLoaded("create")`.
- **RPM**: bila BE turunan `KineticBlockEntity` (atau API setara di 1.21.1), baca `getSpeed()` yang tersedia di klien.
- **Stress**:
  - Prefer data **network klien** jika tersedia.
  - Estimator BFS bila tidak tersedia:
    - Telusuri konektivitas (shaft/gearbox/belt) dengan batas `maxBfsNodes`.
    - Heuristik konsumsi/kapasitas sederhana per tipe blok (opsional, bisa dimatikan).

### 6.5 Render Overlay
- Hook: event GUI overlay pasca HUD.
- Panel berisi:
  - **Title** + ikon kecil (opsional).
  - Bar “Load” (consumption vs capacity) + status teks.
  - RPM & node count.
- Terapkan **tema**, **skala**, **anchor**, **offset** dari config.

### 6.6 Keybinds
- `toggleOverlay` (default: **O**)
- `cycleMode` (default: **SHIFT+O**)
- `lockTarget` (opsional: **ALT+O**)


## 7) Metadata & Build
### `META-INF/mods.toml`
```toml
modLoader="javafml"
loaderVersion="[4,)"
license="MIT"

[[mods]]
modId="createanalyzer"
version="${file.jarVersion}"
displayName="Create Analyzer (Lite + Config)"
authors="Zivalez"
description='''
Client-only HUD for Create metrics (RPM, stress estimates, nodes) with in-game config screen (Mods > Create Analyzer > Config).
Minimalist, informative design with theme, scale, and layout options.
'''

[[dependencies.createanalyzer]]
modId="neoforge"
type="required"
versionRange="[21.0.0,)"
ordering="NONE"
side="CLIENT"

[[dependencies.createanalyzer]]
modId="minecraft"
type="required"
versionRange="[1.21.1,)"
ordering="NONE"
side="CLIENT"

[[dependencies.createanalyzer]]
modId="create"
type="optional"
versionRange="[6.0.0,)"
ordering="NONE"
side="CLIENT"
```

### `gradle.properties` (contoh)
```properties
minecraft_version=1.21.1
neoforge_version=21.1.62
mappings_channel=official
mappings_version=1.21.1

mod_id=createanalyzer
mod_name=Create Analyzer (Lite + Config)
mod_version=0.3.3
group=com.zivalez.createanalyzer

org.gradle.java.installations.auto-download=true
org.gradle.jvmargs=-Xmx2g
```

### `build.gradle` (poin penting)
- Apply plugin NeoForge & Java.
- `processResources` untuk mengganti `${file.jarVersion}` → `mod_version`.
- Reobf jar untuk rilis.


## 8) Testing & QA
**Matrix uji:**
- NeoForge + Create + Create Analyzer (minimal).
- Modpack berat (Sodium/ModernFix/Connector): overlay tetap muncul; tidak ada konflik (kita tidak memakai mixin). 
- Tanpa Create: overlay **off**; tidak crash.

**Checklist:**
- [ ] Toggle overlay persist antar sesi.
- [ ] Tema/scale/anchor tersimpan & termuat ulang.
- [ ] Cache mencegah stutter saat target berubah cepat.
- [ ] Tidak ada alokasi besar di render loop (profil).
- [ ] TOML default terbentuk otomatis.


## 9) Known Issues & Mitigasi
- **Crash dari mod lain** (contoh _Particle Rain_ via Connector): bukan dari kita. Di README, sarankan update/disable mod penyebab bila overlay tidak muncul.
- Estimasi stress pada jaringan besar bisa meleset: beri label “≈” dan sediakan opsi mematikan estimator.


## 10) Roadmap
- Integrasi **Goggles** (opsional “onlyWhenGoggles”).
- Profil jaringan (mini breakdown).
- Export snapshot ke clipboard.
- Theme editor (warna custom).
- Drag & pin panel saat menahan tombol modifier.


## 11) Struktur Repo
```
src/main/java/com/zivalez/createanalyzer/...
src/main/resources/META-INF/mods.toml
src/main/resources/assets/createanalyzer/lang/en_us.json (id_id.json)
src/main/resources/assets/createanalyzer/textures/gui/icons.png (opsional)
LICENSE (MIT)
README.md (ringkas + gambar overlay)
CHANGELOG.md
```

---

## 12) Cara Memakai Dokumen Ini di Repo
- Simpan file ini sebagai **`DESIGN.md`** atau **`docs/BLUEPRINT.md`** di root repo.
- Tambahkan ke `README.md` baris: “Lihat **DESIGN.md** untuk detail teknis.”
- Commit:
  ```bash
  git add DESIGN.md
  git commit -m "docs: add Create Analyzer blueprint (single file)"
  git push
  ```

**Selesai.** Dokumen ini sudah siap dipakai satu-berkas.
