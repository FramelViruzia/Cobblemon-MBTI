package dev.cobblemonmbti;

/**
 * Where the badge goes. Pure arithmetic (no Minecraft/Cobblemon), so it can be unit-tested.
 *
 * Cobblemon draws the dark Nature box as part of its background texture, not in code, so these numbers were
 * measured from a screenshot at GUI scale x2 and cross-checked with Cobblemon's own constants
 * (InfoWidget row pitch 15, label text at x+8, value text at x+53 = box left + 3). All values are GUI pixels,
 * relative to the top-left corner of InfoWidget:
 *   value box: x+50 .. x+129, y+65 .. y+76  (11 high; the row slot is 15 high, the box starts 5 below it)
 * A resource pack can override them with assets/cobblemon_mbti/mbti/layout.json.
 */
public final class MbtiLayout {
    private MbtiLayout() {}

    /** Height of capital letters in Minecraft's default font, used to centre the text vertically. */
    public static final int CAP_HEIGHT = 7;

    public static final class Geometry {
        public static final Geometry DEFAULT = new Geometry(65, 11, 129);

        /** Top edge of the Nature value box, relative to the widget's y. */
        public final int top;
        /** Height of the Nature value box. */
        public final int height;
        /** Right edge (exclusive) of the Nature value box, relative to the widget's x. */
        public final int right;

        public Geometry(int top, int height, int right) {
            this.top = top;
            this.height = Math.max(1, height);
            this.right = right;
        }
    }

    public static final class Placement {
        /** Background rectangle, integer GUI pixels, right and bottom exclusive (as GuiGraphics.fill expects). */
        public final int left, top, right, bottom;
        /** Where the scaled text starts (integer GUI pixels). */
        public final int textX, textY;

        Placement(int left, int top, int right, int bottom, int textX, int textY) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.textX = textX;
            this.textY = textY;
        }
    }

    /**
     * @param textWidth  unscaled width of the text in Minecraft's font
     * @param scale      text scale (the rectangle itself is never scaled, so its edges stay on whole pixels)
     * @param padX       gap between the rectangle's edge and the text on each side
     * @param rightInset how far the badge's right edge sits inside the Nature box's right edge (0 = flush)
     */
    public static Placement place(Geometry g, int widgetX, int widgetY, int textWidth, float scale, int padX,
                                  int rightInset, int offsetX, int offsetY) {
        int width = Math.round(textWidth * scale) + 2 * padX;
        int right = widgetX + g.right - rightInset + offsetX;
        int left = right - width;
        int top = widgetY + g.top + offsetY;
        int bottom = top + g.height;
        int textX = left + Math.round((width - textWidth * scale) / 2.0f);
        int textY = top + Math.round((g.height - CAP_HEIGHT * scale) / 2.0f);
        return new Placement(left, top, right, bottom, textX, textY);
    }
}
