// SPDX-FileCopyrightText: 2026 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.render.text;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dan200.computercraft.shared.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Maps Unicode BMP characters onto the terminal's 8-bit code page.
 *
 * <p>The terminal font can only display 256 glyphs, so all supported input characters must be
 * mapped onto a glyph index in the range {@code 0..255}. That mapping is defined by the configured
 * glyph map JSON, defaulting to {@code glyph_map_ascii.json}.
 *
 * <p>This class is intentionally data-driven. Language-specific aliases such as katakana, full-width
 * ASCII, or other compatibility forms should be listed in the JSON file as additional entries which
 * point at the same glyph index as the primary character.
 *
 * <p>At load time we build a lookup table indexed directly by Java {@code char}. At runtime,
 * conversion is just:
 *
 * <pre>{@code
 * int glyph = TerminalCharset.map(ch);
 * }</pre>
 *
 * <p>The returned value is always in the range {@code 0..255}. Unsupported characters map to
 * {@code fallbackGlyph}.
 */
public final class TerminalCharset {
    private static final Logger LOG = LoggerFactory.getLogger(TerminalCharset.class);

    private static final int TABLE_SIZE = 65536;
    private static final int DEFAULT_FALLBACK = '?';

    private static final ResourceLocation DEFAULT_MAP_PATH =
        new ResourceLocation("computercraft", "textures/gui/glyph_map_ascii.json");
    private static final ResourceLocation DEFAULT_FONT =
        new ResourceLocation("computercraft", "textures/gui/term_font.png");

    private static final int[] GLYPH_LUT = new int[TABLE_SIZE];

    private static boolean loaded = false;
    private static int fallbackGlyph = DEFAULT_FALLBACK;
    private static ResourceLocation font = DEFAULT_FONT;
    private static int imeEnterDelay = 0;

    private TerminalCharset() {
    }

    public static int map(char ch) {
        if (!loaded) load();
        return GLYPH_LUT[ch];
    }

    public static boolean isSupported(char ch) {
        if (!loaded) load();
        return GLYPH_LUT[ch] != fallbackGlyph;
    }

    public static int getFallbackGlyph() {
        if (!loaded) load();
        return fallbackGlyph;
    }

    public static ResourceLocation getFont() {
        if (!loaded) load();
        return font;
    }

    public static int getImeEnterDelay() {
        if (!loaded) load();
        return imeEnterDelay;
    }

    public static void reload() {
        loaded = false;
    }

    private static synchronized void load() {
        if (loaded) return;

        initDefaults();

        var mapPath = getConfiguredMapPath();

        try {
            var resourceManager = Minecraft.getInstance().getResourceManager();
            var resource = resourceManager.getResource(mapPath);

            if (resource.isEmpty()) {
                LOG.warn("Terminal glyph map {} was not found, using fallback mapping.", mapPath);
                loaded = true;
                return;
            }

            try (var reader = new InputStreamReader(resource.get().open(), StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();

                fallbackGlyph = clampGlyph(getInt(root, "fallbackGlyph", DEFAULT_FALLBACK));
                Arrays.fill(GLYPH_LUT, fallbackGlyph);

                font = parseFont(root);
                imeEnterDelay = Math.max(0, getInt(root, "imeEnterDelay", 0));

                JsonObject map = root.getAsJsonObject("map");
                if (map == null) {
                    LOG.warn("Terminal glyph map {} has no \"map\" object, using fallback mapping.", mapPath);
                } else {
                    applyMappings(map);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to load terminal glyph map {}. Falling back to default mapping.", mapPath, e);
            initDefaults();
        }

        loaded = true;
    }

    private static void initDefaults() {
        fallbackGlyph = DEFAULT_FALLBACK;
        font = DEFAULT_FONT;
        imeEnterDelay = 0;
        Arrays.fill(GLYPH_LUT, fallbackGlyph);
    }

    private static ResourceLocation getConfiguredMapPath() {
        var configured = Config.terminalGlyphMap == null ? "" : Config.terminalGlyphMap.trim();
        if (configured.isEmpty()) return DEFAULT_MAP_PATH;

        try {
            return configured.contains(":")
                ? new ResourceLocation(configured)
                : new ResourceLocation("computercraft", configured);
        } catch (IllegalArgumentException e) {
            LOG.warn("Invalid terminal glyph map resource \"{}\", falling back to {}.", configured, DEFAULT_MAP_PATH);
            return DEFAULT_MAP_PATH;
        }
    }

    private static void applyMappings(JsonObject map) {
        for (var entry : map.entrySet()) {
            int codePoint = parseCodePoint(entry.getKey());
            if (!isBmpCodePoint(codePoint)) {
                LOG.warn("Ignoring non-BMP mapping key {}", entry.getKey());
                continue;
            }

            JsonObject value = entry.getValue().getAsJsonObject();
            validateCharField(codePoint, value);

            int glyph = clampGlyph(getInt(value, "glyph", fallbackGlyph));
            GLYPH_LUT[codePoint] = glyph;
        }
    }

    private static void validateCharField(int expectedCodePoint, JsonObject value) {
        if (!value.has("char")) return;

        String chars = value.get("char").getAsString();
        if (chars.codePointCount(0, chars.length()) != 1) {
            throw new IllegalArgumentException(
                "Expected a single character for " + toUPlus(expectedCodePoint) + ", got \"" + chars + "\"."
            );
        }

        int actual = chars.codePointAt(0);
        if (actual != expectedCodePoint) {
            throw new IllegalArgumentException(
                "Character mismatch for " + toUPlus(expectedCodePoint) + ": got \"" + chars + "\"."
            );
        }
    }

    private static ResourceLocation parseFont(JsonObject root) {
        if (!root.has("font")) return DEFAULT_FONT;

        String value = root.get("font").getAsString().trim();
        if (value.isEmpty()) return DEFAULT_FONT;

        if (value.contains(":")) return new ResourceLocation(value);
        return new ResourceLocation("computercraft", "textures/gui/" + value);
    }

    private static int parseCodePoint(String value) {
        if (value == null || !value.startsWith("U+")) {
            throw new IllegalArgumentException("Expected U+XXXX code point, got \"" + value + "\".");
        }

        return Integer.parseInt(value.substring(2), 16);
    }

    private static boolean isBmpCodePoint(int codePoint) {
        return codePoint >= 0 && codePoint < TABLE_SIZE;
    }

    private static int clampGlyph(int value) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException("Glyph value must be between 0 and 255, got " + value + ".");
        }
        return value;
    }

    private static int getInt(JsonObject object, String key, int defaultValue) {
        return object.has(key) ? object.get(key).getAsInt() : defaultValue;
    }

    private static String toUPlus(int codePoint) {
        return String.format("U+%04X", codePoint);
    }
}
