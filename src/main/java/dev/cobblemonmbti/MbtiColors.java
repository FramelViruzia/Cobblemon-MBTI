package dev.cobblemonmbti;

/** Maps an MBTI string to one of the four 16personalities role groups (or grey when the role cannot be told). */
public final class MbtiColors {
    private MbtiColors() {}

    public enum Group { ANALYST, DIPLOMAT, SENTINEL, EXPLORER, UNKNOWN }

    /**
     * 16personalities roles: Analysts = N+T (purple), Diplomats = N+F (green), Sentinels = S+J (blue),
     * Explorers = S+P (yellow).
     *
     * A role colour is used whenever the letters that are known already contain one of those pairs, so
     * XNTX (N+T) is purple, ISXJ (S+J) is blue and XSTP (S+P) is yellow. When more than one role is still
     * possible the result is grey: XSTX could be ISTJ (blue) or ISTP (yellow), INXJ needs T/F, XXFJ could be
     * NF or SJ. This gives the same answer as "colour only if every way of filling in the X letters lands in
     * the same role group" for all 81 patterns (checked in tools/ResolverSelfTest.java).
     *
     * "N/A", wrong length or letters outside E/I/X, N/S/X, T/F/X, J/P/X are grey.
     */
    public static Group groupOf(String mbti) {
        if (mbti == null || mbti.length() != 4) return Group.UNKNOWN;
        String m = mbti.toUpperCase();
        char ei = m.charAt(0), ns = m.charAt(1), tf = m.charAt(2), jp = m.charAt(3);
        if ("EIX".indexOf(ei) < 0 || "NSX".indexOf(ns) < 0 || "TFX".indexOf(tf) < 0 || "JPX".indexOf(jp) < 0) {
            return Group.UNKNOWN;
        }
        if (ns == 'N' && tf == 'T') return Group.ANALYST;
        if (ns == 'N' && tf == 'F') return Group.DIPLOMAT;
        if (ns == 'S' && jp == 'J') return Group.SENTINEL;
        if (ns == 'S' && jp == 'P') return Group.EXPLORER;
        return Group.UNKNOWN;
    }

    /** Parses "#RRGGBB", "RRGGBB" or "0xRRGGBB"; returns the fallback when the text is not a valid colour. */
    public static int parseRgb(String text, int fallback) {
        if (text == null) return fallback;
        String t = text.trim();
        if (t.startsWith("#")) t = t.substring(1);
        else if (t.startsWith("0x") || t.startsWith("0X")) t = t.substring(2);
        if (t.length() != 6) return fallback;
        try {
            return Integer.parseInt(t, 16);
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}
