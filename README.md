<div align="center">
  <img src="docs/icon.svg" alt="Wortik Icon" width="96" height="96" />
  <h1>Wortik</h1>
  <p><strong>Offline-first German–English dictionary for Android</strong></p>

  <p>
    <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
    <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin" />
    <img src="https://img.shields.io/badge/Language-German%20%F0%9F%87%A9%F0%9F%87%AA-DD0000?style=for-the-badge" alt="German" />
    <a href="LICENSE">
      <img src="https://img.shields.io/badge/License-MIT-blue?style=for-the-badge" alt="MIT License" />
    </a>
  </p>

  <p>
    <a href="releases/Wortik-v1.0.0.apk"><strong>Download APK (v1.0.0)</strong></a>
  </p>
</div>

---

## Screenshots

| Home & Word of the Day | 100 Categories & A–Z | Saved & Gender Ratio |
| :---: | :---: | :---: |
| <img src="docs/screenshots/1.jpg" width="240" /> | <img src="docs/screenshots/2.jpg" width="240" /> | <img src="docs/screenshots/3.jpg" width="240" /> |

| Sentence Translation | Word Details & Kasus | History & Translation Log |
| :---: | :---: | :---: |
| <img src="docs/screenshots/4.jpg" width="240" /> | <img src="docs/screenshots/5.jpg" width="240" /> | <img src="docs/screenshots/6.jpg" width="240" /> |

---

## Features

- **97,000+ Offline Words** — Fast Room + SQLite FTS4 search with smart umlaut handling (`ä/ö/ü/ß`) and voice search (`de-DE`).
- **Color-Coded Genders** — Harmonized Material You colors for `der` / `die` / `das`.
- **Grammar Tables** — Noun declensions (*Kasus*) and verb conjugations (*Präsens, Präteritum, Perfekt*).
- **Pronunciation** — Native German audio + offline Android TTS with `0.75× / 1×` speed control.
- **Explore** — 100 curated vocabulary categories + alphabetical A–Z browser.
- **Flashcards & History** — 3D-flipping flashcards, gender stats, and searchable word + sentence history.
- **Online Translation** — Instant DE ⇄ EN fallback for sentences and out-of-dictionary words.
- **Material You Themes** — Dynamic Color, 6 custom palettes, and AMOLED True Black.

---

## Tech Stack

- **UI:** Kotlin, Jetpack Compose, Material 3, Coil
- **Architecture:** MVVM + Repository pattern, Hilt DI, Coroutines & Flow
- **Storage:** Room (pre-populated FTS4 dictionary DB + separate user-data DB), DataStore Preferences
- **Tests:** JUnit 4 unit tests

---

## Build

```bash
git clone https://github.com/sajadrahimi-dev/Wortik.git
cd Wortik
./gradlew testDebugUnitTest assembleDebug
```

*(Optional)* Add `PIXABAY_API_KEY=your_key` to `local.properties` to enable noun photos.

---

## Credits & License

- Dictionary data: [Wiktionary](https://www.wiktionary.org/) via [Kaikki](https://kaikki.org/) (CC BY-SA 4.0)
- Noun photos: [Pixabay](https://pixabay.com/)
- Licensed under the [MIT License](LICENSE)
