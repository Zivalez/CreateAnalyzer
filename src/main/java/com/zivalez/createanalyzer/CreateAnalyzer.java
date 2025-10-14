package com.zivalez.createanalyzer;

import com.zivalez.createanalyzer.client.Keybinds;
import com.zivalez.createanalyzer.client.ClientModEvents;
import com.zivalez.createanalyzer.client.hud.MetricsOverlay;
import com.zivalez.createanalyzer.config.ClientConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod("createanalyzer")
public class CreateAnalyzer {
    public CreateAnalyzer(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);

        modBus.addListener(ClientModEvents::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener((InputEvent.Key e) ->
                Keybinds.handle(e));
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post e) ->
                MetricsOverlay.render(e.getGuiGraphics()));
    }
}
