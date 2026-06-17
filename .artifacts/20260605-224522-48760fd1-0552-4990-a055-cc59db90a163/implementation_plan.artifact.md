# Room Database Implementation Plan

We will set up the local data layer using Room (which is a wrapper around SQLite) to store consumption stats.

## Proposed Changes

### Build Configuration

#### [libs.versions.toml](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/gradle/libs.versions.toml)
- Add Room versions and library definitions.

#### [build.gradle.kts](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/app/build.gradle.kts)
- Apply the KSP plugin (for Room processing).
- Add Room dependencies.

---

### Data Layer (`com.example.brainrottracker.data.local`)

#### [NEW] [UsageEntity.kt](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/app/src/main/java/com/example/brainrottracker/data/local/UsageEntity.kt)
- Define a table `usage_stats` with:
    - `id`: Primary key (auto-generate).
    - `date`: String (e.g., "2024-06-05").
    - `platform`: String ("YouTube" or "Instagram").
    - `count`: Int (number of swipes).

#### [NEW] [UsageDao.kt](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/app/src/main/java/com/example/brainrottracker/data/local/UsageDao.kt)
- Define methods to:
    - `upsertUsage(UsageEntity)`: Add or update a row.
    - `getUsageByDate(String, String)`: Fetch count for a specific day and platform.
    - `getWeeklyUsage()`: Fetch stats for the last 7 days.

#### [NEW] [AppDatabase.kt](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/app/src/main/java/com/example/brainrottracker/data/local/AppDatabase.kt)
- Standard Room database setup.

---

### Repository Layer (`com.example.brainrottracker.data.repository`)

#### [NEW] [UsageRepository.kt](file:///C:/Users/Hanu/AndroidStudioProjects/BrainrotTracker/app/src/main/java/com/example/brainrottracker/data/repository/UsageRepository.kt)
- High-level logic: "If date exists, count++, else create row with count=1".

## Verification Plan

### Automated Tests
- I will verify the build succeeds after adding dependencies.
- I will add a simple unit test for the `UsageDao` to verify that saving and fetching works.

### Manual Verification
- We will update the `BrainrotTrackerService` logs to show the "Database Updated" message with the new total count.
