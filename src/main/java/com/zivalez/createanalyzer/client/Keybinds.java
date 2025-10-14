package com.zivalez.createanalyzer.client;

import com.zivalez.createanalyzer.client.hud.MetricsOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;

public class Keybinds {
    public static final KeyMapping TOGGLE_METRICS = new KeyMapping(
            "key.createanalyzer.toggle_metrics",
            GLFW.GLFW_KEY_K,
            "key.categories.createanalyzer"
    );

    public static void register(RegisterKeyMappingsEvent e) {
        e.register(TOGGLE_METRICS);
    }

    public static void handle(InputEvent.Key e) {
        if (TOGGLE_METRICS.consumeClick()) {
            boolean on = MetricsOverlay.toggle();
            var player = Minecraft.getInstance().player;
            if (player != null) {
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                                on ? "msg.createanalyzer.metrics_on" : "msg.createanalyzer.metrics_off"
                        ),
                        true
                );
            }
        }
    }
}
