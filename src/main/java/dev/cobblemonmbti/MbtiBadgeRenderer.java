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

            graphics.fill(p.left, p.top, p.right, p.bottom, 0xFF000000 | rgb);

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

    /** Lets /mbti reload re-enable rendering after a failure. */
    public static void resetFailure() {
        failed = false;
    }
}
