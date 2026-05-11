# Toyota Corolla E210 (2019) OBD2 PID és Regiszter Dokumentáció

## 🔬 Kutatási Összefoglaló

**Dokumentum verzió:** 1.0  
**Dátum:** 2026.05.11  
**Céljármű:** Toyota Corolla E210 1.8 Hybrid (2019)  
**Protokoll alap:** ELM327 v2.2/v2.3 (Kötelező írási műveletekhez!)

---

## 📋 SZABványos OBD2 PIDs (Mode 01) - E210 Specifikus

### Motor és sebesség
| PID | Név | Leírás | Mértékegység | E210 specifikus |
|-----|-----|--------|--------------|-----------------|
| 0100 | PIDs Supported | Támogatott PID-ek | bitcoded | ✅ |
| 0104 | Engine Load | Motor terhelés | % | 0-100 |
| 0105 | Coolant Temp | Hűtőfolyadék hőmérséklet | °C | -40 to 215 |
| 0106 | Fuel Trim Bank 1 | Üzemanyag trim | % | -100 to 99.22 |
| 0107 | Short Term Fuel Trim Bank 1 | Rövid távú üzemanyag trim | % | -100 to 99.22 |
| 0108 | Long Term Fuel Trim Bank 1 | Hosszú távú üzemanyag trim | % | -100 to 99.22 |
| 010B | Intake Manifold Pressure | Szívócső nyomás | kPa | 0-255 |
| 010C | Engine RPM | Motor fordulatszám | RPM | 0-16383.75 |
| 010D | Vehicle Speed | Sebesség | km/h | 0-255 |
| 010E | Timing Advance | Gyújtás időzítés | ° | -64 to 63.5 |
| 010F | Intake Air Temp | Beszívott levegő hőmérséklet | °C | -40 to 215 |
| 0110 | MAF Air Flow Rate | Légtömegáram | g/s | 0-655.35 |
| 0111 | Throttle Position |Fokozat pozíció | % | 0-100 |
| 011F | Run Time Since Engine Start | Futási idő indítás óta | sec | 0-65535 |
| 0121 | Distance with MIL on | MIL távolság | km | 0-65535 |
| 012F | Fuel Tank Level | Üzemanyag szint | % | 0-100 |
| 0142 | Control Module Voltage | Vezérlőmodul feszültség | V | 0-655.35 |
| 0146 | Ambient Air Temp | Környezeti hőmérséklet | °C | -40 to 215 |
| 0151 | Gear Position | Sebességfokozat | Gear | 0-6 (Hybrid) |

### Hybrid Specifikus PIDs (Nem szabványos, de az E210-en működik)
| PID | Név | Leírás | Mértékegység |
|-----|-----|--------|--------------|
| 014D | Hybrid Battery SOC | Hibrid akkumulátor töltöttség | % |
| 015E | Engine Coolant Temp (HV) | HV hűtőfolyadék | °C |
| 021B | EV Mode Status | EV mód státusz | flag |
| 0220 | Motor Torque | Elektromotor nyomaték | Nm |
| 0223 | Hybrid Battery Current | Akkumulátor áram | A |
| 0224 | Hybrid Battery Voltage | Akkumulátor feszültség | V |

---

## 🔐 REJTETT BEÁLLÍTÁSOK - Customization Regiszterek (Mode 22)

**⚠️ FIGYELMEZTETÉS: Ezek a beállítások csak ELM327 v2.2/v2.3-mal írhatók!**

### Ajtók és Zárolás (Door Lock Customization)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x0B00 | Unlock Method | 0=All, 1=Driver, 2=Driver+2x | 1 |
| 0x0B01 | Auto Lock Speed | 0=Off, 10/20/30 km/h | 20 |
| 0x0B02 | Auto Unlock Method | 0=Off, 1=Park, 2=IGN Off | 1 |
| 0x0B03 | Horn Lock Confirm | 0=Off, 1=Short, 2=Long | 1 |
| 0x0B04 | Horn Unlock Confirm | 0=Off, 1=Short, 2=Double | 2 |

### Világítás (Lighting Customization)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x0C00 | DRL Enable | 0=Off, 1=On | 1 |
| 0x0C01 | DRL Type | 0=Full, 1=Partial, 2=Auto | 2 |
| 0x0C02 | Welcome Light Duration | 0=Off, 15/30/60 sec | 30 |
| 0x0C03 | Follow Me Home | 0=Off, 30/60/90/120 sec | 30 |
| 0x0C04 | Interior Light Auto | 0=Off, 1=Auto | 1 |

### Klimatizálás (Climate Customization)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x0D00 | Auto Recirculation | 0=Manual, 1=Auto | 1 |
| 0x0D01 | Defog Auto Start Temp | 0=Off, 10/15/20 °C | 15 |
| 0x0D02 | Remote Start Duration | 0=Off, 5/10/15/20 min | 10 |

### TPMS (Tyre Pressure Monitoring)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x0E00 | TPMS Warning Mode | 0=Indicator, 1=Message | 1 |
| 0x0E01 | TPMS Pressure Unit | 0=kPa, 1=PSI, 2=Bar | 0 |

### Multimédia és Kijelző (Infotainment)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x0F00 | Beep Volume | 0=Off, 1=Low, 2=High | 1 |
| 0x0F01 | Camera Guidelines | 0=Off, 1=On | 1 |
| 0x0F02 | Park Assist Volume | 0=Off, 1=Low, 2=High | 1 |
| 0x0F03 | Start Sound | 0=Off, 1=Standard, 2=Premium | 1 |

### Toyota Safety Sense (TSS-P)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x1000 | PCS Enable | 0=Off, 1=On | 1 |
| 0x1001 | PCS Sensitivity | 0=Early, 1=Normal, 2=Late | 1 |
| 0x1002 | LKA Enable | 0=Off, 1=On | 1 |
| 0x1003 | LDA Alert Type | 0=Sound, 1=Vibration, 2=Both | 2 |
| 0x1004 | ACC Follow Distance | 1-5 (1=Close, 5=Far) | 3 |
| 0x1005 | RSA Enable | 0=Off, 1=On | 1 |

### Head-Up Display (HUD) - Ha felszerelt
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x1100 | HUD Brightness | 0=Auto, 1-5 (Manual) | 0 |
| 0x1101 | HUD Content | Bitmask: Speed/Navi/TSS/Other | 0x0F |

### Ablak Beállítások (Window Customization)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x1400 | One-Touch Up Driver | 0=Off, 1=On | 1 |
| 0x1401 | One-Touch Up Passenger | 0=Off, 1=On | 1 |
| 0x1402 | One-Touch Up Rear | 0=Off, 1=On | 1 |
| 0x1403 | Anti-Trap Sensitivity | 0=Low, 1=Medium, 2=High | 1 |

### Index/Villogó Beállítások (Indicator Customization)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x1500 | Turn Signal Flashes | 1-5 (1=1 Flash, 3=3 Flashes) | 3 |
| 0x1501 | Lane Change Flash | 0=Off, 1=1 Flash, 2=2 Flashes | 1 |
| 0x1502 | Hazard Auto Off | 0=Off, 1=On | 1 |

### Biztonsági Öv Beállítások (Seatbelt Minder)
| Regiszter | Név | Lehetséges értékek | Alapértelmezett |
|-----------|-----|-------------------|-----------------|
| 0x1600 | Seatbelt Minder | 0=Off, 1=On, 2=Continuous | 1 |
| 0x1601 | Minder Chime Volume | 0=Off, 1=Low, 2=High | 1 |

---

## 🔧 DIAGNOSTIC TROUBLE CODES (DTCs) - Mode 03/07

### Gyakori E210 hibakódok
| Kód | Név | Rendszer |
|-----|-----|----------|
| P0A90 | Motor Electronics Coolant Pump | Hybrid |
| P0B1D | Hybrid Battery Voltage Sense | Hybrid |
| P0300-P0304 | Misfire Detection | Engine |
| P0420 | Catalytic System Efficiency | Emissions |
| C0200 | ABS Sensor Malfunction | Brake |
| C1250 | Steering Angle Sensor | EPS |
| B1500 | Headlamp Assembly | Lighting |

---

## 📊 ELM327 Parancs Struktúra

### Olvasási parancs (Mode 01):
```
ATSH7E4          # Toyota header beállítás
ATFSPH7E4        # Fordított formátum
01 0D             # Sebesség olvasása
```

### Customization olvasás (Mode 22):
```
ATSH7E4
22 0B 00         # Ajtó beállítások olvasása
```

### Customization írás (Mode 22):
```
ATSH7E4
22 0B 00 xx      # xx = új érték (pl. 00 = All unlock)
```

### DTC olvasás:
```
03                # Stored DTCs
07                # Pending DTCs
0A                # Permanent DTCs
```

---

## ⚠️ FONTOS MEGJEGYZÉSEK

1. **ELM327 v1.5 Klónok:** Nem támogatják az írási műveleteket! Kötelező v2.2 vagy v2.3!

2. **Toyota Hibrid Protokoll:** Az E210 hybrid egyedi CAN azonosítókat használ a hibrid rendszerhez.

3. **Állapot visszaállítás:** Bizonyos beállítások csak a jármű újraindítás után lépnek érvénybe.

4. **OEM Diagnosztika:** A teljes customization opciókhoz eredeti Toyota Techstream szoftver szükséges.

---

## 📚 Források

- Toyota Corolla E210 Service Manual
- Android OBD-II Examiner (GitHub)
- Toyota TIS Techinfo Forums
- OBD Prom Codes Database (obd-codes.com)
- Torque Pro PID Database