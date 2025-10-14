package com.zivalez.createanalyzer.client.hud;

import com.zivalez.createanalyzer.client.MetricsCalculator;
import com.zivalez.createanalyzer.client.RaycastUtil;
import com.zivalez.createanalyzer.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;

public class MetricsOverlay {
    private static boolean enabled = true;
    private static long lastSampleGameTime = -1;
    private static MetricsCalculator.Stats lastStats = null;
    private static BlockPos lastPos = null;

    public static boolean toggle() {
        enabled = !enabled;
        return enabled;
    }

    private static void sampleIfNeeded() {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.level == null || mc.player == null) {
            lastStats = null;
            lastPos = null;
            return;
        }
        long now = mc.level.getGameTime();
        if (now - lastSampleGameTime < ClientConfig.CONFIG.sampleIntervalTicks.get()) return;
        lastSampleGameTime = now;

        BlockHitResult bhr = RaycastUtil.getCrosshairBlock();
        if (bhr == null) {
            lastStats = null;
            lastPos = null;
            return;
        }
        BlockPos pos = bhr.getBlockPos();
        lastPos = pos;

        lastStats = MetricsCalculator.sample(
                mc.level,
                pos,
                ClientConfig.CONFIG.scanRadius.get(),
                ClientConfig.CONFIG.scanLimit.get()
        );
    }

    public static void render(GuiGraphics g) {
        if (!enabled || !ClientConfig.CONFIG.overlayEnabled.get()) return;
        var mc = Minecraft.getInstance();
        if (mc == null || mc.font == null) return;

        sampleIfNeeded();

        var font = mc.font;
        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        var anchor = ClientConfig.CONFIG.anchor.get();
        int pad = 8;
        int ox = ClientConfig.CONFIG.offsetX.get();
        int oy = ClientConfig.CONFIG.offsetY.get();
        double scale = ClientConfig.CONFIG.scale.get();
        float alpha = (float) Math.max(0.0, Math.min(1.0, ClientConfig.CONFIG.opacity.get()));
        int accent = ClientConfig.CONFIG.accentColor.get();

        String title = "Kinetic Metrics";
        String hint = "(arahkan crosshair ke blok Create)";
        String[] lines;

        int decimals = ClientConfig.CONFIG.decimals.get();
        String fmt = switch (decimals) {
            case 0 -> "%.0f";
            case 1 -> "%.1f";
            case 2 -> "%.2f";
            default -> "%.3f";
        };

        if (lastStats == null) {
            lines = new String[] { hint };
        } else {
            var s = lastStats;
            java.util.ArrayList<String> L = new java.util.ArrayList<>();
            L.add(ChatFormatting.AQUA + s.blockName());
            if (ClientConfig.CONFIG.showRPM.get()) {
                L.add("RPM: " + (s.rpm() == 0f ? "~0" : String.format(fmt, s.rpm())));
            }
            if (ClientConfig.CONFIG.showStress.get()) {
                float use = s.stressUse();
                float cap = s.stressCap();
                float net = cap - use;
                L.add("Use: " + String.format(fmt, use));
                L.add("Cap: " + String.format(fmt, cap));
                L.add((net >= 0 ? ChatFormatting.GREEN : ChatFormatting.RED) + "Net: " + String.format(fmt, net));
            }
            if (ClientConfig.CONFIG.showNodes.get()) {
                L.add("Nodes: " + s.nodes() + "  (T:" + s.transmitters() + "  C:" + s.consumers() + ")");
                L.add("Y: " + s.minY() + ".." + s.maxY());
            }
            if (s.approx()) {
                L.add(ChatFormatting.DARK_GRAY + "[approx; install Create deobf for exact RPM/stress]");
            }
            lines = L.toArray(new String[0]);
        }

        int w = 0;
        int h = 0;
        int lineH = ClientConfig.CONFIG.theme.get() == ClientConfig.Theme.COMPACT ? 10 : 12;
        for (String l : lines) {
            w = Math.max(w, font.width(l));
            h += lineH;
        }
        int titleW = font.width(title);
        w = Math.max(w, titleW);
        h += 12;

        int boxW = w + 12;
        int boxH = h + 10;

        int x = switch (anchor) {
            case BR, TR -> sw - (boxW + pad) - ox;
            case BL, TL -> pad + ox;
        };
        int y = switch (anchor) {
            case BR, BL -> sh - (boxH + pad) - oy;
            case TR, TL -> pad + oy;
        };

        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale((float)scale, (float)scale, 1f);

        int bg = ((int)(alpha * 255) << 24) | 0x000000;
        g.fill(0, 0, boxW, boxH, bg);
        g.fill(0, 0, 3, boxH, accent);

        g.drawString(font, title, 6, 6, 0xFFFFFF);

        int ty = 6 + 12;
        for (String l : lines) {
            g.drawString(font, l, 6, ty, 0xFFFFFF);
            ty += lineH;
        }

        g.pose().popPose();
    }
}
