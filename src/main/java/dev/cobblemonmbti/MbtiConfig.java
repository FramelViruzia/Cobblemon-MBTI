package dev.cobblemonmbti;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/** Client config, stored in config/cobblemon_mbti.json. Missing keys keep their defaults. */
public final class MbtiConfig {
    private static final int CURRENT_VERSION = 2;

    /** Used to migrate old files. Do not edit. */
    public int configVersion = 0;
    /** Master switch. */
    public boolean enabled = true;
    /** Show a grey "N/A" badge when a Pokémon has no MBTI (default: hide the badge). */
    public boolean showUnknownAsNa = false;
    /** Text scale. The coloured box always has the same height as the Nature box. */
    public float badgeScale = 0.75f;
    /** Gap in pixels between the box edge and the text, on each side. */
    public int textPaddingX = 3;
    /** How far the badge's right edge sits inside the Nature box's right edge (0 = flush). */
    public int boxRightInset = 0;
    /** Extra shift in GUI pixels, e.g. if a resource pack moves the Nature row's artwork. */
    public int offsetX = 0;
    public int offsetY = 0;
    public boolean textShadow = true;
    /** Logs species, form, aspects and the lookup result of each Pokémon shown in the Info tab. */
    public boolean debug = false;

    public String analystColor = "#88619A";   // N + T  (purple)
    public String diplomatColor = "#33A474";  // N + F  (green)
    public String sentinelColor = "#4298B4";  // S + J  (blue)
    public String explorerColor = "#E4AE3A";  // S + P  (yellow)
    public String unknownColor = "#7A7A7A";   // role cannot be told (X letters, N/A)  (grey)
    public String textColor = "#FFFFFF";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile MbtiConfig instance = new MbtiConfig();

    public static MbtiConfig get() {
        return instance;
    }

    public static void reload() {
        Path file = FabricLoader.getInstance().getConfigDir().resolve("cobblemon_mbti.json");
        MbtiConfig loaded = null;
        if (Files.exists(file)) {
            try (Reader r = Files.newBufferedReader(file)) {
                loaded = GSON.fromJson(r, MbtiConfig.class);
            } catch (IOException | JsonParseException e) {
                CobblemonMbtiClient.LOGGER.warn("Could not read {} - using defaults", file, e);
            }
        }
        if (loaded == null) {
            loaded = new MbtiConfig();
        }
        if (loaded.configVersion < CURRENT_VERSION) {
            // Version 2 measures positions from the Nature box itself. Offsets tuned against the old layout
            // would now push the badge off the box, so start from zero.
            loaded.offsetX = 0;
            loaded.offsetY = 0;
            loaded.configVersion = CURRENT_VERSION;
        }
        loaded.badgeScale = Math.max(0.4f, Math.min(2.0f, loaded.badgeScale));
        loaded.textPaddingX = Math.max(0, Math.min(10, loaded.textPaddingX));
        instance = loaded;
        // write back so new options appear in the file and obsolete ones (rightPadding) disappear
        try (Writer w = Files.newBufferedWriter(file)) {
            GSON.toJson(loaded, w);
        } catch (IOException e) {
            CobblemonMbtiClient.LOGGER.warn("Could not write {}", file, e);
        }
    }

    public int backgroundRgb(MbtiColors.Group group) {
        switch (group) {
            case ANALYST:  return MbtiColors.parseRgb(analystColor, 0x88619A);
            case DIPLOMAT: return MbtiColors.parseRgb(diplomatColor, 0x33A474);
            case SENTINEL: return MbtiColors.parseRgb(sentinelColor, 0x4298B4);
            case EXPLORER: return MbtiColors.parseRgb(explorerColor, 0xE4AE3A);
            default:       return MbtiColors.parseRgb(unknownColor, 0x7A7A7A);
        }
    }

    public int textRgb() {
        return MbtiColors.parseRgb(textColor, 0xFFFFFF);
    }
}
