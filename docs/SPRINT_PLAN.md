# Sprint Ütemterv - Toyota Corolla E210 OBD2 Alkalmazás

**Projekt:** Toyota Corolla E210 OBD2 Diagnosztikai Alkalmazás  
**Verzió:** 1.0.0  
**Dátum:** 2026.05.11  
**Módszertan:** Kanban + Scrum hibrid

---

## 🎯 Projekt Áttekintés

### Cél
Teljes értékű Android OBD2 diagnosztikai alkalmazás fejlesztése a Toyota Corolla E210 (2019) modellhez, amely:
- Összes szenzor olvasása
- Rejtett beállítások módosítása
- Carista-stílusú modern felület
- Android 15+ optimalizáció

### Technikai Követelmények
- **Target SDK:** 35
- **Min SDK:** 26 (Android 8.0)
- **Programozási nyelv:** Kotlin 1.9.x
- **UI Framework:** Jetpack Compose
- **OBD Protokoll:** ELM327 v2.2/v2.3 (írási műveletekhez kötelező)
- **Build:** Gradle 8.5, AGP 8.3.x

---

## 📅 Sprint Struktúra

### Sprint 1: Alapok és Infrastruktúra (1-2 hét)
**Cél:** Projekt struktúra, build rendszer, OBD2 kommunikáció alapjai

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Gradle wrapper és projekt struktúra | 🔴 Kritikus | Build Engineer | 📋 |
| Android manifest és engedélyek | 🔴 Kritikus | Android Dev | 📋 |
| OBD2 Bluetooth kommunikáció alap | 🔴 Kritikus | OBD Expert | 📋 |
| ELM327 parancs küldő/vevő | 🔴 Kritikus | OBD Expert | 📋 |
| Állapotgép (Connection states) | 🟠 Magas | Android Dev | 📋 |

### Sprint 2: Adat réteg (1 hét)
**Cél:** Reposzitorok, DAO-k, hibrid rendszer adatok

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Sensor data modellek | 🟠 Magas | Android Dev | 📋 |
| DTC kezelés (hibakódok) | 🟠 Magas | OBD Expert | 📋 |
| Settings/Customization repo | 🟠 Magas | OBD Expert | 📋 |
| Room database integráció | 🟠 Magas | Android Dev | 📋 |
| DataSource absztrakció | 🟡 Közepes | Android Dev | 📋 |

### Sprint 3: UI/UX Fejlesztés (2 hét)
**Cél:** Carista-stílusú Material 3 felület

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Fő navigáció (Bottom nav) | 🔴 Kritikus | GUI Designer | 📋 |
| Dashboard kártya nézet | 🔴 Kritikus | GUI Designer | 📋 |
| Real-time gauge-ok | 🟠 Magas | GUI Designer | 📋 |
| Beállítások szerkesztő UI | 🟠 Magas | GUI Designer | 📋 |
| DTC viewer és törlő | 🟠 Magas | Android Dev | 📋 |
| Szűrő és kereső funkció | 🟡 Közepes | GUI Designer | 📋 |

### Sprint 4: Rejtett Beállítások (1 hét)
**Cél:** Mode 22 customization írás

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Mode 22 írási protokoll | 🔴 Kritikus | OBD Expert | 📋 |
| Beállítás mentés/visszaállítás | 🔴 Kritikus | Android Dev | 📋 |
| Factory reset funkció | 🟠 Magas | OBD Expert | 📋 |
| Batch írási műveletek | 🟡 Közepes | Android Dev | 📋 |

### Sprint 5: Hybrid Specifikus (1 hét)
**Cél:** E210 hybrid rendszer támogatás

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| HV Battery monitoring | 🔴 Kritikus | OBD Expert | 📋 |
| EV mode status | 🔴 Kritikus | OBD Expert | 📋 |
| Motor/generátor torque | 🟠 Magas | OBD Expert | 📋 |
| Battery degradation tracking | 🟡 Közepes | Android Dev | 📋 |

### Sprint 6: Google Play Előkészítés (1 hét)
**Cél:** 2026-os követelmények teljesítése

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Target SDK 35 migráció | 🔴 Kritikus | Build Eng | 📋 |
| Scoped Storage implementáció | 🔴 Kritikus | Android Dev | 📋 |
| Privacy Sandbox compliance | 🔴 Kritikus | Privacy Expert | 📋 |
| APK Signature v3 | 🔴 Kritikus | Build Eng | 📋 |
| Adatvédelmi dokumentáció | 🟠 Magas | Privacy Expert | 📋 |
| Play Console listing | 🟠 Magas | PM | 📋 |

### Sprint 7: Testing és QA (1 hét)
**Cél:** Stabil release előkészítés

| Feladat | Prioritás | Felelős | Állapot |
|---------|-----------|---------|---------|
| Unit tesztek | 🟠 Magas | Tester | 📋 |
| Integration tesztek | 🟠 Magas | Tester | 📋 |
| UI screenshot tesztek | 🟡 Közepes | Tester | 📋 |
| Performance benchmark | 🟡 Közepes | Tester | 📋 |
| Beta tesztelés (valós hw) | 🔴 Kritikus | All | 📋 |

---

## 📋 Backlog (Product Backlog Items)

### Must Have (MVP)
- [ ] Bluetooth OBD kapcsolat
- [ ] Sebesség, RPM, hőmérséklet olvasás
- [ ] Hibakód olvasás és törlés
- [ ] Egyszerű dashboard
- [ ] ELM327 v2.x kompatibilitás

### Should Have
- [ ] Rejtett beállítások olvasása
- [ ] Real-time grafikonok
- [ ] DTC előzmények
- [ ] Hybrid specifikus PID-ek

### Nice to Have
- [ ] Rejtett beállítások írása
- [ ] Multi-vehicle támogatás
- [ ] Export/Import beállítások
- [ ] Widget támogatás

---

## 🔄 Kanban Board Layout

```
┌─────────────┬─────────────┬─────────────┬─────────────┐
│   BACKLOG   │     TO DO   │   IN PROGRESS│    DONE    │
├─────────────┼─────────────┼─────────────┼─────────────┤
│ Feature X   │ Task A      │ Task D      │ Sprint 1    │
│ Feature Y   │ Task B      │ Task E      │ Sprint 2    │
│ Feature Z   │ Task C      │ Task F      │ Sprint 3    │
└─────────────┴─────────────┴─────────────┴─────────────┘
```

---

## 📊 Mérőszámok

| Metrika | Célérték |
|---------|----------|
| Build Success Rate | 100% |
| Unit Test Coverage | >70% |
| APK Méret | <15MB |
| cold start | <3s |
| OBD response time | <200ms |

---

## ⚠️ Kockázatok és Mitigációk

| Kockázat | Valószínűség | Hatás | Mitigáció |
|----------|--------------|-------|-----------|
| ELM327 klón nem működik | 🟡 Közepes | 🔴 Magas | v2.2/v2.3 kötelező |
| Régi firmware | 🟡 Közepes | 🟠 Közepes | Firmware update guide |
| PID nem támogatott | 🟢 Alacsony | 🟡 Közepes | Fallback hibakezelés |
| Jármű specifikus eltérés | 🟡 Közepes | 🟠 Közepes | Konfigurálható PIDs |

---

## 📞 Kommunikációs Csatornák

- **Napi standup:** 9:00 (virtuális)
- **Sprint review:** Minden hét péntek
- **Heti status:** Slack/Discord thread