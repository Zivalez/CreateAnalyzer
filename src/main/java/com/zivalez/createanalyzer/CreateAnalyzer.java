package com.zivalez.createanalyzer;

import com.zivalez.createanalyzer.client.Keybinds;
import com.zivalez.createanalyzer.client.hud.MetricsOverlay;
import com.zivalez.createanalyzer.config.ClientConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.javafmlmod.FMLJavaModLoadingContext;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod("createanalyzer")
public class CreateAnalyzer {
    public CreateAnalyzer(ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(com.zivalez.createanalyzer.client.ClientModEvents::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.addListener((InputEvent.Key e) -> Keybinds.handle(e));
        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Post e) -> MetricsOverlay.render(e.getGuiGraphics()));
    }
}
