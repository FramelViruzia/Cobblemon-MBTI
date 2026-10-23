package dev.cobblemonmbti;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Species;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds a report from the species and forms the game really has loaded (Cobblemon, plus whatever add-on
 * species/form data is installed), and compares it with the MBTI table. The species collection is reached by reflection so that a change in
 * Cobblemon's registry class cannot break the build.
 */
public final class MbtiDump {
    private MbtiDump() {}

    public static String writeReport() {
        try {
            Collection<?> all = allSpecies();
            MbtiTable table = MbtiData.table();
            StringBuilder out = new StringBuilder();
            Set<String> gameKeys = new HashSet<>();
            List<String> withoutEntry = new ArrayList<>();
            List<String> formNotes = new ArrayList<>();
            List<String> aliasNotes = new ArrayList<>();
            List<String> catalogue = new ArrayList<>();

            for (Object o : all) {
                Species s = (Species) o;
                String path = s.getResourceIdentifier().getPath();
                String k1 = MbtiKeys.norm(path);
                String k2 = MbtiKeys.norm(s.getName());
                gameKeys.add(k1);
                gameKeys.add(k2);
                MbtiTable.Entry e = table.get(k1) != null ? table.get(k1) : table.get(k2);

                Set<String> formNames = new TreeSet<>();
                Set<String> aspectTokens = new TreeSet<>();
                for (FormData f : s.getForms()) {
                    formNames.add(f.getName());
                    for (String a : f.getAspects()) aspectTokens.add(MbtiKeys.stripSuffix(MbtiKeys.norm(a)));
                }
                catalogue.add(s.getResourceIdentifier() + " | " + s.getName() + " | standardForm='"
                        + s.getStandardForm().getName() + "' | forms=" + formNames + " | aspects=" + aspectTokens
                        + " | features=" + s.getFeatures());

                if (e == null || (e.base == null && e.forms.isEmpty())) {
                    withoutEntry.add(path);
                    continue;
                }
                Set<String> normForms = new HashSet<>();
                for (String n : formNames) normForms.add(MbtiKeys.norm(n));
                for (String key : e.forms.keySet()) {
                    boolean viaForm = normForms.stream().anyMatch(n -> key.equals(MbtiTable.matchForm(e, n)));
                    boolean viaAspect = aspectTokens.contains(key);
                    boolean viaGender = key.equals("male") || key.equals("female");
                    if (!viaForm && !viaAspect && !viaGender) {
                        formNotes.add(path + ": MBTI form key '" + key + "' matches no form name or aspect the species declares "
                                + "(forms=" + formNames + ", aspects=" + aspectTokens + ") - check with 'debug': true in game");
                    }
                }
                if (e.standard != null) {
                    aliasNotes.add(path + ": no base MBTI, standard form '" + s.getStandardForm().getName()
                            + "' is shown as '" + e.standard + "' (forms in game: " + formNames + ")");
                }
            }

            List<String> orphanKeys = new ArrayList<>();
            for (Map.Entry<String, MbtiTable.Entry> te : table.all().entrySet()) {
                if (!gameKeys.contains(te.getKey())) orphanKeys.add(te.getKey());
            }

            out.append("Cobblemon MBTI report\n=====================\n");
            out.append("Species loaded in game: ").append(all.size()).append('\n');
            out.append("Species in MBTI table:  ").append(table.size()).append("\n\n");
            out.append("-- Species in game with no MBTI at all (").append(withoutEntry.size()).append(") --\n");
            withoutEntry.forEach(x -> out.append(x).append('\n'));
            out.append("\n-- MBTI table keys that match no species in game (").append(orphanKeys.size()).append(") --\n");
            orphanKeys.forEach(x -> out.append(x).append('\n'));
            out.append("\n-- MBTI form keys that match no form/aspect the species declares (").append(formNotes.size()).append(") --\n");
            formNotes.forEach(x -> out.append(x).append('\n'));
            out.append("\n-- Species using a standard-form alias (").append(aliasNotes.size()).append(") --\n");
            aliasNotes.forEach(x -> out.append(x).append('\n'));
            out.append("\n-- Full catalogue: id | name | standard form | forms | aspects | features --\n");
            catalogue.forEach(x -> out.append(x).append('\n'));

            Path file = FabricLoader.getInstance().getConfigDir().resolve("cobblemon_mbti_report.txt");
            Files.writeString(file, out.toString(), StandardCharsets.UTF_8);
            return "report written to " + file + " (" + withoutEntry.size() + " species without MBTI, "
                    + orphanKeys.size() + " unknown table keys, " + formNotes.size() + " form keys to check)";
        } catch (Throwable t) {
            CobblemonMbtiClient.LOGGER.error("MBTI dump failed", t);
            return "dump failed: " + t;
        }
    }

    private static Collection<?> allSpecies() throws Exception {
        Class<?> registry = Class.forName("com.cobblemon.mod.common.api.pokemon.PokemonSpecies");
        Object instance = registry.getField("INSTANCE").get(null);
        return (Collection<?>) registry.getMethod("getSpecies").invoke(instance);
    }
}
