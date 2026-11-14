package dev.cobblemonmbti;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Draws the MBTI badge flush with the right end of the Nature value box in Cobblemon's Summary > Info tab.
 * The coloured rectangle uses whole GUI pixels and the same height as the Nature box; only the text is scaled.
 */
public final class MbtiBadgeRenderer {
    private MbtiBadgeRenderer() {}

    private static boolean failed = false;

    /** Called from the mixin at the end of InfoWidget.renderWidget. Must never throw. */
    public static void render(GuiGraphics graphics, Pokemon pokemon, int widgetX, int widgetY) {
        if (failed) return;
        try {
            MbtiConfig cfg = MbtiConfig.get();
            if (!cfg.enabled) return;

            MbtiTable.Result result = MbtiLookup.resolve(pokemon);
            String text;
            if (result == null) {
                if (!cfg.showUnknownAsNa) return;
                text = "N/A";
            } else {
                text = result.mbti;
            }

            int rgb = cfg.backgroundRgb(MbtiColors.groupOf(text));
            Font font = Minecraft.getInstance().font;
            MbtiLayout.Placement p = MbtiLayout.place(MbtiData.layout(), widgetX, widgetY, font.width(text),
                    cfg.badgeScale, cfg.textPaddingX, cfg.boxRightInset, cfg.offsetX, cfg.offsetY);

            fillBadge(graphics, p, rgb, cfg.boxRightInset == 0);

            PoseStack pose = graphics.pose();
            pose.pushPose();
            pose.translate((float) p.textX, (float) p.textY, 0.0f);
            pose.scale(cfg.badgeScale, cfg.badgeScale, 1.0f);
            graphics.drawString(font, text, 0, 0, 0xFF000000 | cfg.textRgb(), cfg.textShadow);
            pose.popPose();
        } catch (Throwable t) {
            failed = true;
            CobblemonMbtiClient.LOGGER.error("MBTI badge rendering failed and has been switched off until the next resource reload", t);
        }
    }

    /**
     * Cobblemon rounds the Nature box's outer corners by painting each corner pixel halfway between the box and the
     * background. When the badge is flush with that edge, its right corners get the same look: drawn at 50% alpha so
     * they blend with whatever is behind them, including resource pack artwork.
     */
    private static void fillBadge(GuiGraphics graphics, MbtiLayout.Placement p, int rgb, boolean roundRightCorners) {
        int opaque = 0xFF000000 | rgb;
        if (!roundRightCorners || p.right - p.left < 2 || p.bottom - p.top < 3) {
            graphics.fill(p.left, p.top, p.right, p.bottom, opaque);
            return;
        }
        int edge = p.right - 1;
        graphics.fill(p.left, p.top, edge, p.bottom, opaque);
        graphics.fill(edge, p.top + 1, p.right, p.bottom - 1, opaque);
        int half = 0x80000000 | rgb;
        graphics.fill(edge, p.top, p.right, p.top + 1, half);
        graphics.fill(edge, p.bottom - 1, p.right, p.bottom, half);
    }

    /** Lets /mbti reload re-enable rendering after a failure. */
    public static void resetFailure() {
        failed = false;
    }
}
