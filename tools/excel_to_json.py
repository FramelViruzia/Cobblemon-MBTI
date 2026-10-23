#!/usr/bin/env python3
"""Convert the MBTI Excel sheet into the JSON the mod reads.

Usage:  python tools/excel_to_json.py [input.xlsx] [output.json]
Needs:  pip install pandas openpyxl

Rules
  * One entry per species, keyed by its Cobblemon/Showdown-style id (lowercase, letters and digits only).
  * "base"     = MBTI of the species (omitted when the sheet says N/A).
  * "forms"    = MBTI of in-game forms (Mega, regional, Oricorio styles ...), keyed by a normalised form key.
  * "standard" = which form key stands in for the standard form when "base" is missing (e.g. Oricorio -> baile).
  * Anime-only entries (Ash's Pikachu, James' Weezing ...) cannot exist in game and are skipped.
  * Values containing X (XXXX, EXFP ...) are kept as-is; the mod draws them in grey.
Variants that cannot be turned into a form key are skipped and counted, so you can add a rule below.
"""
import json, re, sys, unicodedata
import pandas as pd

SRC = sys.argv[1] if len(sys.argv) > 1 else 'data/pokemon_1025_mbti.xlsx'
OUT = sys.argv[2] if len(sys.argv) > 2 else 'src/main/resources/assets/cobblemon_mbti/mbti/species.json'
VALID = re.compile(r'^[EIX][NSX][TFX][JPX]$')


def norm(s):
    s = unicodedata.normalize('NFKD', s)
    s = ''.join(c for c in s if not unicodedata.combining(c))
    return re.sub(r'[^a-z0-9]', '', s.lower())


# Sheet names that differ from the game's registry id
KEY_ALIASES = {'ironboulders': 'ironboulder'}   # the Pokémon is "Iron Boulder"


def species_key(name):
    if name.startswith('Nidoran'):
        return 'nidoranf' if '\u2640' in name else 'nidoranm'
    name = re.sub(r'\s*\(.*?\)', '', name)       # "Rotom (All form)" -> "Rotom"
    key = norm(name)
    return KEY_ALIASES.get(key, key)


# A possessive is a name followed by 's, s' or ' and then a space (Ash's Pikachu, James' Weezing).
# Farfetch'd and P'au are not possessives.
POSSESSIVE = re.compile(r"\b[A-Z][a-z]+(?:['\u2019]s|s['\u2019]|['\u2019]) ")
ONE_OFFS = re.compile(r'\[|Strikes Back|MS016|"|Captain |Boss |Singing|Evil |Elder |Dada |Eternal |'
                      r'Bloodmoon|Shuckle II|Rotom Dex|Phone|Nebby')


def form_keys(sp, label):
    """Return the list of form keys this label stands for, or None if it is not an in-game form."""
    L = label
    # forms that look like anime one-offs but really exist in Cobblemon (checked with /mbti dump)
    if sp == 'Greninja' and L.startswith('Ash'):
        return ['ash']
    if sp == 'Ursaluna' and 'Bloodmoon' in L:
        return ['bloodmoon']
    if sp == 'Floette' and 'Eternal' in L:
        return ['eternal']
    if POSSESSIVE.search(L) or ONE_OFFS.search(L):
        return None
    m = re.match(r'^Mega (.+?)(?: ([XYZ]))?$', L)
    if m:
        if sp == 'Meowstic':
            return ['mmega', 'fmega']
        return ['mega' + (m.group(2).lower() if m.group(2) else '')]
    if L.startswith('Primal '):
        return ['primal']
    if L.startswith('Ultra '):
        return ['ultra']
    low = L.lower()
    for word, key in (('alolan', 'alola'), ('galarian', 'galar'), ('hisuian', 'hisui'), ('paldean', 'paldea')):
        if low.startswith(word + ' '):
            tail = ''
            if sp == 'Tauros':
                b = re.search(r'\((Blaze|Aqua|Combat)\)', L)
                tail = b.group(1).lower() if b else ''
            if 'zen mode' in low:
                tail = 'zen'
            return [key + tail]
    if sp == 'Darmanitan' and 'zen mode' in low:
        return ['zen']
    if sp == 'Kyurem':
        return [norm(L.split('-')[-1])]  # white / black
    if sp == 'Meloetta':
        return ['pirouette']
    if sp == 'Zygarde':
        return ['10'] if '10%' in L else None  # "50 %" is the standard form
    if sp == 'Necrozma':
        m = re.search(r'\((Dawn Wings|Dusk Mane)\)', L)
        return [norm(m.group(1))] if m else None
    if sp == 'Rotom':
        return ['mow'] if 'Mow' in L else None
    if sp == 'Shaymin':
        return ['sky'] if 'Sky' in L else None
    if sp in ('Deerling', 'Sawsbuck'):
        m = re.search(r'\b(Spring|Summer|Autumn|Winter)\b', L)
        return [m.group(1).lower()] if m else None
    if sp == 'Meowstic':
        m = re.search(r'\b(Male|Female)\b', L)
        return [m.group(1).lower()] if m else None
    if sp == 'Oricorio':
        for k, w in (('pau', "P'au"), ('baile', 'Baile'), ('pompom', 'Pom-Pom'), ('sensu', 'Sensu')):
            if w in L:
                return [k]
        return None
    if sp == 'Lycanroc':
        m = re.search(r'\b(Midnight|Midday|Dusk)\b', L)
        return [m.group(1).lower()] if m else None
    if sp == 'Toxtricity':
        return ['amped'] if L.startswith('Amped') else (['lowkey'] if L.startswith('Low Key') else None)
    if sp == 'Urshifu':
        return ['rapidstrike'] if 'Rapid Strike' in L else (['singlestrike'] if 'Single Strike' in L else None)
    return None


# species whose standard form has no dedicated sheet row: which form key stands in for it
STANDARD = {'oricorio': 'baile', 'lycanroc': 'midday', 'toxtricity': 'amped',
            'deerling': 'spring', 'sawsbuck': 'spring', 'meowstic': 'male', 'indeedee': 'male'}

df = pd.read_excel(SRC, sheet_name='Pok\u00e9mon MBTI', keep_default_na=False)
data, skipped, skipped_labels = {}, 0, []
for _, r in df.iterrows():
    sp = r['Pok\u00e9mon']
    key = species_key(sp)
    entry = {}
    base = str(r['MBTI']).strip().upper()
    if base != 'N/A':
        if not VALID.match(base):
            print('!! invalid base MBTI', sp, base)
            continue
        entry['base'] = base
    forms = {}
    for k in range(1, 6):
        label = str(r.get(f'Variant {k}', '')).strip()
        val = str(r.get(f'Variant {k} MBTI', '')).strip().upper()
        if not label:
            continue
        if sp == 'Indeedee' and re.search(r'\((Male|Female)\)', label):
            keys = [re.search(r'\((Male|Female)\)', label).group(1).lower()]
        else:
            keys = form_keys(sp, label)
        if keys is None:
            skipped += 1
            skipped_labels.append((sp, label))
            continue
        if val == 'N/A':
            continue
        if not VALID.match(val):
            print('!! invalid variant MBTI', sp, label, val)
            continue
        for fk in keys:
            forms[fk] = val
    if forms:
        entry['forms'] = forms
    if 'base' not in entry and key in STANDARD and STANDARD[key] in forms:
        entry['standard'] = STANDARD[key]
    if entry:
        data[key] = entry

out = {'_comment': 'Generated by tools/excel_to_json.py from pokemon_1025_mbti.xlsx. '
                   'Keys: species id -> base / forms / standard.',
       'species': data}
with open(OUT, 'w', encoding='utf-8') as f:
    json.dump(out, f, ensure_ascii=False, indent=1)
nf = sum(len(e.get('forms', {})) for e in data.values())
print(f'species entries: {len(data)}  with base: {sum("base" in e for e in data.values())}  '
      f'form entries: {nf}  skipped(anime/other): {skipped}')
print('skipped variants (not in-game forms):', skipped_labels)
print('standard aliases:', {k: v['standard'] for k, v in data.items() if 'standard' in v})
print('rows without any entry:', [r['Pok\u00e9mon'] for _, r in df.iterrows() if species_key(r['Pok\u00e9mon']) not in data])
