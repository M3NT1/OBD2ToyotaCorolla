# Toyota Corolla E210 TPMS (Tyre Pressure Monitoring System) Útmutató

**Dokumentum verzió:** 1.0  
**Dátum:** 2026.05.11  
**Céljármű:** Toyota Corolla E210 (2019)  
**Adapter követelmény:** ELM327 v2.2/v2.3

---

## 📋 TPMS Áttekintés

A Toyota Corolla E210 közvetlen TPMS (Tyre Pressure Monitoring System) rendszert használ, amely minden kerékben egyedi azonosítójú nyomásszenzorral rendelkezik.

### Rendszer komponensek:
| Alkatrész | Leírás | OBD2 elérés |
|----------|--------|-------------|
| TPMS ECU (7E6) | Központi TPMS vezérlő | ❌ Direktírás nem lehetséges |
| Szenzorok (4x) | Egyedi ID-jú nyomásszenzorok | ❌ Nem programozható |
| Infotainment kijelző | Nyomás megjelenítés | ✅ Csak olvasás |

---

## 🔧 OBD2-n Keresztül Elérhető Funkciók

### ✅ Módosítható Beállítások (Mode 22)

| Regiszter | Beállítás | Lehetséges értékek | Alapértelmezett |
|-----------|-----------|-------------------|-----------------|
| `0x0E00` | TPMS Warning Mode | 0=Indicator Only, 1=Message Display | 1 |
| `0x0E01` | TPMS Pressure Unit | 0=kPa, 1=PSI, 2=Bar | 0 |

### ✅ Olvasható PID-ek (Mode 01)

| PID | Leírás | Mértékegység | Megjegyzés |
|-----|--------|--------------|------------|
| `0136` | Request tire pressure (FL) | kPa | - |
| `0137` | Request tire pressure (FR) | kPa | - |
| `0138` | Request tire pressure (RL) | kPa | - |
| `0139` | Request tire pressure (RR) | kPa | - |

---

## ⚠️ Kerékcsere és Szenzorcsere Utáni Teendők

### 🔄 Automatikus TPMS Kalibrálás

A Toyota Corolla E210 **automatikusan megtanulja** az új szenzor ID-ket vezetés közben.

**Folyamat:**
1. **Új szenzor behelyezése** - Helyezd be az új TPMS szenzort a kerékbe
2. **Jármű beindítása** - Gyújtás be, de motor ne legyen beindítva
3. **Inicializálási mód** - Néhány másodperc alatt a jármű felismeri az új szenzort
4. **Vezetés** - Legalább 20 km/h sebesség elérése szükséges az ID megtanulásához
5. **Ellenőrzés** - Infotainment kijelzőn ellenőrizd a nyomásértékeket

### 📱 Alkalmazásból Végrehajtható Műveletek

| Művelet | Lehetséges? | Metódus |
|---------|-------------|---------|
| Szenzor ID programozás | ❌ **NEM** | Gyári szerszám szükséges |
| Nyomás értékek olvasása | ✅ Igen | Mode 01 PIDs |
| Figyelmeztetési mód változtatás | ✅ Igen | Mode 22 `0x0E00` |
| Mértékegység változtatás | ✅ Igen | Mode 22 `0x0E01` |
| TPMS ECU reset | ✅ Igen | Jármű menü / OBD parancs |

---

## 🔬 TPMS Reset Parancsok

### Gyári Reset (Manuális)
1. Menü → Beállítások → TPMS → "Initialize/Reset"
2. Vagy: Hosszan nyomd a TPMS reset gombot (ha van)

### OBD2 Reset Parancs (Kísérleti)
```
ATSH7E6          # TPMS ECU header
ATSP6            # ISO 9141-2 protokoll
22 00 01         # TPMS reset parancs (kísérleti)
```

**⚠️ Figyelem:** Ez a parancs nem minden firmware verzión működik. Tesztelés szükséges!

---

## 📊 Szenzor Specifikációk

| Specifikáció | Érték |
|-------------|-------|
| Működési frekvencia | 433.92 MHz |
| Nyomástartomány | 0-50 PSI (0-345 kPa) |
| Hőmérséklet tartomány | -40°C to +125°C |
| Elem élettartam | kb. 5-7 év |
| Szenzor típus | Schrader SEIKEN (gyári) |

---

## 🔗 Kapcsolódó Forumok és Források

1. **Toyota TIS Techinfo** - Hivatalos Toyota szerviz információk
2. **ToyotaNation Forum** - Felhasználói tapasztalatok
3. **Reddit r/Toyota** - TPMS kérdések és válaszok
4. **GitHub: Android-OBD-II-Examiner** - TPMS PID kutatás
5. **Torque Pro PID Database** - Extended PIDs

---

## ⚠️ Fontos Megjegyzések

1. **OEM Korlátozás:** A TPMS szenzor ID-ket kizárólag gyári diagnosztikai eszközökkel lehet programozni
2. **Biztonság:** Soha ne módosíts TPMS beállításokat vezetés közben!
3. **Garancia:** A TPMS módosítása nem affectálja a jármű garanciáját, de a szenzorok garanciáját igen
4. **Szakszerviz:** Szenzorcsere esetén ajánlott hivatalos szervizben elvégeztetni az ID programozást

---

## 📝 TODO Lista Fejlesztőknek

- [ ] TPMS Mode 01 PID-ek implementálása az alkalmazásban
- [ ] Real-time nyomás megjelenítés dashboard-on
- [ ] TPMS warning grafikon készítés
- [ ] Kerékcsere utáni reset funkció tesztelése
- [ ] Szenzor ID olvasás lehetőségének kutatása
