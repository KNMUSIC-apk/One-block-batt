package com.oneblock.visuals.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public final class Text {

    private static final LegacyComponentSerializer LEGACY =
            LegacyComponentSerializer.legacyAmpersand();

    private Text() {}

    public static Component c(String legacy) {
        return LEGACY.deserialize(legacy == null ? "" : legacy);
    }

    public static String color(String legacy) {
        return legacy == null ? "" : legacy.replace('&', '§');
    }
}
