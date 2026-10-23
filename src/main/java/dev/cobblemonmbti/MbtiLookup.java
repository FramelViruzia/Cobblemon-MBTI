package dev.cobblemonmbti;

import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Turns a Cobblemon Pokemon into the inputs of {@link MbtiTable#resolve}.
 * Everything read here is declared in Cobblemon 1.7.3 (Pokemon.kt, Species.kt, FormData.kt).
 */
public final class MbtiLookup {
    private MbtiLookup() {}

    // one-entry cache: the Info tab asks every frame for the same Pokemon
    private static Pokemon cachedPokemon;
    private static FormData cachedForm;
    private static Set<String> cachedAspects;
    private static int cachedVersion = -1;
    private static MbtiTable.Result cachedResult;
    private static String lastDebugSignature = "";

    /** @return the MBTI to show, or null when nothing is known for this Pokemon. */
    public static MbtiTable.Result resolve(Pokemon pokemon) {
        FormData form = pokemon.getForm();
        Set<String> aspects = pokemon.getAspects();
        if (pokemon == cachedPokemon && form == cachedForm && aspects == cachedAspects
                && cachedVersion == MbtiData.version()) {
            return cachedResult;
        }

        Species species = pokemon.getSpecies();
        List<String> speciesKeys = new ArrayList<>(2);
        addUnique(speciesKeys, MbtiKeys.norm(species.getResourceIdentifier().getPath()));
        addUnique(speciesKeys, MbtiKeys.norm(species.getName()));

        FormData standardForm = species.getStandardForm();
        boolean isStandard = form == null || form == standardForm
                || (standardForm != null && form.getName().equals(standardForm.getName()));
        String formKey = isStandard ? null : MbtiKeys.norm(form.getName());

        Set<String> tokens = new LinkedHashSet<>();
        for (String aspect : aspects) {
            String t = MbtiKeys.stripSuffix(MbtiKeys.norm(aspect));
            if (!t.isEmpty()) tokens.add(t);
        }
        String gender = String.valueOf(pokemon.getGender()).toLowerCase(Locale.ROOT);
        if (gender.equals("male") || gender.equals("female")) tokens.add(gender);

        MbtiTable.Result result = MbtiData.table().resolve(speciesKeys, formKey, tokens);

        if (MbtiConfig.get().debug) {
            String signature = pokemon.getUuid() + "|" + speciesKeys + "|" + formKey + "|" + aspects;
            if (!signature.equals(lastDebugSignature)) {
                lastDebugSignature = signature;
                StringBuilder features = new StringBuilder();
                for (Object feature : pokemon.getFeatures()) features.append(feature).append("; ");
                CobblemonMbtiClient.LOGGER.info(
                        "[debug] species={} (name={}) form='{}' standardForm='{}' isStandard={} gender={} aspects={} features=[{}] -> {}",
                        species.getResourceIdentifier(), species.getName(), form == null ? null : form.getName(),
                        standardForm == null ? null : standardForm.getName(), isStandard, gender, aspects, features,
                        result == null ? "no MBTI" : result.mbti + " (" + result.reason + ")");
            }
        }

        cachedPokemon = pokemon;
        cachedForm = form;
        cachedAspects = aspects;
        cachedVersion = MbtiData.version();
        cachedResult = result;
        return result;
    }

    private static void addUnique(List<String> list, String value) {
        if (!value.isEmpty() && !list.contains(value)) list.add(value);
    }
}
