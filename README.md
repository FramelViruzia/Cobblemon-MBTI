# Cobblemon MBTI

Client-side Fabric mod that shows each Pokémon's **MBTI type** at the right end of the **Nature** box in Cobblemon's
Summary → Info tab, in the four 16personalities role colours. Text is drawn with Minecraft's default font, so a font
resource pack changes it too.

* Target: Minecraft 1.21.1, Fabric, **Cobblemon 1.7.3**
* Needs only Cobblemon and what Cobblemon itself needs (Fabric Loader, Fabric API). Nothing else must be installed,
  and the source uses no classes or files from any other mod.
* Optional: if the *All the Mons x Mega Showdown* setup is installed, its extra species and forms (Mega, Mega-X/Y ...)
  get their own MBTI as well. This works through names only (species id, form name, aspects, gender); nothing of those
  mods is referenced in code, so without them those entries are simply never used.
* Client only – nothing to install on the server.

Thai instructions: [README.th.md](README.th.md)

## Colours
| Group | Letters | Colour |
|---|---|---|
| Analysts | N + T | purple `#88619A` |
| Diplomats | N + F | green `#33A474` |
| Sentinels | S + J | blue `#4298B4` |
| Explorers | S + P | yellow `#E4AE3A` |
| role cannot be told, or N/A | | grey `#7A7A7A` |

A type with `X` (partly unknown) gets a role colour when the letters that are known already contain one of the pairs
above: `XNTX` purple, `ISXJ` blue, `XSTP` yellow. If several roles are still possible (`XSTX` could be ISTJ or ISTP,
`XXFJ` could be NF or SJ) it is grey. `N/A` hides the badge unless `showUnknownAsNa` is on.

## How it works
* `InfoWidgetMixin` injects at the end of `InfoWidget.renderWidget` (that widget only exists while the Info tab is open)
  and calls `MbtiBadgeRenderer`.
* The badge rectangle is drawn with whole GUI pixels, the same height as the Nature value box and flush with its right
  edge. Only the text is scaled (`badgeScale`). The box position comes from `MbtiLayout`
  (default: top `y+65`, height `11`, right edge `x+129`, relative to the widget).
* `MbtiLookup` reads species, form, aspects and gender from the `Pokemon`; `MbtiTable` resolves the MBTI
  (form → aspect/gender tokens → species base → "standard form" alias).
* Data: `assets/cobblemon_mbti/mbti/species.json`, generated from `data/pokemon_1025_mbti.xlsx` by `tools/excel_to_json.py`.

## Resource packs
A pack can ship `assets/cobblemon_mbti/mbti/layout.json` to move the badge if it redraws Cobblemon's
`summary_info_base.png`:
```json
{ "nature_box_top": 65, "nature_box_height": 11, "nature_box_right": 129 }
```
(GUI pixels relative to the top-left corner of the Info widget; `right` is exclusive.) A pack can also ship
`assets/cobblemon_mbti/mbti/species.json` to add or override single MBTI entries.

## Config (`config/cobblemon_mbti.json`)
`enabled`, `showUnknownAsNa`, `badgeScale`, `textPaddingX`, `boxRightInset`, `offsetX`, `offsetY`, `textShadow`, `debug`
and the six colours. Commands: `/mbti reload`, `/mbti dump` (writes `config/cobblemon_mbti_report.txt`).
Old config files are migrated: offsets are reset to 0 once and the obsolete `rightPadding` key disappears.

## Build
1. JDK 21 and Gradle 8.10.2 (or run `gradle wrapper --gradle-version 8.10.2` once to create the wrapper).
2. Put the Fabric build of Cobblemon 1.7.3 into `libs/` (recommended), or rely on the Maven coordinates in `gradle.properties`.
3. `gradle build` → `build/libs/cobblemon-mbti-1.0.0.jar`; `gradle runClient` to test.

## Pure-Java tests (no Minecraft needed)
```
javac -d /tmp/t src/main/java/dev/cobblemonmbti/MbtiKeys.java src/main/java/dev/cobblemonmbti/MbtiTable.java \
      src/main/java/dev/cobblemonmbti/MbtiColors.java src/main/java/dev/cobblemonmbti/MbtiLayout.java tools/ResolverSelfTest.java
java -cp /tmp/t ResolverSelfTest
```

## Status
0.1.1 was built and checked in game by the author (Cobblemon 1.7.3 + Mega Showdown + ATM x MSD): badge, colours, random
forms and all MBTI values. **1.0.0 (new badge geometry, new colour rule) and running with Cobblemon alone have not yet
been tested in game.**

## Data
MBTI votes come from Personality Database (user-submitted). Check their terms before redistributing the data.
Pokémon is a trademark of Nintendo / Creatures Inc. / GAME FREAK inc.; this is an unofficial fan project.
