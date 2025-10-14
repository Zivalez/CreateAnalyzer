package com.zivalez.createanalyzer.client;

import com.zivalez.createanalyzer.client.hud.MetricsOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = "createanalyzer", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEvents {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key e) {
        Keybinds.handle(e);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            MetricsOverlay.onClientTick();
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post e) {
        MetricsOverlay.render(e.getGuiGraphics());
    }
}
