# Compiling KekeGo iOS with EAS (Expo Application Services)

The `ios-kekego-rn` project is configured for **EAS Build** to generate `.ipa` (physical iPhone device/TestFlight) or simulator builds.

---

### Quick Commands (Inside `ios-kekego-rn/`):

1. **Install EAS CLI** (if not already installed):
   ```bash
   npm install -g eas-cli
   ```

2. **Log in to your Expo account**:
   ```bash
   eas login
   ```

3. **Configure Project ID** (one-time setup):
   ```bash
   eas project:init
   ```
   *(This automatically populates the `projectId` in `app.json`).*

4. **Build for iOS**:

   - **For Internal Ad-Hoc / TestFlight Distribution (Physical iPhone)**:
     ```bash
     npm run build:ios:preview
     # or: eas build --platform ios --profile preview
     ```

   - **For iOS Simulator (`.tar.gz`)**:
     ```bash
     npm run build:ios:sim
     # or: eas build --platform ios --profile development
     ```

   - **For Apple App Store Production**:
     ```bash
     npm run build:ios:prod
     # or: eas build --platform ios --profile production
     ```

---

### What Has Been Pre-Configured:
- **`eas.json`**: Configured with `development`, `preview`, and `production` build profiles with `autoIncrement` enabled for iOS build numbers.
- **`app.json`**:
  * Bundle Identifier: `com.aistudio.kekego.ios`
  * Deployment Target: iOS 15.1+
  * Location permissions and `ITSAppUsesNonExemptEncryption = false` configured for Apple App Store compliance.
  * Static CocoaPods frameworks configured via `expo-build-properties`.
