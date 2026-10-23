import dev.cobblemonmbti.MbtiColors;
import dev.cobblemonmbti.MbtiKeys;
import dev.cobblemonmbti.MbtiLayout;
import dev.cobblemonmbti.MbtiTable;

import java.util.List;

/**
 * Run without Minecraft:
 *   javac -d /tmp/mbti-test src/main/java/dev/cobblemonmbti/MbtiKeys.java src/main/java/dev/cobblemonmbti/MbtiTable.java \
 *         src/main/java/dev/cobblemonmbti/MbtiColors.java src/main/java/dev/cobblemonmbti/MbtiLayout.java tools/ResolverSelfTest.java
 *   java -cp /tmp/mbti-test ResolverSelfTest
 */
public class ResolverSelfTest {
    static int failures = 0;
    static final MbtiColors.Group A = MbtiColors.Group.ANALYST, D = MbtiColors.Group.DIPLOMAT,
            S = MbtiColors.Group.SENTINEL, E = MbtiColors.Group.EXPLORER;

    static void check(String name, Object expected, Object actual) {
        boolean ok = expected == null ? actual == null : expected.equals(actual);
        if (!ok) failures++;
        System.out.println((ok ? "ok    " : "FAIL  ") + name + (ok ? "" : "  expected=" + expected + " actual=" + actual));
    }

    static String mbti(MbtiTable t, String species, String formKey, String... tokens) {
        MbtiTable.Result r = t.resolve(List.of(species), formKey, List.of(tokens));
        return r == null ? null : r.mbti;
    }

    static String why(MbtiTable t, String species, String formKey, String... tokens) {
        MbtiTable.Result r = t.resolve(List.of(species), formKey, List.of(tokens));
        return r == null ? null : r.reason;
    }

    public static void main(String[] args) {
        MbtiTable t = new MbtiTable();
        MbtiTable.Entry charizard = t.getOrCreate("charizard");
        charizard.base = "ESTJ"; charizard.forms.put("megax", "ENTJ"); charizard.forms.put("megay", "ESTP");
        MbtiTable.Entry vulpix = t.getOrCreate("vulpix");
        vulpix.base = "INFP"; vulpix.forms.put("alola", "INFP");
        MbtiTable.Entry ninetales = t.getOrCreate("ninetales");
        ninetales.base = "INTJ"; ninetales.forms.put("alola", "INFJ");
        MbtiTable.Entry tauros = t.getOrCreate("tauros");
        tauros.base = "ESTJ"; tauros.forms.put("paldeablaze", "ESTJ"); tauros.forms.put("paldeaaqua", "ISFP");
        MbtiTable.Entry rattata = t.getOrCreate("rattata");
        rattata.base = "ISTP"; rattata.forms.put("paldea", "ESFP");
        MbtiTable.Entry ori = t.getOrCreate("oricorio");
        ori.standard = "baile";
        ori.forms.put("baile", "ESFJ"); ori.forms.put("pompom", "ENFP"); ori.forms.put("pau", "INFJ"); ori.forms.put("sensu", "INTJ");
        MbtiTable.Entry urs = t.getOrCreate("urshifu");
        urs.forms.put("singlestrike", "ESTP"); urs.forms.put("rapidstrike", "ENFJ");
        MbtiTable.Entry meow = t.getOrCreate("meowstic");
        meow.standard = "male";
        meow.forms.put("male", "INFP"); meow.forms.put("female", "INTJ"); meow.forms.put("mmega", "INFJ"); meow.forms.put("fmega", "INFJ");
        MbtiTable.Entry staryu = t.getOrCreate("staryu");
        staryu.base = "INXJ";
        MbtiTable.Entry absol = t.getOrCreate("absol");
        absol.base = "INFJ"; absol.forms.put("mega", "INFJ"); absol.forms.put("megaz", "INFJ");
        MbtiTable.Entry raichu = t.getOrCreate("raichu");
        raichu.base = "ESFP"; raichu.forms.put("mega", "ENFP");

        check("base only", "INXJ", mbti(t, "staryu", null));
        check("mega-x by form name", "ENTJ", mbti(t, "charizard", MbtiKeys.norm("Mega-X")));
        check("mega-y by form name", "ESTP", mbti(t, "charizard", MbtiKeys.norm("Mega-Y")));
        check("unknown form (Gmax) falls back to base", "ESTJ", mbti(t, "charizard", MbtiKeys.norm("Gmax")));
        check("gmax reason mentions fallback", true, why(t, "charizard", "gmax").startsWith("base (form"));
        check("alola form", "INFP", mbti(t, "vulpix", MbtiKeys.norm("Alola")));
        check("alolan spelling aliased", "INFJ", mbti(t, "ninetales", MbtiKeys.norm("Alolan")));
        check("paldea breed exact", "ISFP", mbti(t, "tauros", MbtiKeys.norm("Paldea-Aqua")));
        check("paldea breed w/o entry -> base", "ESTJ", mbti(t, "tauros", MbtiKeys.norm("Paldea-Combat")));
        check("generic paldea prefix", "ESFP", mbti(t, "rattata", MbtiKeys.norm("Paldea-Zoom")));
        check("oricorio standard -> baile alias", "ESFJ", mbti(t, "oricorio", null));
        check("oricorio standard reason", "standard alias baile", why(t, "oricorio", null));
        check("oricorio pom-pom by form", "ENFP", mbti(t, "oricorio", MbtiKeys.norm("Pom-Pom")));
        check("oricorio pa'u by form", "INFJ", mbti(t, "oricorio", MbtiKeys.norm("Pa\u2019u")));
        check("oricorio sensu by aspect", "INTJ", mbti(t, "oricorio", null, MbtiKeys.stripSuffix(MbtiKeys.norm("sensu-style"))));
        check("urshifu single by aspect", "ESTP", mbti(t, "urshifu", null, MbtiKeys.stripSuffix(MbtiKeys.norm("single_strike-style"))));
        check("urshifu rapid by aspect", "ENFJ", mbti(t, "urshifu", null, MbtiKeys.stripSuffix(MbtiKeys.norm("rapid_strike-style"))));
        check("urshifu with no info -> hidden", null, mbti(t, "urshifu", null));
        check("meowstic female by gender token", "INTJ", mbti(t, "meowstic", null, "female"));
        check("meowstic male by gender token", "INFP", mbti(t, "meowstic", null, "male"));
        check("meowstic no gender -> standard alias male", "INFP", mbti(t, "meowstic", null));
        check("meowstic m-mega", "INFJ", mbti(t, "meowstic", MbtiKeys.norm("M-Mega")));
        check("meowstic f-mega", "INFJ", mbti(t, "meowstic", MbtiKeys.norm("F-Mega")));
        check("absol mega-z exact", "INFJ", mbti(t, "absol", "megaz"));
        check("raichu mega-x falls to 'mega'", "ENFP", mbti(t, "raichu", "megax"));
        check("unknown species -> null", null, mbti(t, "doesnotexist", null));
        check("second species key is tried", "INXJ", t.resolve(List.of("nope", "staryu"), null, List.of()).mbti);

        check("norm Flabebe", "flabebe", MbtiKeys.norm("Flab\u00e9b\u00e9"));
        check("norm Mr. Mime", "mrmime", MbtiKeys.norm("Mr. Mime"));
        check("norm Farfetch'd", "farfetchd", MbtiKeys.norm("Farfetch'd"));
        check("norm Type: Null", "typenull", MbtiKeys.norm("Type: Null"));
        check("norm Jangmo-o", "jangmoo", MbtiKeys.norm("Jangmo-o"));
        check("norm Wo-Chien", "wochien", MbtiKeys.norm("Wo-Chien"));
        check("stripSuffix style", "pompom", MbtiKeys.stripSuffix("pompomstyle"));
        check("stripSuffix form", "sky", MbtiKeys.stripSuffix("skyform"));
        check("stripSuffix keeps bare 'form'", "form", MbtiKeys.stripSuffix("form"));

        // ---- complete types follow the 16personalities table
        check("INTJ analyst", MbtiColors.Group.ANALYST, MbtiColors.groupOf("INTJ"));
        check("ENTP analyst", MbtiColors.Group.ANALYST, MbtiColors.groupOf("ENTP"));
        check("ENFP diplomat", MbtiColors.Group.DIPLOMAT, MbtiColors.groupOf("ENFP"));
        check("INFJ diplomat", MbtiColors.Group.DIPLOMAT, MbtiColors.groupOf("INFJ"));
        check("ISTJ sentinel", MbtiColors.Group.SENTINEL, MbtiColors.groupOf("ISTJ"));
        check("ESFJ sentinel", MbtiColors.Group.SENTINEL, MbtiColors.groupOf("ESFJ"));
        check("ESTP explorer", MbtiColors.Group.EXPLORER, MbtiColors.groupOf("ESTP"));
        check("ISFP explorer", MbtiColors.Group.EXPLORER, MbtiColors.groupOf("ISFP"));
        check("lowercase accepted", MbtiColors.Group.ANALYST, MbtiColors.groupOf("intj"));

        // ---- types with X: the values that really occur in the sheet
        check("XNTX purple (Iron Moth)", MbtiColors.Group.ANALYST, MbtiColors.groupOf("XNTX"));
        check("ISXJ blue (Dwebble)", MbtiColors.Group.SENTINEL, MbtiColors.groupOf("ISXJ"));
        check("XSTP yellow (Zangoose, Mega Zeraora)", MbtiColors.Group.EXPLORER, MbtiColors.groupOf("XSTP"));
        check("XSTX grey (Paldean Tauros: ISTJ or ISTP)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("XSTX"));
        check("INXJ grey (Staryu)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("INXJ"));
        check("IXXX grey (Tangrowth)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("IXXX"));
        check("EXFP grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("EXFP"));
        check("EXXX grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("EXXX"));
        check("XXFJ grey (NF or SJ)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("XXFJ"));
        check("IXXP grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("IXXP"));
        check("XXTP grey (NT or SP)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("XXTP"));
        check("EXTJ grey (NT or SJ)", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("EXTJ"));
        check("IXTX grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("IXTX"));
        check("XXXX grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("XXXX"));
        check("N/A grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("N/A"));
        check("null grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf(null));
        check("junk letters grey", MbtiColors.Group.UNKNOWN, MbtiColors.groupOf("ABCD"));
        check("rgb #88619A", 0x88619A, MbtiColors.parseRgb("#88619A", 0));
        check("rgb bad -> fallback", 7, MbtiColors.parseRgb("zzz", 7));

        // ---- exhaustive: colour only when EVERY completion of the X letters lands in the same role group
        String[] official = {"INTJ","INTP","ENTJ","ENTP","INFJ","INFP","ENFJ","ENFP","ISTJ","ISFJ","ESTJ","ESFJ","ISTP","ISFP","ESTP","ESFP"};
        MbtiColors.Group[] officialGroup = {A,A,A,A,D,D,D,D,S,S,S,S,E,E,E,E};
        java.util.Map<String, MbtiColors.Group> table = new java.util.HashMap<>();
        for (int i = 0; i < official.length; i++) table.put(official[i], officialGroup[i]);
        int mismatches = 0, patterns = 0;
        String[][] letters = {{"E","I"},{"N","S"},{"T","F"},{"J","P"}};
        for (String a : new String[]{"E","I","X"}) for (String b : new String[]{"N","S","X"})
            for (String c : new String[]{"T","F","X"}) for (String d : new String[]{"J","P","X"}) {
                String pattern = a + b + c + d;
                String[] x = {a, b, c, d};
                java.util.Set<MbtiColors.Group> groups = new java.util.HashSet<>();
                for (int i0 = 0; i0 < (x[0].equals("X") ? 2 : 1); i0++)
                  for (int i1 = 0; i1 < (x[1].equals("X") ? 2 : 1); i1++)
                    for (int i2 = 0; i2 < (x[2].equals("X") ? 2 : 1); i2++)
                      for (int i3 = 0; i3 < (x[3].equals("X") ? 2 : 1); i3++) {
                        String full = (x[0].equals("X") ? letters[0][i0] : x[0]) + (x[1].equals("X") ? letters[1][i1] : x[1])
                                + (x[2].equals("X") ? letters[2][i2] : x[2]) + (x[3].equals("X") ? letters[3][i3] : x[3]);
                        groups.add(table.get(full));
                      }
                MbtiColors.Group expected = groups.size() == 1 ? groups.iterator().next() : MbtiColors.Group.UNKNOWN;
                patterns++;
                if (expected != MbtiColors.groupOf(pattern)) { mismatches++; System.out.println("   mismatch " + pattern); }
            }
        check("all " + patterns + " E/I/X x N/S/X x T/F/X x J/P/X patterns agree with 'every completion in one group'", 0, mismatches);

        // ---- badge geometry (widget at 0,0; text "INTJ" is 22 px wide in Minecraft's font)
        MbtiLayout.Geometry g = MbtiLayout.Geometry.DEFAULT;
        check("default box top", 65, g.top);
        check("default box height", 11, g.height);
        check("default box right", 129, g.right);
        MbtiLayout.Placement p = MbtiLayout.place(g, 0, 0, 22, 0.75f, 3, 0, 0, 0);
        check("badge right edge flush with the Nature box", 129, p.right);
        check("badge top equals box top", 65, p.top);
        check("badge bottom equals box bottom (same height)", 76, p.bottom);
        check("badge left", 106, p.left);
        check("text x", 109, p.textX);
        check("text y", 68, p.textY);
        MbtiLayout.Placement wide = MbtiLayout.place(g, 0, 0, 40, 0.75f, 3, 0, 0, 0);
        check("longer text keeps the right edge", 129, wide.right);
        check("longer text widens leftwards", true, wide.left < p.left);
        MbtiLayout.Placement moved = MbtiLayout.place(g, 77, 12, 22, 0.75f, 3, 0, 0, 0);
        check("widget position is added (right)", 77 + 129, moved.right);
        check("widget position is added (top)", 12 + 65, moved.top);
        MbtiLayout.Placement shifted = MbtiLayout.place(g, 0, 0, 22, 0.75f, 3, 1, 2, -1);
        check("inset and offsets apply (right)", 129 - 1 + 2, shifted.right);
        check("offsets apply (top)", 64, shifted.top);
        MbtiLayout.Placement full = MbtiLayout.place(g, 0, 0, 22, 1.0f, 3, 0, 0, 0);
        check("scale 1.0 text y = top + 2", 67, full.textY);
        check("scale 1.0 badge width = 22 + 6", 28, full.right - full.left);
        check("custom layout is honoured", 150, MbtiLayout.place(new MbtiLayout.Geometry(70, 9, 150), 0, 0, 22, 0.75f, 3, 0, 0, 0).right);
        check("geometry height is never below 1", 1, new MbtiLayout.Geometry(0, 0, 0).height);

        System.out.println(failures == 0 ? "\nALL PASSED" : "\n" + failures + " FAILED");
        if (failures > 0) System.exit(1);
    }
}
