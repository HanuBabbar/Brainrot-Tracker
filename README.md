<div align="center">
  <h1>🧠 Brainrot Tracker</h1>
  <p><b>Take back your attention. Stop the brainrot.</b></p>
  <p><i>An Android app that counts every short-form video you swipe through — so you finally know how deep the hole goes.</i></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
    <img src="https://img.shields.io/badge/Language-Kotlin-0095D5?style=for-the-badge&logo=kotlin&logoColor=white" />
    <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
    <img src="https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-brightgreen?style=for-the-badge" />
    <img src="https://img.shields.io/badge/Status-Coming%20Soon%20to%20Play%20Store-orange?style=for-the-badge&logo=googleplay&logoColor=white" />
  </p>
</div>

<br/>

## 🎯 What is Brainrot Tracker?

Most screen time apps tell you *how long* you used an app. Brainrot Tracker tells you *how many videos* you actually watched.

There's a big difference between "2 hours on Instagram" and **"You swiped through 143 videos today."** One is easy to dismiss. The other is uncomfortable — and discomfort is where habit change starts.

Using Android's Accessibility Service, Brainrot Tracker silently monitors your short-form video consumption across **Instagram Reels, YouTube Shorts, and TikTok**, counting every individual swipe in real time. No battery drain, no slowdowns, no sending your data anywhere without your consent.

---

## ✨ Features

| Feature | Description |
|---|---|
| 📊 **Swipe-Level Tracking** | Counts every individual short-form video swipe across Instagram, TikTok & YouTube Shorts |
| 🛑 **Daily Limits & Alerts** | Set a daily swipe threshold — get an instant high-priority notification + haptic buzz when you cross it |
| 🏆 **Friends & Leaderboard** | Add friends, compete globally. The goal? Have the *lowest* brainrot score |
| ⚡ **CPU Performance Modes** | Choose between Battery Saver, Balanced, or High-Performance tracking |
| 📱 **Home Screen Widget** | A Glance widget that puts your brainrot stats front-and-center on your home screen |
| 🔒 **Banking Mode** | One-tap pause from the notification tray for banking apps that block Accessibility Services |
| 🎨 **Material You Theming** | Full Dark/Light mode support built on Material Design 3 |

---

## 🛠️ Tech Stack

Built entirely with modern Android best practices:

| Layer | Technology |
|---|---|
| **UI** | [Jetpack Compose](https://developer.android.com/jetpack/compose) + Material Design 3 |
| **Architecture** | MVVM with Kotlin Coroutines & StateFlow |
| **Local Storage** | [Room Database](https://developer.android.com/training/data-storage/room) + [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) |
| **Networking** | [Ktor Client](https://ktor.io/) — async, lightweight |
| **Background** | Android Accessibility Service + Coroutine Workers |
| **Widgets** | [Jetpack Glance](https://developer.android.com/jetpack/compose/glance) |

---

## 📱 Tracked Platforms

- **Instagram** — Reels
- **YouTube** — Shorts
- **TikTok** — For You Page

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (Koala or newer recommended)
- JDK 17
- Android device or emulator running **Android 7.0 (API 24)** or higher

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/HanuBabbar/Brainrot-Tracker.git
   ```
2. Open the project in Android Studio.
3. Sync Gradle files.
4. Run the app on your device or emulator.

### First Launch
On first launch, you'll be prompted to enable the **Accessibility Service**. This is required for the app to detect scroll events in other apps.

> **Privacy note**: All tracking is done locally on-device. Swipe data is only synced to the server if you explicitly opt-in to the Friends/Leaderboard features.

---

## 🔒 Privacy & Banking Mode

Some banking apps enforce strict security policies that block the app from running if any Accessibility Service is active.

Brainrot Tracker solves this with **Banking Mode**:
1. Tap **"Disable App"** on the persistent notification before opening your bank app
2. The tracker shuts down instantly
3. Do your banking
4. Tap the temporary "disabled" notification to jump back to settings and re-enable

Zero friction. No workarounds needed.

---

## 🗺️ Roadmap

A few ideas cooking for future versions:

- 🐾 **Virtual Brain Pet** — A visual brain that gets "sick" as you approach your limit
- 🌫️ **Doomscroll Interventions** — Breathing overlays and grayscale mode when limits are near
- ⚔️ **Friend Duels** — 7-day head-to-head focus challenges
- 📈 **Scroll Velocity Tracking** — Detects rapid swiping and triggers a "Touch Grass" notification
- 🔐 **Lock Screen Widget** — See your brainrot level before you even unlock your phone

---

<div align="center">
  <i>You already know you scroll too much. Now you'll know exactly how much.</i>
  <br/><br/>
  <b>⭐ Star this repo if you're rooting for the project!</b>
</div>
