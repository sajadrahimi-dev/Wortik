# Wortik — Offline German Dictionary for Android

**Wortik** is a modern, fast, offline-first German–English dictionary Android app built with **Kotlin**, **Jetpack Compose**, **Material 3 (Material You)**, **Room FTS4**, and **Hilt**.

Designed for German learners, Wortik combines a pre-populated **97,000+ entry offline SQLite database** with color-coded grammatical genders (`der` / `die` / `das`), full declension and conjugation tables, native audio pronunciation, flashcards, 100 curated vocabulary categories, and instant online sentence translation.

---

## Key Features

- **Offline-First Search (97,309 Entries)**
  - Parallel prefix + SQLite **FTS4** full-text search with custom relevance ranking (`WordSearchRanker`).
  - Smart German umlaut normalization (`ä → ae/a`, `ö → oe/o`, `ü → ue/u`, `ß → ss`) plus a dedicated `ä ö ü ß` quick-input strip.
  - Voice search (`de-DE`) and system-wide **"Lookup in Wortik"** (`ACTION_PROCESS_TEXT`) from any app.
  - Automatic online translation fallback for full sentences or words not in the offline database.
- **Rich Word Details**
  - Harmonized Material You color-coding for masculine (`der`), feminine (`die`), and neuter (`das`) nouns.
  - Noun declension tables (*Nominativ, Genitiv, Dativ, Akkusativ* — Singular & Plural) and verb conjugation tables (*Präsens, Präteritum, Perfekt*).
  - Native German audio playback alongside offline Android TTS (`Locale.GERMANY`), `0.75× / 1×` speed toggle, and automatic mute-detection toast that resumes playback as soon as media volume is raised.
  - Compound & related word discovery + optional Pixabay noun photography with 24-hour caching.
- **Explore & Vocabulary Building**
  - **100 curated German categories** organized into 10 topic groups with search and filtering.
  - Paginated **A–Z** (+ `Ä, Ö, Ü`) alphabetical browser.
  - **Word of the Day** with a persistent daily randomizer.
  - **Saved** vocabulary with gender ratio bar, article filters, and a **3D-flipping Flashcard** review mode.
  - **History** tracking both looked-up words and translated sentences, grouped by *Today / Earlier* with swipe-to-delete.
- **Personalization**
  - Dynamic Color (Android 12+), 6 custom Material 3 color palettes, Light/Dark/System modes, and **AMOLED True Black**.

---

## Tech Stack & Architecture

- **Language:** Kotlin (JVM 17), Coroutines & Flow
- **UI:** Jetpack Compose, Material 3, Navigation Compose, Coil
- **Dependency Injection:** Dagger Hilt
- **Persistence:**
  - `WortikDatabase` — Read-only pre-populated SQLite database with Room + FTS4 (`assets/databases/wortik.db`)
  - `UserDataDatabase` — Separate Room database (`wortik_user_data.db`) with explicit migrations so user bookmarks, word history, and translation history survive dictionary updates
  - Jetpack DataStore Preferences for theme & user settings
- **Serialization:** `kotlinx.serialization`
- **Testing:** JUnit 4 unit tests for German text normalization and search ranking

```text
app/src/main/java/com/wortik/app/
├── data/
│   ├── local/        # Room DAOs, Entities, Databases, DataStore, Categories
│   ├── remote/       # Pixabay photo service (24h cache), Online translation
│   └── repository/   # DictionaryRepositoryImpl, WordSearchRanker
├── di/               # Hilt modules
├── domain/
│   ├── model/        # WordEntry, NounDeclension, VerbConjugation, etc.
│   └── repository/   # DictionaryRepository interface
└── ui/
    ├── components/   # FloatingBottomBar, PronunciationControls, VolumeToast, WordListItem
    ├── screens/      # Home, Explore, Saved, History, WordDetail, Settings
    └── theme/        # Material 3 palettes, AMOLED Black, harmonized gender colors
```

---

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/sajadrahimi-dev/Wortik.git
   ```
2. Open the project in **Android Studio** (JDK 17, Compile SDK 35, Min SDK 26).
3. *(Optional)* To enable noun photographs via Pixabay, add your free [Pixabay API key](https://pixabay.com/api/docs/) to `local.properties` (this file is gitignored):
   ```properties
   PIXABAY_API_KEY=your_pixabay_api_key_here
   ```
   Without this key, the app builds and runs normally with photos disabled.
4. Run unit tests and build:
   ```bash
   ./gradlew testDebugUnitTest assembleDebug
   ```

---

## Data & Attribution

- **Dictionary Data:** [Wiktionary](https://www.wiktionary.org/) via [Kaikki](https://kaikki.org/) (CC BY-SA 4.0)
- **Noun Photographs (optional):** [Pixabay](https://pixabay.com/) (Pixabay License)

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
