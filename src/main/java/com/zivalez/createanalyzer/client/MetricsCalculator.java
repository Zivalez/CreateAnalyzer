package com.zivalez.createanalyzer.client;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.reflect.Method;
import java.util.*;

public class MetricsCalculator {

    public record Stats(
            String blockName,
            float rpm,
            float stressUse,
            float stressCap,
            int nodes,
            int consumers,
            int transmitters,
            int minY,
            int maxY,
            boolean approx
    ) {}

    private static final Map<String, Float> BASE_STRESS = Map.ofEntries(
            Map.entry("millstone", 8f),
            Map.entry("press", 8f),
            Map.entry("mixer", 8f),
            Map.entry("crushing", 16f),
            Map.entry("crusher", 16f),
            Map.entry("drill", 12f),
            Map.entry("fan", 4f),
            Map.entry("saw", 8f),
            Map.entry("deployer", 8f)
    );
    private static final Map<String, Float> BASE_CAPACITY = Map.ofEntries(
            Map.entry("water_wheel", 32f),
            Map.entry("windmill_bearing", 64f),
            Map.entry("steam_engine", 256f),
            Map.entry("flywheel", 128f)
    );

    private static Class<?> KINETIC_BE_CLASS;
    private static Method M_GET_SPEED;

    static {
        try {
            KINETIC_BE_CLASS = Class.forName("com.simibubi.create.content.kinetics.base.KineticBlockEntity");
            M_GET_SPEED = KINETIC_BE_CLASS.getMethod("getSpeed");
        } catch (Throwable ignored) {
            KINETIC_BE_CLASS = null;
            M_GET_SPEED = null;
        }
    }

    public static Stats sample(Level level, BlockPos start, int radius, int hardLimit) {
        if (level == null || start == null) return null;
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> q = new ArrayDeque<>();
        q.add(start);
        visited.add(start);

        int nodes = 0, cons = 0, trans = 0;
        int minY = start.getY(), maxY = start.getY();
        float stressUse = 0f, stressCap = 0f;

        float rpm = readSpeed(level, start);
        boolean approx = (M_GET_SPEED == null);

        int scanned = 0;
        while (!q.isEmpty()) {
            BlockPos p = q.poll();
            if (p.distManhattan(start) > radius) continue;

            var st = level.getBlockState(p);
            ResourceLocation id = level.registryAccess()
                    .registryOrThrow(net.minecraft.core.registries.Registries.BLOCK)
                    .getKey(st.getBlock());

            if (id != null && "create".equals(id.getNamespace())) {
                nodes++;
                String path = id.getPath();

                if (isTransmitter(path)) trans++;
                if (isConsumer(path)) {
                    cons++;
                    stressUse += guessStress(path);
                }
                stressCap += guessCapacity(path);

                minY = Math.min(minY, p.getY());
                maxY = Math.max(maxY, p.getY());

                for (var d : net.minecraft.core.Direction.values()) {
                    BlockPos np = p.relative(d);
                    if (visited.add(np)) q.add(np);
                }
            } else {
                for (var d : net.minecraft.core.Direction.values()) {
                    BlockPos np = p.relative(d);
                    if (visited.add(np)) q.add(np);
                }
            }

            scanned++;
            if (scanned > hardLimit) break;
        }

        return new Stats(displayName(level, start), rpm, stressUse, stressCap,
                nodes, cons, trans, minY, maxY, approx);
    }

    private static String displayName(Level level, BlockPos pos) {
        var st = level.getBlockState(pos);
        var key = level.registryAccess()
                .registryOrThrow(net.minecraft.core.registries.Registries.BLOCK)
                .getKey(st.getBlock());
        return key == null ? "unknown" : key.toString();
    }

    private static boolean isTransmitter(String path) {
        return path.contains("shaft") || path.contains("cogwheel") || path.contains("gearbox")
                || path.contains("chain_drive") || path.contains("clutch") || path.contains("gearshift");
    }

    private static boolean isConsumer(String path) {
        return BASE_STRESS.keySet().stream().anyMatch(path::contains);
    }

    private static float guessStress(String path) {
        for (var e : BASE_STRESS.entrySet()) if (path.contains(e.getKey())) return e.getValue();
        return 0f;
    }

    private static float guessCapacity(String path) {
        for (var e : BASE_CAPACITY.entrySet()) if (path.contains(e.getKey())) return e.getValue();
        return 0f;
    }

    private static float readSpeed(Level level, BlockPos pos) {
        if (KINETIC_BE_CLASS == null || M_GET_SPEED == null) return 0f;
        try {
            var be = level.getBlockEntity(pos);
            if (be == null) return 0f;
            if (KINETIC_BE_CLASS.isInstance(be)) {
                Object val = M_GET_SPEED.invoke(be);
                if (val instanceof Number n) return n.floatValue();
            }
        } catch (Throwable ignored) {}
        return 0f;
    }
}
