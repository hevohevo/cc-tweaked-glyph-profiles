// SPDX-FileCopyrightText: 2021 The CC: Tweaked Developers
//
// SPDX-License-Identifier: MPL-2.0

package dan200.computercraft.client.render;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.client.gui.GuiSprites;
import dan200.computercraft.client.render.monitor.MonitorTextureBufferShader;
import dan200.computercraft.client.render.text.TerminalCharset;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Shared {@link RenderType}s used throughout the mod.
 */
public class RenderTypes {
    public static final int FULL_BRIGHT_LIGHTMAP = (0xF << 4) | (0xF << 20);

    private static @Nullable MonitorTextureBufferShader monitorTboShader;
    private static @Nullable ResourceLocation terminalFont;
    private static @Nullable RenderType terminal;
    private static @Nullable RenderType printoutText;
    private static @Nullable ResourceLocation monitorTboFont;
    private static @Nullable RenderType monitorTbo;

    /**
     * Renders a fullbright terminal.
     *
     * @return The render type for the currently selected terminal font.
     */
    public static RenderType terminal() {
        var font = TerminalCharset.getFont();
        if (terminal == null || !font.equals(terminalFont)) {
            terminalFont = font;
            terminal = RenderType.text(font);
        }
        return terminal;
    }

    /**
     * Renders a monitor with the TBO shader.
     *
     * @return The render type for monitor TBO rendering with the current terminal font.
     * @see MonitorTextureBufferShader
     */
    public static RenderType monitorTbo() {
        var font = TerminalCharset.getFont();
        if (monitorTbo == null || !font.equals(monitorTboFont)) {
            monitorTboFont = font;
            monitorTbo = Types.createMonitorTbo(font);
        }
        return monitorTbo;
    }

    /**
     * A variant of {@link #terminal()} which uses the lightmap rather than rendering fullbright.
     *
     * @return The render type for printout text with the current terminal font.
     */
    public static RenderType printoutText() {
        var font = TerminalCharset.getFont();
        if (printoutText == null || !font.equals(terminalFont)) {
            terminalFont = font;
            printoutText = RenderType.text(font);
            terminal = printoutText;
        }
        return printoutText;
    }

    /**
     * Printout's background texture. {@link RenderType#text(ResourceLocation)} is a <em>little</em> questionable, but
     * it is what maps use, so should behave the same as vanilla in both item frames and in-hand.
     */
    public static final RenderType PRINTOUT_BACKGROUND = RenderType.text(new ResourceLocation("computercraft", "textures/gui/printout.png"));

    /**
     * Render type for {@linkplain GuiSprites GUI sprites}.
     */
    public static final RenderType GUI_SPRITES = RenderType.text(GuiSprites.TEXTURE);

    public static MonitorTextureBufferShader getMonitorTextureBufferShader() {
        if (monitorTboShader == null) throw new NullPointerException("MonitorTboShader has not been registered");
        return monitorTboShader;
    }

    public static ShaderInstance getTerminalShader() {
        return Objects.requireNonNull(GameRenderer.getRendertypeTextShader(), "Text shader has not been registered");
    }

    public static void registerShaders(ResourceProvider resources, BiConsumer<ShaderInstance, Consumer<ShaderInstance>> load) throws IOException {
        load.accept(
            new MonitorTextureBufferShader(
                resources,
                ComputerCraftAPI.MOD_ID + "/monitor_tbo",
                monitorTbo().format()
            ),
            x -> monitorTboShader = (MonitorTextureBufferShader) x
        );
    }

    private static final class Types extends RenderType {
        private static RenderType createMonitorTbo(ResourceLocation font) {
            var termFontTexture = new TextureStateShard(font, false, false);

            return RenderType.create(
                "monitor_tbo", DefaultVertexFormat.POSITION_TEX, VertexFormat.Mode.TRIANGLE_STRIP, 128,
                false, false, // useDelegate, needsSorting
                RenderType.CompositeState.builder()
                    .setTextureState(termFontTexture)
                    .setShaderState(new ShaderStateShard(RenderTypes::getMonitorTextureBufferShader))
                    .createCompositeState(false)
            );
        }

        @SuppressWarnings("UnusedMethod")
        private Types(String name, VertexFormat format, VertexFormat.Mode mode, int buffer, boolean crumbling, boolean sort, Runnable setup, Runnable teardown) {
            super(name, format, mode, buffer, crumbling, sort, setup, teardown);
        }
    }
}
