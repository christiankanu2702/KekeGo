# Automated GitHub Actions: Dual Deployment to Firebase Storage & Firebase App Distribution

Build, version, and dual-deploy the KekeGo Android APK to both **Firebase Cloud Storage Bucket** (for direct public/private URL downloads) and **Firebase App Distribution** (for over-the-air tester installs), with dynamic build numbering and GitHub release artifacts.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The workflow includes both Firebase endpoints:
> 1. **Dynamic Version & Build Numbering**:
>    - `versionCode = System.getenv("BUILD_NUMBER")?.toIntOrNull() ?: 1`
>    - `versionName = System.getenv("APP_VERSION_NAME") ?: "1.0.0"`
>    - Dynamically incremented with each GitHub Actions execution `${{ github.run_number }}`.
> 2. **Dual Firebase Deployment**:
>    - **Firebase Storage (`gs://gen-lang-client-0639805966.appspot.com/builds/`)**:
>      * Automatically uploads `kekego-v1.0.<build_number>.apk` to the storage bucket using `google-github-actions/upload-cloud-storage`.
>      * Generates a permanent download link for users and fleet operators.
>    - **Firebase App Distribution**:
>      * Automatically pushes the APK to Firebase App Distribution with release notes specifying the build number, tester group notifications, and invite links.
>    - **GitHub Artifacts**:
>      * Retains the raw APK artifact on GitHub for 30 days.
> 3. **Non-Disruptive Local Environment**:
>    - Local compilation (`compile_applet`) remains clean with safe fallback defaults.

---

## 1. Automated Pipeline Architecture

```
GitHub Push / Manual Trigger
  │
  ▼
[ 1. Setup Java 17 & Android SDK ]
  │
  ▼
[ 2. Tests & Lint Quality Check ]
  │
  ▼
[ 3. Inject Version Code & Build Number ]
     BUILD_NUMBER: ${{ github.run_number }}
     APP_VERSION_NAME: 1.0.${{ github.run_number }}
  │
  ▼
[ 4. Assemble Release APK (gradle assembleRelease) ]
  │
  ├───────────────────────┬────────────────────────┬───────────────────────┐
  ▼                       ▼                        ▼                       ▼
[ Firebase Storage ]    [ App Distribution ]     [ GitHub Artifacts ]    [ Telegram / Log ]
gs://.../builds/*.apk   testers: "testers,fleet"  direct zip download     build success link
```
