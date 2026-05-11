# Toyota Corolla E210 OBD2 Diagnosztikai Alkalmazás

<div align="center">

![Toyota Corolla E210](https://img.shields.io/badge/J%C3%A1rm%C5%B1-Toyota%20Corolla%20E210-ff0000?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-15%2B-34A853?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-7F52FF?style=for-the-badge&logo=kotlin)
![C%C3%A9l%20SDK](https://img.shields.io/badge/C%C3%A9l%20SDK-35-4285F4?style=for-the-badge)

**⚠️ Figyelmeztetés: Ez a projekt oktatási és fejlesztési célokat szolgál. A jármű beállításainak módosítása saját felelősségre történik.**

</div>

---

## 📋 Projekt Áttekintés

Modern Android OBD2 diagnosztikai alkalmazás a Toyota Corolla E210 (2019) modellhez, amely lehetővé teszi:
- ✅ Összes szenzor valós idejű olvasása
- ✅ Hibakódok (DTC) olvasása és törlése  
- ✅ Rejtett beállítások módosítása (Customization)
- ✅ Hibrid rendszer specifikus monitorozás
- ✅ Carista-stílusú modern felhasználói felület

---

## 🚗 Támogatott Járművek

| Modell | Évjárat | Megjegyzés |
|--------|---------|------------|
| Toyota Corolla E210 | 2019-2024 | Teljes támogatás |
| Toyota Corolla Cross | 2022-2024 | Kompatibilis |
| Toyota Prius | 2016-2024 | Hibrid PIDs |

---

## 🔧 Technikai Követelmények

### Hardware
- **OBD2 Adapter:** ELM327 v2.2 vagy v2.3 (⚠️ v1.5 klónok NEM működnek íráshoz!)
- **Bluetooth:** 4.0+
- **Android:** 8.0+ (API 26+)
- **Ajánlott:** Android 15 (SDK 35)

### Software
- Kotlin 1.9.x
- Jetpack Compose (Material 3)
- Target SDK 35 / Compile SDK 35
- Gradle 8.5

---

## 🏗️ Projekt Struktúra

```
Toyota_Corolla_OBD2/
├── app/
│   ├── src/main/
│   │   ├── java/com/toyota/obd210/
│   │   │   ├── di/                    # Hilt dependency injection
│   │   │   ├── data/
│   │   │   │   ├── obd/              # OBD2 kommunikáció
│   │   │   │   ├── repository/       # Repositorok
│   │   │   │   └── model/            # Data modellek
│   │   │   ├── domain/
│   │   │   │   ├── model/            # Domain modellek
│   │   │   │   ├── usecase/          # Use case-ok
│   │   │   │   └── repository/       # Repository interfészek
│   │   │   ├── ui/
│   │   │   │   ├── theme/            # Material 3 téma
│   │   │   │   ├── navigation/       # Navigáció
│   │   │   │   ├── components/       # UI komponensek
│   │   │   │   └── screens/         # Képernyők
│   │   │   └── util/                # Segédfunkciók
│   │   └── res/
│   └── build.gradle.kts
├── docs/
│   ├── PID_RESEARCH_E210.md         # PID és regiszter dokumentáció
│   ├── SPRINT_PLAN.md               # Fejlesztési ütemterv
│   └── TPMS_GUIDE.md                # TPMS útmutató
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📖 Funkciók

### Dashboard
- Valós idejű sebesség, RPM, hőmérséklet
- Hibrid akkumulátor állapot
- Üzemanyag szint
- Gázadagolás pozíció

### Diagnosztika
- Hibakód olvasás (Stored, Pending, Permanent)
- Hibakód részletek és javaslatok
- Hibakód törlés
- Freeze frame adatok

### Beállítások
- Ajtó zárolás testreszabás
- Világítás beállítások
- Klimatizálás opciók
- Toyota Safety Sense konfiguráció
- Multimédia beállítások

### Hibrid Monitor
- HV Battery SOC (State of Charge)
- Motor/generátor nyomaték
- EV mode állapot
- Energia áramlás vizualizáció

---

## 🔐 Biztonsági Figyelmeztetések

1. **Rejtett beállítások módosítása:** Csak megfelelő ELM327 v2.x adapterrel lehetséges
2. **Gyári beállítások visszaállítása:** Bizonyos módosítások visszafordíthatatlanok
3. **Garancia:** A beállítások módosítása érvénytelenítheti a jármű garanciáját
4. **Biztonság:** Soha ne módosíts beállításokat vezetés közben!

---

## 📜 Licenc

MIT Licenc - Lásd [LICENSE](LICENSE) fájlt.

---

## 🤝 Hozzájárulás

1. Fork-olás
2. Feature branch létrehozás
3. Commit
4. Pull Request

---

**Fejlesztő:** Autonomous DevOps Agent  
**Dátum:** 2026.05.11  
**Verzió:** 1.0.0-alpha
