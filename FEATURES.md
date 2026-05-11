# Toyota Corolla E210 OBD2 - Funkcióleírás

## Áttekintés

Ez a dokumentum részletesen leírja a Toyota Corolla E210 OBD2 diagnosztikai alkalmazás összes funkcióját és képességét.

---

## 1. OBD2 Kommunikáció

### 1.1 Bluetooth Kapcsolat
- **Protokoll:** Bluetooth Classic (SPP)
- **Eszközök:** ELM327 v2.2/v2.3 Bluetooth adapterek
- **Csatlakozás:** Automatikus és manuális eszköz választás
- **Állapotgép:** disconnected → connecting → connected → error

### 1.2 ELM327 Protokoll
- AT parancsok támogatása (ATZ, ATE0, ATL0, ATH0, ATSP0)
- ISO 9141-2 / ISO 14230-4 protokollok
- Mode 01: Valós idejű adatok olvasása
- Mode 02: Freeze Frame adatok
- Mode 03: Hibakódok olvasása
- Mode 04: Hibakódok törlése
- Mode 07: Pending DTC olvasása
- Mode 09: Vehicle Info olvasása
- Mode 22: Gyári-specifikus parancsok (rejtett beállítások)

---

## 2. Dashboard Funkciók

### 2.1 Motor adatok
| PID | Leírás | Mértékegység |
|-----|--------|--------------|
| 010C | Motor fordulatszám (RPM) | 1/min |
| 010D | Jármű sebesség | km/h vagy mph |
| 0105 | Hűtőfolyadék hőmérséklet | °C |
| 010F | Beszívott levegő hőmérséklet | °C |
| 0111 | Gázadagolás pozíció | % |
| 0104 | Motor terhelés | % |
| 0106 | Short Term Fuel Trim | % |
| 0107 | Long Term Fuel Trim | % |

### 2.2 Üzemanyag rendszer
| PID | Leírás | Mértékegység |
|-----|--------|--------------|
| 012F | Üzemanyag szint | % |
| 015E | Üzemanyag fogyasztás (átlag) | L/100km |
| 0100 | Üzemanyag rendszer állapot | - |

### 2.3 Hibrid-specifikus PID-ek
| PID | Leírás | Mértékegység |
|-----|--------|--------------|
| E210-HV01 | HV Battery SOC | % |
| E210-HV02 | Motor nyomaték | Nm |
| E210-HV03 | Generátor nyomaték | Nm |
| E210-HV04 | EV Mode állapot | - |
| E210-HV05 | HV Battery feszültség | V |
| E210-HV06 | HV Battery áram | A |

---

## 3. Diagnosztika

### 3.1 Hibakód (DTC) Kezelés
- **Mode 03:** Olvasás (Stored DTCs)
- **Mode 07:** Olvasás (Pending DTCs)
- **Mode 0A:** Olvasás (Permanent DTCs)
- **Mode 04:** Törlés

### 3.2 DTC Részletek
- Hibakód (P0xxx, C0xxx, B0xxx, U0xxx)
- Leírás magyarul és angolul
- Javasolt javítási lépések
- Kritikussági szint (Info, Warning, Critical)
- Kapcsolódó freeze frame adatok

### 3.3 Freeze Frame
- Soron következő hibakódhoz tartozó pillanatfelvétel
- Motor paraméterek rögzítése a hiba pillanatában
- Sebesség, RPM, hőmérséklet, terhelés

---

## 4. Rejtett Beállítások (Customization)

### 4.1 Támogatott Kategóriák

#### Ajtó zárolás
| Regiszter | Beállítás | Lehetséges értékek |
|-----------|-----------|---------------------|
| 0xF0B0 | Auto Lock | OFF / On Unlock / On Shift to Drive |
| 0xF0B1 | Auto Unlock | OFF / On Park / On Unlock |
| 0xF0B2 | Unlock All | All Doors / Driver Only |
| 0xF0B3 | Remote Start | OFF / ON |

#### Világítás
| Regiszter | Beállítás | Lehetséges értékek |
|-----------|-----------|---------------------|
| 0xF0C0 | DRL | OFF / ON |
| 0xF0C1 | Follow Me Home | OFF / 30s / 60s / 90s |
| 0xF0C2 | Approach Lighting | OFF / ON |

#### Klimatizálás
| Regiszter | Beállítás | Lehetséges értékek |
|-----------|-----------|---------------------|
| 0xF0D0 | Auto AC | OFF / ON |
| 0xF0D1 | Rear Defogger Auto | OFF / ON |

#### Toyota Safety Sense
| Regiszter | Beállítás | Lehetséges értékek |
|-----------|-----------|---------------------|
| 0xF0E0 | PCS (Pre-Collision) | OFF / ON |
| 0xF0E1 | LDA (Lane Departure) | OFF / Warning / Assist |
| 0xF0E2 | ACC (Adaptive Cruise) | OFF / ON |

#### Multimédia
| Regiszter | Beállítás | Lehetséges értékek |
|-----------|-----------|---------------------|
| 0xF0F0 | Beep Sound | OFF / ON |
| 0xF0F1 | Rear Camera Guide | OFF / ON |
| 0xF0F2 | Parking Sensors | OFF / ON |

### 4.2 Beállítás Műveletek
- **Olvasás:** Mode 22 Seed-Key authentikációval
- **Írás:** Gyári titkosított protokollal
- **Mentés:** JSON export local storage-ba
- **Visszaállítás:** Gyári alapértelmezett értékek

---

## 5. TPMS (Tire Pressure Monitoring)

### 5.1 Funkciók
- Egyéni guminyomás értékek beállítása
- PSI és Bar mértékegység választás
- Érzékelő ID olvasás
- Low pressure warning threshold

### 5.2 Támogatott Formátumok
- Toyota specifikus TPMS protokoll
- SAE J2657 kompatibilitás

---

## 6. Jármű Információ

### 6.1 Mode 09 - Vehicle Info
- VIN szám olvasás
- Kalibrációs ID
- ECU azonosító
- Gyártási dátum

### 6.2 Támogatott Modellek
| Modell | Kód | Évjárat |
|--------|-----|--------|
| Corolla E210 | ZWE21 | 2019-2024 |
| Corolla Cross | ZWG26 | 2022-2024 |
| Prius | ZVW50 | 2016-2024 |

---

## 7. Android Auto Integráció

### 7.1 CarAppService
- `ToyotaCarAppService` - Fő belépési pont Android Auto-hoz
- `ToyotaCarSession` - Session kezelés
- AndroidManifest.xml-ben regisztrálva

### 7.2 Képernyők Android Auto-n

| Képernyő | Leírás |
|----------|--------|
| `CarMainMenuScreen` | Főmenü - Dashboard, Diagnosztika, Beállítások, Hibrid |
| `CarDashboardScreen` | Valós idejű szenzor adatok (Speed, RPM, Temp, stb.) |
| `CarDiagnosticsScreen` | DTC kategóriák és törlés |
| `CarSettingsScreen` | Rejtett beállítások kategóriák |
| `CarSettingsDoorLocksScreen` | Ajtó zárolás beállítások |
| `CarSettingsLightingScreen` | Világítás beállítások |
| `CarSettingsClimateScreen` | Klimatizálás beállítások |
| `CarSettingsMultimediaScreen` | Multimédia beállítások |
| `CarSettingsTssScreen` | Toyota Safety Sense beállítások |
| `CarHybridScreen` | Hibrid rendszer monitorozás |

### 7.3 Biztonsági Megfontolások
- **Vezetés közben:** Csak olvasási műveletek engedélyezettek
- **Álló jármű:** Beállítások módosítása megerősítés után
- **Megerősítő dialog:** AlertDialog minden írási művelethez

---

## 8. User Interface (Telefon alkalmazás)

### 8.1 Navigáció
- Bottom Navigation (3 tab)
  - Dashboard
  - Diagnostics
  - Settings

### 8.2 Dashboard nézet
- Valós idejű műszerek (gauge-ok)
- Lista nézet szenzor adatokkal
- Grafikon megjelenítés idővel
- Hybrid energy flow vizualizáció

### 8.3 Diagnosztika nézet
- DTC lista szűrhető listával
- DTC részletek dialog
- Törlés megerősítés dialog
- Freeze frame megjelenítés

### 8.4 Beállítások nézet
- Kategória alapú csoportosítás
- Toggle/Selection/Radio UI elemek
- Gyári érték visszaállítás
- Export/Import funkciók

---

## 9. Hibakezelés

### 9.1 Kapcsolati Hibák
- Adapter nem található
- Bluetooth kikapcsolva
- Kapcsolat megszakadt
- Időtúllépés

### 9.2 OBD Hibák
- Nincs válasz (No Data)
- Nem támogatott PID
- Session hiba
- authentikáció sikertelen

### 9.3 Alkalmazás Hibák
- Memory warning
- Storage telítettség
- jogosultság hiány

---

## 10. Adat Menedzsment

### 10.1 Lokális Tárolás
- Room database
- SharedPreferences (beállítások)
- JSON export/import

### 10.2 Cache
- PID válaszok cache-elése
- DTC előzmények
- Beállítás backup

---

## 11. Biztonság

### 11.1 Jogosultságok
- BLUETOOTH (kapcsolat)
- BLUETOOTH_ADMIN (párosítás)
- ACCESS_FINE_LOCATION (Android 12+)
- ACCESS_COARSE_LOCATION

### 11.2 Figyelmeztetések
- Minden írási művelet előtt figyelmeztetés
- Gyári beállításokhoz visszaállítás lehetősége
- Warranty disclaimer

---

## 12. Jövőbeli Funkciók

- [ ] Data logging és export (CSV/JSON)
- [ ] Widget támogatás (Glance API)
- [ ] OBD2 írási funkció (CAN bus közvetlen)
- [ ] Service reset (olaj, inspection)
- [ ] WiFi OBD adapter támogatás
- [ ] Multi-vehicle támogatás
- [ ] Apple CarPlay/Android Auto teljes integráció
