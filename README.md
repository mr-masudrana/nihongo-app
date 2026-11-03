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

## ডেটার ফোল্ডার (`app/src/main/assets/`)
```
hiragana.json                    ১০৪টি (মূল ৪৬ + দাকুতেন ২৫ + যৌগিক ৩৩)
katakana.json                    ১০৪টি
n5/vocabulary/lesson_01.json     লেসন ১–২৫-এর শব্দ
n5/grammar/lesson_01.json        লেসন ১–২৫-এর ব্যাকরণ
n5/kanji.json                    ১২০টি কাঞ্জি (লেসন ছাড়া)
n4/vocabulary/lesson_26.json     লেসন ২৬–৫০-এর শব্দ
n4/grammar/lesson_26.json        লেসন ২৬–৫০-এর ব্যাকরণ
n4/kanji.json                    N4 কাঞ্জি
```
- শব্দ ও ব্যাকরণের ফাইল আলাদা, তাই একটি লেসনে শুধু একটি থাকলেও চলে (অন্যটি তালিকায় ধূসর দেখায়)
- কানা ডেটাবেজ (Room) শুধু প্রথমবার `hiragana.json` + `katakana.json` থেকে সিড হয়। কানা JSON বদলালে `AppDatabase`-এর version বাড়ান (ডেভেলপমেন্টে destructive migration চালু আছে)।
- শব্দ, ব্যাকরণ ও কাঞ্জি Room ব্যবহার করে না — ফাইল বদলালেই হয়, ভার্সন বাড়াতে হয় না

## কুইজ (মাল্টিপল চয়েস) — কানা ও শব্দের জন্য আলাদা
- কানা কুইজ: হিরাগানা / কাতাকানা / দুটোই; কানা → রোমাজি অথবা রোমাজি → কানা
- শব্দ কুইজ: লেসন বেছে নেওয়া যায়; জাপানি → বাংলা অথবা বাংলা → জাপানি
- প্রতি কুইজে ১০টি প্রশ্ন, ৪টি অপশন। ভুল অপশন একই স্ক্রিপ্ট/বিভাগ থেকে আসে; প্রায় একই অর্থের অপশন আটকানো হয়। উত্তরের পর উচ্চারণ বাজে।

## XP ও স্ট্রিক (domain/Gamification.kt, data/StatsRepository.kt)
- কুইজে সঠিক উত্তর +১০ XP; সব ঠিক হলে +২০ বোনাস
- দিনে অন্তত একটি কুইজ দিলে স্ট্রিক বাড়ে; একদিন বাদ গেলে স্ট্রিক ০ থেকে শুরু
- প্রতি ১০০ XP-তে ১ লেভেল; দৈনিক লক্ষ্য ৫০ XP
- XP/স্ট্রিক SharedPreferences-এ থাকে, তাই Room ডেটাবেজ রিসেট হলেও মুছে যায় না

## ডিজাইন (UI) — নতুন
- `ui/theme/Theme.kt`: ইন্ডিগো + সাকুরা + মাচা রঙের থিম; লাইট ও ডার্ক মোড (সিস্টেম অনুযায়ী অটো); বাংলা অক্ষরের জন্য ঠিক করা টাইপোগ্রাফি
- `ui/components/`: শেয়ার্ড কম্পোনেন্ট (SoftCard, SegmentedTabs, ChipRow, ProgressRing, ActionButton…)
- পুরো অ্যাপে একটাই `TtsHelper` (`LocalTts`) — স্ক্রিন বদলালে আর নতুন করে TTS চালু হয় না
- edge-to-edge, ট্যাব বদলানোর ক্রসফেড, ব্যাক চাপলে প্রথম ট্যাবে ফেরা, হ্যাপটিক ফিডব্যাক

## টুলচেইন
AGP 8.5.2 • Kotlin 2.0.20 • KSP 2.0.20-1.0.25 • Gradle 8.9 • JDK 17 • compileSdk 34 • minSdk 24

## সেটিংস — নতুন
- প্রগ্রেস ট্যাবের উপরে ডানে ⚙️ বাটন → সেটিংস স্ক্রিন (`ui/settings/`)
- দৈনিক XP লক্ষ্য (২০/৩০/৫০/১০০/১৫০), থিম (অটো/লাইট/ডার্ক), উচ্চারণের গতি (ধীর/স্বাভাবিক/দ্রুত)
- সেটিংস `data/SettingsRepository.kt`-এ SharedPreferences-এ থাকে

## নেভিগেশন — নিচে ৩টি ট্যাব
- **Home** (`ui/home`): শুভেচ্ছা, আজকের লক্ষ্যের হিরো কার্ড (XP রিং, লেভেল, স্ট্রিক) ও তিনটি সেকশন
  - **Basic**: Hiragana, Katakana, Counting (সংখ্যা + ～つ/～人/～個), Test (কানা কুইজ)
  - **N5**: Vocabulary, Kanji, Grammar, Test (শব্দ কুইজ) — লেসন ১–২৫
  - **N4**: Vocabulary, Kanji, Grammar, Test (শব্দ কুইজ) — লেসন ২৬–৫০
- **Search** (`ui/search`): সব লেসনের শব্দে খোঁজা (জাপানি / romaji / বাংলা / English)
- **Settings** (`ui/settings`): অগ্রগতি কার্ড (লেভেল, মোট XP, সর্বোচ্চ স্ট্রিক, কুইজ সংখ্যা), দৈনিক লক্ষ্য, থিম, উচ্চারণের গতি
- Home-এর ভেতরে ব্যাক চাপলে এক ধাপ উপরে যায়; নিচে Home আবার চাপলে হোমের মূল পাতায় ফেরে

## লেসন-ভিত্তিক কাঠামো
- Home → লেভেল (N5 / N4) → বিষয় (Vocabulary / Kanji / Grammar) → **লেসন তালিকা** → লেসনের কনটেন্ট
- N5 = লেসন ১–২৫, N4 = লেসন ২৬–৫০ (`ContentRepository.lessonRange`)। ডেটা না থাকা লেসন তালিকায় ধূসর দেখায়
- Hiragana ও Katakana আলাদা চার্ট (ট্যাপ করলে উচ্চারণ), Counting স্ট্যাটিক তালিকা

### শব্দের ফাইল — `n5/vocabulary/lesson_01.json`
```json
{
  "lesson": 1,
  "title": "লেসনের শিরোনাম",
  "words": [
    { "word": "先生", "reading": "せんせい", "romaji": "sensei", "bn": "শিক্ষক", "en": "teacher",
      "note": "ঐচ্ছিক — ব্যবহারের টীকা" }
  ]
}
```
`word` = কাঞ্জি/লেখার রূপ (কাঞ্জি না থাকলে কানাই দিন), `reading` = কানা পড়া। `note` ঐচ্ছিক।

### ব্যাকরণের ফাইল — `n5/grammar/lesson_01.json`
```json
{
  "lesson": 1,
  "title": "লেসনের শিরোনাম",
  "points": [
    { "title": "১. N₁ は N₂ です", "pattern": "N₁ は N₂ です",
      "explain": "ব্যাখ্যা (একাধিক লাইনের জন্য \\n ব্যবহার করুন)",
      "note": "ঐচ্ছিক — [Note]-এর মতো",
      "examples": [ { "jp": "", "romaji": "", "bn": "" } ] }
  ]
}
```
নমুনা ডেটা (`lesson_01–03`, `lesson_26–27`) শুধু কাঠামো দেখানোর জন্য — নিজের ডেটা দিয়ে বদলে নিন।

### কাঞ্জির ডেটা
`assets/n5/kanji.json` (N5, ১২০টি) ও `assets/n4/kanji.json` (নমুনা) — একটি JSON অ্যারে, তালিকার ক্রমেই গ্রিডে দেখায়:
```json
{ "kanji": "人", "strokes": 2, "on": "ジン・ニン", "kun": "ひと", "bn": "মানুষ", "en": "person",
  "words": [ { "w": "日本人", "r": "にほんじん", "bn": "জাপানি মানুষ" } ],
  "sentence": { "jp": "…", "bn": "…" } }
```
`words` ও `sentence` ঐচ্ছিক। পড়া, শব্দ ও বাক্য আমার নিজের লেখা — প্রকাশের আগে একবার মিলিয়ে নিন।

- ট্যাপ করলে উচ্চারণ বাজে। N5/N4 Test ও Search এই লেসনের শব্দ থেকেই চলে (Test শুধু নিজের লেভেলের শব্দ নেয়)। কাঞ্জি ও ব্যাকরণের টেস্ট এখনো নেই।

## মুছে ফেলা হয়েছে
- ফ্ল্যাশকার্ড, SRS (`Srs.kt`), পুরনো ২৪০ শব্দ (`vocab_n5.json`, `VocabEntity`) — Room ডেটাবেজ ভার্সন ৪ (শুধু `kana` টেবিল; ইনস্টল করা অ্যাপে পুরনো ডেটাবেজ নতুন করে তৈরি হবে)
- XP এখন শুধু কুইজ থেকে আসে, তাই স্ট্রিকও কুইজ দিয়ে বাড়ে
