package dev.cobblemonmbti;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Species -> MBTI data plus the resolution rules. No Minecraft/Cobblemon dependency, so it can be unit-tested. */
public final class MbtiTable {

    public static final class Entry {
        /** MBTI of the species itself; null when the source sheet has none. */
        public String base;
        /** Form key that stands in for the standard form when base is null (e.g. oricorio -> baile). */
        public String standard;
        /** Normalised form key -> MBTI. */
        public final Map<String, String> forms = new LinkedHashMap<>();
    }

    public static final class Result {
        public final String mbti;
        public final String reason;

        public Result(String mbti, String reason) {
            this.mbti = mbti;
            this.reason = reason;
        }
    }

    private static final String[] GENERIC_PREFIXES = {"alola", "galar", "hisui", "paldea"};

    private final Map<String, Entry> species = new HashMap<>();

    public Entry get(String key) {
        return species.get(key);
    }

    public Entry getOrCreate(String key) {
        return species.computeIfAbsent(key, k -> new Entry());
    }

    public Map<String, Entry> all() {
        return Collections.unmodifiableMap(species);
    }

    public int size() {
        return species.size();
    }

    /**
     * Finds which of the entry's form keys matches the given raw key, or null.
     * Exact match first, then generic regional prefix (paldeablaze -> paldea), then mega-x/y/z -> mega.
     */
    public static String matchForm(Entry e, String rawKey) {
        if (e == null || rawKey == null || rawKey.isEmpty()) return null;
        String k = MbtiKeys.canonicalForm(rawKey);
        if (e.forms.containsKey(k)) return k;
        for (String g : GENERIC_PREFIXES) {
            if (k.startsWith(g) && e.forms.containsKey(g)) return g;
        }
        if (k.length() == 5 && k.startsWith("mega") && "xyz".indexOf(k.charAt(4)) >= 0 && e.forms.containsKey("mega")) {
            return "mega";
        }
        return null;
    }

    /**
     * @param speciesKeys candidate species keys in priority order (registry path first, then name)
     * @param formKey     normalised name of the Pokémon's form, or null when it is the standard form
     * @param tokens      extra variant tokens in priority order (aspects such as "pompom", gender "male"/"female")
     * @return the MBTI and why, or null when nothing is known (the UI then hides the badge)
     */
    public Result resolve(List<String> speciesKeys, String formKey, Collection<String> tokens) {
        Entry e = null;
        for (String k : speciesKeys) {
            Entry c = species.get(k);
            if (c != null) {
                e = c;
                break;
            }
        }
        if (e == null) return null;

        if (formKey != null) {
            String m = matchForm(e, formKey);
            if (m != null) return new Result(e.forms.get(m), "form " + m);
        }
        for (String t : tokens) {
            String m = matchForm(e, t);
            if (m != null) return new Result(e.forms.get(m), "token " + m);
        }
        if (e.base != null) {
            return new Result(e.base, formKey != null ? "base (form '" + formKey + "' has no entry of its own)" : "base");
        }
        if (e.standard != null) {
            String v = e.forms.get(e.standard);
            if (v != null) return new Result(v, "standard alias " + e.standard);
        }
        return null;
    }
}
