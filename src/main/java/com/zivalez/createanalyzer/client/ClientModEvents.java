package com.zivalez.createanalyzer.client;

import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

public class ClientModEvents {
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent e) {
        Keybinds.register(e);
    }
}
