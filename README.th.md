# Cobblemon MBTI (คู่มือภาษาไทย)

ม็อดฝั่ง client สำหรับ Fabric ที่แสดง **MBTI** ของโปเกมอนชิดขวาของกรอบ **Nature** ในหน้า Summary → แท็บ Info ของ Cobblemon
ใช้ฟอนต์ default ของ Minecraft ถ้า resourcepack เปลี่ยนฟอนต์ ป้ายก็เปลี่ยนตาม

* ใช้กับ Minecraft 1.21.1, Fabric, **Cobblemon 1.7.3**
* ต้องมีแค่ Cobblemon กับสิ่งที่ Cobblemon ต้องใช้อยู่แล้ว (Fabric Loader, Fabric API) ไม่ต้องลงม็อดอื่น และโค้ดไม่ได้ใช้คลาสหรือไฟล์ของม็อดอื่นเลย
* ถ้าผู้เล่นลง All the Mons x Mega Showdown ไว้ สปีชีส์และฟอร์มเพิ่มเติม (Mega, Mega-X/Y ฯลฯ) จะแสดง MBTI ด้วย โดยจับคู่จากชื่อ (id สปีชีส์ ชื่อฟอร์ม aspects เพศ) เท่านั้น ถ้าไม่มีม็อดเหล่านั้น รายการพวกนี้ก็ไม่ถูกใช้ ไม่ทำให้เกิดข้อผิดพลาด
* ติดตั้งฝั่งผู้เล่นอย่างเดียว เซิร์ฟเวอร์ไม่ต้องลง

## สีพื้นหลัง
| กลุ่ม | ตัวอักษร | สี |
|---|---|---|
| Analysts | N + T | ม่วง `#88619A` |
| Diplomats | N + F | เขียว `#33A474` |
| Sentinels | S + J | ฟ้า `#4298B4` |
| Explorers | S + P | เหลือง `#E4AE3A` |
| บอกกลุ่มไม่ได้ หรือ N/A | | เทา `#7A7A7A` |

ชนิดที่มี X จะได้สีกลุ่มเมื่อตัวอักษรที่รู้แล้วครบคู่ของกลุ่มใดกลุ่มหนึ่ง เช่น `XNTX` ม่วง, `ISXJ` ฟ้า, `XSTP` เหลือง ถ้ายังเป็นไปได้หลายกลุ่ม (เช่น `XSTX` เป็นได้ทั้ง ISTJ และ ISTP) จะเป็นสีเทา ส่วน `N/A` จะซ่อนป้าย เว้นแต่เปิด `showUnknownAsNa`

## ตำแหน่งป้าย
สี่เหลี่ยมพื้นหลังวาดด้วยพิกเซลเต็ม สูงเท่ากรอบ Nature และชิดขอบขวาของกรอบพอดี ย่อเฉพาะตัวหนังสือ (`badgeScale`) ค่าตำแหน่งเริ่มต้นเทียบกับมุมบนซ้ายของ widget: ขอบบน `y+65`, สูง `11`, ขอบขวา `x+129` ค่านี้วัดจากภาพหน้าจอ เพราะกรอบ Nature เป็นส่วนของภาพ `summary_info_base.png` ไม่ได้ถูกวาดด้วยโค้ด

resourcepack ที่วาดภาพนั้นใหม่สามารถแนบไฟล์ `assets/cobblemon_mbti/mbti/layout.json` เพื่อย้ายป้ายได้ (ดูตัวอย่างใน README.md)

## ติดตั้งเครื่องมือและ Build
1. JDK 21, IntelliJ IDEA (หรือ VS Code), Git และ Gradle 8.10.2 (หรือรัน `gradle wrapper --gradle-version 8.10.2` หนึ่งครั้ง)
2. วางไฟล์ `Cobblemon-fabric-1.7.3+1.21.1.jar` (รุ่น Fabric) ไว้ในโฟลเดอร์ `libs/` (แนะนำ เพราะพิกัด Maven ใน `gradle.properties` ผมไม่ได้ยืนยัน)
3. `gradle build` ได้ไฟล์ `build/libs/cobblemon-mbti-1.0.0.jar` นำไปใส่โฟลเดอร์ `mods` ของเกม หรือใช้ `gradle runClient` เพื่อทดสอบ

## ขึ้น GitHub
1. สร้าง repo แล้ว push ขึ้นไป GitHub Actions (`.github/workflows/build.yml`) จะทดสอบตรรกะและ build jar ให้ (ดาวน์โหลดจากหน้า Actions → Artifacts)
2. ติด tag เช่น `git tag v1.0.0 && git push --tags` จะสร้าง Release พร้อมไฟล์ jar
3. อย่านำ jar ของ Cobblemon ขึ้น repo สาธารณะ

## ทดสอบหลัง build
1. **ตำแหน่งป้าย:** เปิด Summary แท็บ Info ที่ GUI scale 1–4 และเต็มจอ ป้ายต้องสูงเท่ากรอบ Nature และชิดขอบขวา ถ้ายังเหลื่อม ส่งภาพมาให้วัด หรือปรับ `offsetX`, `offsetY`, `boxRightInset` ใน config
2. **สี:** Iron Moth (ม่วง), Dwebble (ฟ้า), Zangoose กับ Mega Zeraora (เหลือง), Paldean Tauros และ Staryu (เทา)
3. **ชุดที่มีแค่ Cobblemon:** สร้างโปรไฟล์ใหม่ที่มีแค่ Fabric Loader, Fabric API, Fabric Language Kotlin, Cobblemon และม็อดนี้ เกมต้องเปิดได้ ล็อกไม่มี error เกี่ยวกับม็อดอื่น ป้ายขึ้นกับสปีชีส์ปกติ ร่าง Alola/Galar/Hisui และ Meowstic แล้วรัน `/mbti dump` เก็บรายงานไว้เทียบกับชุดที่มี ATM x MSD
4. **ชุดที่มี ATM x MSD:** ทดสอบฟอร์มที่สุ่ม (Oricorio, Toxtricity, Urshifu, Deerling, Sawsbuck) และ Mega ซ้ำ

## แก้ข้อมูล MBTI
แก้ `data/pokemon_1025_mbti.xlsx` แล้วรัน `pip install pandas openpyxl` และ `python tools/excel_to_json.py` จากนั้น build ใหม่ ตัวพิเศษที่มีเฉพาะในอนิเมะจะถูกข้าม เพราะไม่มีในเกม

## config (`config/cobblemon_mbti.json`)
`enabled`, `showUnknownAsNa`, `badgeScale`, `textPaddingX`, `boxRightInset`, `offsetX`, `offsetY`, `textShadow`, `debug` และสี 6 ค่า คำสั่ง `/mbti reload` กับ `/mbti dump` ไฟล์ config เก่าจะถูกย้ายรุ่นอัตโนมัติ (ค่า offset รีเซ็ตเป็น 0 หนึ่งครั้ง และคีย์เก่า `rightPadding` หายไป)

## สถานะ
รุ่น 0.1.1 ผู้พัฒนาทดสอบในเกมแล้ว (Cobblemon 1.7.3 + Mega Showdown + ATM x MSD) **รุ่น 1.0.0 (ตำแหน่งป้ายใหม่ กฎสีใหม่ และการรันกับ Cobblemon ตัวเดียว) ยังไม่ได้ทดสอบในเกม** ตรรกะฝั่ง Java ที่ไม่พึ่ง Minecraft มีชุดทดสอบ 84 รายการ
