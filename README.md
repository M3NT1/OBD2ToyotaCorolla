# Toyota Corolla E210 OBD2 Diagnostic Application

<div align="center">

![Toyota Corolla E210](https://img.shields.io/badge/Vehicle-Toyota%20Corolla%20E210-ff0000?style=for-the-badge)
![Android](https://img.shields.io/badge/Android-15%2B-34A853?style=for-the-badge&logo=android)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9.x-7F52FF?style=for-the-badge&logo=kotlin)
![Target SDK](https://img.shields.io/badge/Target%20SDK-35-4285F4?style=for-the-badge)

**⚠️ Warning: This project is for educational and development purposes. Modifying vehicle settings is done at your own risk.**

</div>

---

## 📋 Project Overview

Modern Android OBD2 diagnostic application for Toyota Corolla E210 (2019) model, enabling:
- ✅ Read all sensors in real-time
- ✅ Read and clear DTC (Diagnostic Trouble Codes)
- ✅ Modify hidden settings (Customization)
- ✅ Hybrid system specific monitoring
- ✅ Carista-style modern UI

---

## 🚗 Supported Vehicles

| Model | Year | Notes |
|-------|------|-------|
| Toyota Corolla E210 | 2019-2024 | Full support |
| Toyota Corolla Cross | 2022-2024 | Compatible |
| Toyota Prius | 2016-2024 | Hybrid PIDs |

---

## 🔧 Technical Requirements

### Hardware
- **OBD2 Adapter:** ELM327 v2.2 or v2.3 (⚠️ v1.5 clones do NOT work for writing!)
- **Bluetooth:** 4.0+
- **Android:** 8.0+ (API 26+)
- **Recommended:** Android 15 (SDK 35)

### Software
- Kotlin 1.9.x
- Jetpack Compose (Material 3)
- Target SDK 35 / Compile SDK 35
- Gradle 8.5

---

## 🏗️ Project Structure

```
Toyota_Corolla_OBD2/
├── app/
│   ├── src/main/
│   │   ├── java/com/toyota/obd210/
│   │   │   ├── di/                    # Hilt dependency injection
│   │   │   ├── data/
│   │   │   │   ├── obd/              # OBD2 communication
│   │   │   │   ├── repository/       # Repositories
│   │   │   │   └── model/            # Data models
│   │   │   ├── domain/
│   │   │   │   ├── model/            # Domain models
│   │   │   │   ├── usecase/          # Use cases
│   │   │   │   └── repository/       # Repository interfaces
│   │   │   ├── ui/
│   │   │   │   ├── theme/            # Material 3 theme
│   │   │   │   ├── navigation/       # Navigation
│   │   │   │   ├── components/       # UI components
│   │   │   │   └── screens/         # Screens
│   │   │   └── util/                # Utilities
│   │   └── res/
│   └── build.gradle.kts
├── docs/
│   ├── PID_RESEARCH_E210.md         # PID and register documentation
│   ├── SPRINT_PLAN.md               # Development schedule
│   └── TPMS_GUIDE.md                # TPMS guide
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## 📖 Features

### Dashboard
- Real-time speed, RPM, temperature
- Hybrid battery status
- Fuel level
- Throttle position

### Diagnostics
- DTC reading (Stored, Pending, Permanent)
- DTC details and suggestions
- DTC clearing
- Freeze frame data

### Settings
- Door lock customization
- Lighting settings
- Climate control options
- Toyota Safety Sense configuration
- Multimedia settings

### Hybrid Monitor
- HV Battery SOC (State of Charge)
- Motor/generator torque
- EV mode status
- Energy flow visualization

---

## 🔐 Safety Warnings

1. **Hidden settings modification:** Only possible with proper ELM327 v2.x adapter
2. **Factory reset:** Some modifications are irreversible
3. **Warranty:** Modifying settings may void vehicle warranty
4. **Safety:** Never modify settings while driving!

---

## 📜 License

MIT License - See [LICENSE](LICENSE) file.

---

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Commit changes
4. Create Pull Request

---

**Developer:** Autonomous DevOps Agent  
**Date:** 2026.05.11  
**Version:** 1.0.0-alpha
