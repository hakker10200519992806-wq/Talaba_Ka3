# CppUz — Kamola uchun C++

C++ o'rganish ilovasi (mushukcha bilan, offline ishlaydi). Android WebView ilova.

## APK olish (GitHub Actions)

1. Repo → **Settings → Secrets and variables → Actions → New repository secret**
   - Name: `TG_BOT_TOKEN`
   - Value: Telegram bot tokeningiz (BotFather'dan)
2. Repo → **Actions → Build APK → Run workflow** (yoki har `push`da avtomatik ishlaydi)
3. Ish tugagach, run sahifasi pastidagi **Artifacts → CppUz-Kamola-apk** ni yuklab oling, ichidan `.apk` chiqadi.
4. Telefonda APK'ni oching va "noma'lum manbalardan o'rnatish"ga ruxsat bering.

## Tuzilma

- `app/src/main/assets/` — ilovaning veb qismi (`index.html`, ikonkalar)
- `app/src/main/java/.../MainActivity.java` — WebView + bildirishnomalar
- `.github/workflows/build-apk.yml` — avtomatik APK build

> Bot tokenni hech qachon `index.html` yoki kodga yozmang — faqat GitHub Secret orqali.
