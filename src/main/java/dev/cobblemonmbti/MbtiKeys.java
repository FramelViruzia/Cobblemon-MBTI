package dev.cobblemonmbti;

import java.text.Normalizer;

/** Key helpers shared by the data loader, the lookup and the report. No Minecraft/Cobblemon dependency. */
public final class MbtiKeys {
    private MbtiKeys() {}

    /** Lowercase, strip accents, keep only a-z and 0-9 ("Mr. Mime" -> "mrmime", "Flabébé" -> "flabebe"). */
    public static String norm(String s) {
        if (s == null) return "";
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder(n.length());
        for (int i = 0; i < n.length(); i++) {
            char c = Character.toLowerCase(n.charAt(i));
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) sb.append(c);
        }
        return sb.toString();
    }

    /** "pompomstyle" -> "pompom", "singlestrikestyle" -> "singlestrike". Repeats until nothing is left to strip. */
    public static String stripSuffix(String token) {
        String t = token;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String suffix : new String[] {"style", "forme", "form", "mode"}) {
                if (t.length() > suffix.length() && t.endsWith(suffix)) {
                    t = t.substring(0, t.length() - suffix.length());
                    changed = true;
                }
            }
        }
        return t;
    }

    /** Different packs spell regional forms differently: "alolan" -> "alola", "galarian" -> "galar" ... */
    public static String canonicalForm(String key) {
        String[][] map = {{"alolan", "alola"}, {"galarian", "galar"}, {"hisuian", "hisui"}, {"paldean", "paldea"}};
        for (String[] m : map) {
            if (key.startsWith(m[0])) return m[1] + key.substring(m[0].length());
        }
        return key;
    }
}
