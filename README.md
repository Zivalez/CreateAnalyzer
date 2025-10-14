# Create Analyzer — Lite + Config (NeoForge 1.21.1)

**Fixed3**: Hilangkan ketergantungan `TickEvent` & `@Mod.EventBusSubscriber`. Listener:
- MOD bus: keybind register via `FMLJavaModLoadingContext.get().getModEventBus().addListener(...)`
- NeoForge bus: input & render via `NeoForge.EVENT_BUS.addListener(...)`
Sampling dilakukan di `RenderGuiEvent.Post`.

Untuk CI: Gradle 8.13 + JDK 17; settings.gradle menambahkan `libraries.minecraft.net` + `PREFER_PROJECT`.
