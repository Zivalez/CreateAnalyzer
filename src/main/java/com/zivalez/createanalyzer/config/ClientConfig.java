package com.zivalez.createanalyzer.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ClientConfig {

    public enum Anchor { BR, BL, TR, TL }
    public enum Theme { COMPACT, EXPANDED }

    public static final ModConfigSpec SPEC;
    public static final ClientConfig CONFIG;

    static {
        Pair<ClientConfig, ModConfigSpec> pair = new ModConfigSpec.Builder()
                .configure(ClientConfig::new);
        CONFIG = pair.getLeft();
        SPEC = pair.getRight();
    }

    public final ModConfigSpec.BooleanValue overlayEnabled;
    public final ModConfigSpec.EnumValue<Anchor> anchor;
    public final ModConfigSpec.IntValue offsetX;
    public final ModConfigSpec.IntValue offsetY;
    public final ModConfigSpec.DoubleValue scale;
    public final ModConfigSpec.DoubleValue opacity;
    public final ModConfigSpec.BooleanValue showRPM;
    public final ModConfigSpec.BooleanValue showStress;
    public final ModConfigSpec.BooleanValue showNodes;
    public final ModConfigSpec.BooleanValue connectedOnly;
    public final ModConfigSpec.IntValue scanRadius;
    public final ModConfigSpec.IntValue scanLimit;
    public final ModConfigSpec.IntValue sampleIntervalTicks;
    public final ModConfigSpec.EnumValue<Theme> theme;
    public final ModConfigSpec.IntValue decimals;
    public final ModConfigSpec.IntValue accentColor;

    private ClientConfig(ModConfigSpec.Builder b) {
        b.push("overlay");
        overlayEnabled = b.define("enabled", true);
        anchor = b.defineEnum("anchor", Anchor.BR);
        offsetX = b.defineInRange("offsetX", 8, 0, 10000);
        offsetY = b.defineInRange("offsetY", 8, 0, 10000);
        scale = b.defineInRange("scale", 1.0, 0.5, 3.0);
        opacity = b.defineInRange("opacity", 0.35, 0.0, 1.0);
        theme = b.defineEnum("theme", Theme.COMPACT);
        decimals = b.defineInRange("decimals", 1, 0, 3);
        accentColor = b.defineInRange("accentColor", 0xFF00FFFF, 0x00000000, 0xFFFFFFFF);
        b.pop();

        b.push("content");
        showRPM = b.define("showRPM", true);
        showStress = b.define("showStress", true);
        showNodes = b.define("showNodes", true);
        connectedOnly = b.define("connectedOnly", false);
        b.pop();

        b.push("performance");
        scanRadius = b.defineInRange("scanRadius", 16, 4, 128);
        scanLimit = b.defineInRange("scanLimit", 2048, 256, 20000);
        sampleIntervalTicks = b.defineInRange("sampleIntervalTicks", 1, 1, 20);
        b.pop();
    }
}
