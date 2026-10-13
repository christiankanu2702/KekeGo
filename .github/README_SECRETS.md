# GitHub Actions Secrets Setup for KekeGo

To activate automated deployment to **Firebase Storage** and **Firebase App Distribution**, add the following Secrets to your GitHub repository:

Go to: **GitHub Repo ➔ Settings ➔ Secrets and variables ➔ Actions ➔ New repository secret**

---

### Required Secrets for Firebase Deployment:

1. `GCP_SA_KEY`:
   - The JSON content of your Google Cloud / Firebase Service Account key.
   - Roles needed:
     * *Storage Admin* (for `gs://gen-lang-client-0639805966.appspot.com/builds/`)
     * *Firebase App Distribution Admin*
   - How to generate: Google Cloud Console ➔ IAM & Admin ➔ Service Accounts ➔ Create Key (JSON).

2. `FIREBASE_APP_ID`:
   - Your Firebase Android App ID.
   - Value: `1:715487607162:android:87babc36870e85381cbec3`

---

### Optional Secrets for Custom Production Keystore Signing:

3. `KEYSTORE_BASE64`:
   - The base64 string of your release `.jks` or `.keystore` file (`base64 -w 0 upload.jks`).

4. `STORE_PASSWORD`:
   - Password for your release keystore.

5. `KEY_PASSWORD`:
   - Password for your keystore key alias.

*(Note: If keystore secrets are left empty, GitHub Actions gracefully compiles a universal, installable APK signed for testing so the build never fails).*
