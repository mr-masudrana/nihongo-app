# Nihongo — জাপানিজ শেখার অ্যাপ (Kotlin + Jetpack Compose)

## GitHub Actions দিয়ে বিল্ড
1. GitHub-এ নতুন repo বানান।
2. এই ফোল্ডারের সব ফাইল (`.github` ও `.gitignore` সহ) push করুন:
   git init && git add . && git commit -m "init"
   git branch -M main
   git remote add origin https://github.com/<user>/<repo>.git
   git push -u origin main
3. repo-র **Actions** ট্যাবে "Build APK" চলবে (হাতে চালাতে: Run workflow)।
4. শেষ হলে রান পেজের নিচে **Artifacts → nihongo-debug-apk** ডাউনলোড করে APK ইনস্টল করুন।

## (ঐচ্ছিক) সাইন করা release APK
repo → Settings → Secrets and variables → Actions-এ এই চারটি যোগ করুন:
KEYSTORE_BASE64 (keystore ফাইলের base64), KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD
→ তখন `nihongo-release-apk` আর্টিফ্যাক্টও তৈরি হবে।
keystore বানাতে: keytool -genkey -v -keystore release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias nihongo
base64 করতে (Linux/Mac): base64 -w0 release.jks

## ডেটা
- assets/kana.json: ২০৮টি কানা (মূল ৪৬ + দাকুতেন ২৫ + যৌগিক ৩৩, হিরাগানা ও কাতাকানা প্রতিটিতে)
- assets/vocab_n5.json: ২৪০টি প্রাথমিক N5 স্তরের শব্দ (১৩ বিভাগ)
- ডেটাবেজ শুধু প্রথমবার সিড হয়। JSON বদলালে `AppDatabase`-এর version বাড়ান (ডেভেলপমেন্টে destructive migration চালু আছে)।

## SRS (domain/Srs.kt)
"জানি" → লেভেল +১ (১, ২, ৪, ৭, ১৫, ৩০, ৬০ দিন); "জানি না" → ১০ মিনিট পর, ওই সেশনেই আবার।
সেশনে সর্বোচ্চ ২০ রিভিউ + ১০ নতুন কার্ড।

## কুইজ (মাল্টিপল চয়েস) — কানা ও শব্দের জন্য আলাদা
- কানা কুইজ: হিরাগানা / কাতাকানা / দুটোই; কানা → রোমাজি অথবা রোমাজি → কানা
- শব্দ কুইজ: বিভাগ বেছে নেওয়া যায়; জাপানি → বাংলা অথবা বাংলা → জাপানি
- প্রতি কুইজে ১০টি প্রশ্ন, ৪টি অপশন। ভুল অপশন একই স্ক্রিপ্ট/বিভাগ থেকে আসে; প্রায় একই অর্থের অপশন আটকানো হয়। উত্তরের পর উচ্চারণ বাজে।

## XP ও স্ট্রিক (domain/Gamification.kt, data/StatsRepository.kt)
- কুইজে সঠিক উত্তর +১০ XP; সব ঠিক হলে +২০ বোনাস
- ফ্ল্যাশকার্ডে "জানি" +৫, "জানি না" +১ (চেষ্টাও স্ট্রিক গণ্য)
- দিনে যেকোনো একটি অ্যাক্টিভিটিতে স্ট্রিক বাড়ে; একদিন বাদ গেলে স্ট্রিক ০ থেকে শুরু
- প্রতি ১০০ XP-তে ১ লেভেল; দৈনিক লক্ষ্য ৫০ XP
- XP/স্ট্রিক SharedPreferences-এ থাকে, তাই Room ডেটাবেজ রিসেট হলেও মুছে যায় না

## টুলচেইন
AGP 8.5.2 • Kotlin 2.0.20 • KSP 2.0.20-1.0.25 • Gradle 8.9 • JDK 17 • compileSdk 34 • minSdk 24
