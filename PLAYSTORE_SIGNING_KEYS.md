# 🔐 Google Play Store Signing Keys & Keystore Info

## 🔑 Keystore Credentials
- **Keystore File:** `release-upload.jks` (Project Root)
- **Keystore Password:** `12345678`
- **Key Alias:** `upload`
- **Key Password:** `12345678`
- **Config File:** `keystore.properties`
- **Public PEM File:** `upload_certificate.pem`

---

## 📜 Certificate & Fingerprint Details
- **Owner / Issuer:** `CN=Hanu Babbar, OU=roguecodes, O=roguecodes, L=Mohali, ST=Punjab, C=IN`
- **Validity:** 20 Aug 2026 – 05 Jan 2054
- **SHA-1 Fingerprint:** `97:78:0A:77:F6:4F:49:9D:23:FA:87:D0:F7:32:0C:EE:78:81:12:1A`
- **SHA-256 Fingerprint:** `F2:FE:E3:8A:CD:0B:42:6C:75:7A:B7:E9:86:AB:73:84:E9:0C:54:5A:EE:22:E5:2C:50:00:7B:18:DC:F5:11:AB`

---

## 🛠️ Build & Verification Commands

### 1. View Keystore Details Locally
```powershell
keytool -list -v -keystore release-upload.jks -alias upload -storepass 12345678
```

### 2. Export PEM Certificate (If needed by Google Play Support)
```powershell
keytool -export -rfc -keystore release-upload.jks -alias upload -file upload_certificate.pem -storepass 12345678
```

### 3. Build Signed Release Bundle (.aab)
```powershell
.\gradlew.bat bundleRelease
```

The generated AAB is placed at:
`app/build/outputs/bundle/release/app-release.aab`
