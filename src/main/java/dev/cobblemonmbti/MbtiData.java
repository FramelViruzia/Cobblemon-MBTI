package dev.cobblemonmbti;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads assets/cobblemon_mbti/mbti/species.json from every resource pack. When several packs contain the file
 * they are merged species by species and form by form, so a pack can add or override single entries.
 */
public final class MbtiData {
    private static volatile MbtiTable table = new MbtiTable();
    private static volatile MbtiLayout.Geometry layout = MbtiLayout.Geometry.DEFAULT;
    /** Bumped on every reload so cached lookups know to refresh. */
    private static volatile int version = 0;

    private MbtiData() {}

    public static MbtiTable table() {
        return table;
    }

    public static int version() {
        return version;
    }

    public static MbtiLayout.Geometry layout() {
        return layout;
    }

    public static void load(ResourceManager manager) {
        MbtiTable fresh = new MbtiTable();
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CobblemonMbtiClient.MOD_ID, "mbti/species.json");
        List<Resource> stack = manager.getResourceStack(id);
        // Assumed order: lowest priority first, so later packs override earlier ones.
        for (Resource resource : stack) {
            try (Reader reader = resource.openAsReader()) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                if (root.has("species")) merge(fresh, root.getAsJsonObject("species"));
            } catch (Exception e) {
                CobblemonMbtiClient.LOGGER.error("Failed to read MBTI data from pack '{}'", resource.sourcePackId(), e);
            }
        }
        table = fresh;
        layout = loadLayout(manager);
        version++;
        CobblemonMbtiClient.LOGGER.info("Loaded MBTI data for {} species ({} pack file(s))", fresh.size(), stack.size());
    }

    /** assets/cobblemon_mbti/mbti/layout.json - the highest-priority pack wins. Falls back to the built-in values. */
    private static MbtiLayout.Geometry loadLayout(ResourceManager manager) {
        MbtiLayout.Geometry d = MbtiLayout.Geometry.DEFAULT;
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CobblemonMbtiClient.MOD_ID, "mbti/layout.json");
        Optional<Resource> resource = manager.getResource(id);
        if (resource.isEmpty()) return d;
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject o = JsonParser.parseReader(reader).getAsJsonObject();
            return new MbtiLayout.Geometry(intOr(o, "nature_box_top", d.top), intOr(o, "nature_box_height", d.height),
                    intOr(o, "nature_box_right", d.right));
        } catch (Exception e) {
            CobblemonMbtiClient.LOGGER.error("Failed to read layout.json from pack '{}' - using built-in layout",
                    resource.get().sourcePackId(), e);
            return d;
        }
    }

    private static int intOr(JsonObject o, String key, int fallback) {
        return o.has(key) && o.get(key).isJsonPrimitive() ? o.get(key).getAsInt() : fallback;
    }

    private static void merge(MbtiTable target, JsonObject species) {
        for (Map.Entry<String, JsonElement> se : species.entrySet()) {
            if (!se.getValue().isJsonObject()) continue;
            JsonObject o = se.getValue().getAsJsonObject();
            MbtiTable.Entry e = target.getOrCreate(se.getKey());
            if (o.has("base") && o.get("base").isJsonPrimitive()) e.base = o.get("base").getAsString().toUpperCase();
            if (o.has("standard") && o.get("standard").isJsonPrimitive()) e.standard = o.get("standard").getAsString();
            if (o.has("forms") && o.get("forms").isJsonObject()) {
                for (Map.Entry<String, JsonElement> fe : o.getAsJsonObject("forms").entrySet()) {
                    if (fe.getValue().isJsonPrimitive()) e.forms.put(fe.getKey(), fe.getValue().getAsString().toUpperCase());
                }
            }
        }
    }
}
