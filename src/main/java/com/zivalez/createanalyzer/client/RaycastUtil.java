package com.zivalez.createanalyzer.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

public class RaycastUtil {
    public static BlockHitResult getCrosshairBlock() {
        var mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return null;
        HitResult hr = mc.hitResult;
        if (hr instanceof BlockHitResult bhr) return bhr;
        return null;
    }
}
