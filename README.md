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
number.json                      সংখ্যা (Basic → Number)
sentences.json                   দৈনন্দিন বাক্য (Basic → Sentences)
hiragana.json                    ১০৪টি (মূল ৪৬ + দাকুতেন ২৫ + যৌগিক ৩৩)
katakana.json                    ১০৪টি
n5/vocabulary/lesson_01.json     লেসন ১–২৫-এর শব্দ
n5/grammar/lesson_01.json        লেসন ১–২৫-এর ব্যাকরণ
n5/conversation/lesson_01.json   লেসন ১–২৫-এর কথোপকথন
n5/kanji_n5.json                 ১২০টি কাঞ্জি (লেসন ছাড়া)
n4/vocabulary/lesson_26.json     লেসন ২৬–৫০-এর শব্দ
n4/grammar/lesson_26.json        লেসন ২৬–৫০-এর ব্যাকরণ
n4/conversation/lesson_26.json   লেসন ২৬–৫০-এর কথোপকথন
n4/kanji_n4.json                 N4 কাঞ্জি
```
- শব্দ ও ব্যাকরণের ফাইল আলাদা, তাই একটি লেসনে শুধু একটি থাকলেও চলে (অন্যটি তালিকায় ধূসর দেখায়)
- কানা ডেটাবেজ (Room) শুধু প্রথমবার `hiragana.json` + `katakana.json` থেকে সিড হয়। কানা JSON বদলালে `AppDatabase`-এর version বাড়ান (ডেভেলপমেন্টে destructive migration চালু আছে)।
- শব্দ, ব্যাকরণ ও কাঞ্জি Room ব্যবহার করে না — ফাইল বদলালেই হয়, ভার্সন বাড়াতে হয় না

## কুইজ (মাল্টিপল চয়েস) — কানা ও শব্দের জন্য আলাদা
- কানা কুইজ: হিরাগানা / কাতাকানা / দুটোই; কানা → রোমাজি অথবা রোমাজি → কানা
- শব্দ কুইজ: লেসন বেছে নেওয়া যায়; জাপানি → বাংলা অথবা বাংলা → জাপানি
- প্রতি কুইজে ১০টি প্রশ্ন, ৪টি অপশন। ভুল অপশন একই স্ক্রিপ্ট/বিভাগ থেকে আসে; প্রায় একই অর্থের অপশন আটকানো হয়। উত্তরের পর উচ্চারণ বাজে।

## কুইজ, স্ট্রিক ও হাই স্কোর (domain/Gamification.kt, data/StatsRepository.kt)
- কুইজ আনলিমিটেড: অসীম প্রশ্ন, প্রথম ভুলে বা মাঝপথে বের হলে রান শেষ। শুরু হলেই স্টপওয়াচ চলে ও প্রশ্ন গোনা হয়; ফলাফলে মোট সময় ও প্রশ্ন সংখ্যা দেখায়। XP/লেভেল নেই
- হাই স্কোর = টানা সঠিক উত্তর; প্রতিটি সেকশনের (কানা, N5, N4) আলাদা। ভুলে রান শেষ হলেই সেভ হয়। দেখা যায় কুইজ সেটআপে ও প্রোফাইল পাতায়
- দিনে অন্তত একটি কুইজ দিলে স্ট্রিক বাড়ে; একদিন বাদ গেলে স্ট্রিক ০ থেকে শুরু
- স্ট্রিক ও হাই স্কোর SharedPreferences-এ থাকে, তাই Room ডেটাবেজ রিসেট হলেও মুছে যায় না

## ডিজাইন (UI) — নতুন
- `ui/theme/Theme.kt`: ইন্ডিগো + সাকুরা + মাচা রঙের থিম; লাইট ও ডার্ক মোড (সিস্টেম অনুযায়ী অটো); বাংলা অক্ষরের জন্য ঠিক করা টাইপোগ্রাফি
- `ui/components/`: শেয়ার্ড কম্পোনেন্ট (SoftCard, SegmentedTabs, ChipRow, ProgressRing, ActionButton…)
- পুরো অ্যাপে একটাই `TtsHelper` (`LocalTts`) — স্ক্রিন বদলালে আর নতুন করে TTS চালু হয় না
- edge-to-edge, ট্যাব বদলানোর ক্রসফেড, ব্যাক চাপলে প্রথম ট্যাবে ফেরা, হ্যাপটিক ফিডব্যাক

## টুলচেইন
AGP 8.5.2 • Kotlin 2.0.20 • KSP 2.0.20-1.0.25 • Gradle 8.9 • JDK 17 • compileSdk 34 • minSdk 24

## সেটিংস — নতুন
- প্রগ্রেস ট্যাবের উপরে ডানে ⚙️ বাটন → সেটিংস স্ক্রিন (`ui/settings/`)
- থিম (অটো/লাইট/ডার্ক), উচ্চারণের গতি (ধীর/স্বাভাবিক/দ্রুত)
- সেটিংস `data/SettingsRepository.kt`-এ SharedPreferences-এ থাকে

## নেভিগেশন — নিচে ৩টি ট্যাব
- **Home** (`ui/home`): শুভেচ্ছা, স্ট্রিক ও তিনটি সেকশন (Basic, N5, N4)
  - **Basic**: Hiragana, Katakana, Sentences (দৈনন্দিন বাক্য — `assets/sentences.json`), Number (সংখ্যা ০–১০ কোটি, দশমিক, ভগ্নাংশ — `assets/number.json` থেকে), Test (কানা কুইজ)
  - **N5**: Vocabulary, Kanji, Grammar, Test (শব্দ কুইজ) — লেসন ১–২৫
  - **N4**: Vocabulary, Kanji, Grammar, Test (শব্দ কুইজ) — লেসন ২৬–৫০
- **Search** (`ui/search`): সব লেসনের শব্দে খোঁজা (জাপানি / romaji / বাংলা / English)
- **Settings** (`ui/settings`): থিম, উচ্চারণের গতি
- Home-এর ভেতরে ব্যাক চাপলে এক ধাপ উপরে যায়; নিচে Home আবার চাপলে হোমের মূল পাতায় ফেরে

## লেসন-ভিত্তিক কাঠামো
- Home → লেভেল (N5 / N4) → বিষয় (Vocabulary / Kanji / Grammar) → **লেসন তালিকা** → লেসনের কনটেন্ট
- N5 = লেসন ১–২৫, N4 = লেসন ২৬–৫০ (`ContentRepository.lessonRange`)। ডেটা না থাকা লেসন তালিকায় ধূসর দেখায়
- Hiragana ও Katakana আলাদা চার্ট (ট্যাপ করলে উচ্চারণ), Number `assets/number.json` থেকে লোড হয়

### ভাষা ও অডিও (সেটিংস)
- **ভাষা**: সিস্টেম / বাংলা / English। সিস্টেম = ফোন বাংলা হলে বাংলা, নইলে ইংরেজি। UI-র লেখা `tr("বাংলা", "English")` দিয়ে বাছা হয় (`ui/components/Lang.kt`); ইংরেজি খালি থাকলে বাংলায় ফিরে যায়।
- কন্টেন্টের ইংরেজি JSON-এ ঐচ্ছিক ঘরে থাকে: `title_en`, `explain_en`, `pattern_en`, `note_en`, উদাহরণ/লাইন/বাক্যে `en`, কথোপকথনের `title_en`।
- **অডিও**: একটি সুইচ — বন্ধ থাকলে সব স্পিকার বাটন ও কুইজের স্বয়ংক্রিয় উচ্চারণ নীরব।

### শব্দ কার্ডের অংশ দেখানো/লুকানো
সেটিংস → "শব্দ কার্ডে কী দেখাবেন": কানা পড়া সবসময় মূল অংশ (বন্ধ করা যায় না); অ্যাপের ভাষার অর্থও সবসময় চালু (বাংলা UI-তে বাংলা অর্থ, ইংরেজিতে ইংরেজি)। কাঞ্জি/লেখা, রোমাজি ও অন্য ভাষার অর্থ ঐচ্ছিক। সেভ হয় SharedPreferences-এ (`show_WORD`, `show_READING`, …)।

### কথোপকথনের ফাইল — `n5/conversation/lesson_01.json`
```json
{ "lesson": 1, "title": "নিজের পরিচয়",
  "dialogues": [ { "title": "প্রথম পরিচয়",
    "lines": [ { "speaker": "A", "jp": "はじめまして。", "romaji": "Hajimemashite.", "bn": "প্রথম দেখা হলো।" } ] } ] }
```
নতুন লেসন যোগ করতে শুধু `lesson_NN.json` ফাইল রাখুন — কোড বদলাতে হবে না।

### শব্দের ফাইল — `n5/vocabulary/lesson_01.json`
(বর্তমানে N5 লেসন ১–৪ ও N4 লেসন ২৬–২৮ — Minna no Nihongo-র শব্দতালিকা থেকে; `note` ঘরে ক্রিয়ার গ্রুপ (I/II/III) ও ব্যবহারের টীকা আছে)
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
`assets/n5/kanji_n5.json` (N5, ১২০টি) ও `assets/n4/kanji_n4.json` (নমুনা) — একটি JSON অ্যারে, তালিকার ক্রমেই গ্রিডে দেখায়:
```json
{ "kanji": "人", "strokes": 2, "on": "ジン・ニン", "kun": "ひと", "bn": "মানুষ", "en": "person",
  "words": [ { "w": "日本人", "r": "にほんじん", "bn": "জাপানি মানুষ" } ],
  "sentence": { "jp": "…", "bn": "…" } }
```
`words` ও `sentence` ঐচ্ছিক। পড়া, শব্দ ও বাক্য আমার নিজের লেখা — প্রকাশের আগে একবার মিলিয়ে নিন।

- ট্যাপ করলে উচ্চারণ বাজে। N5/N4 Test ও Search এই লেসনের শব্দ থেকেই চলে (Test শুধু নিজের লেভেলের শব্দ নেয়)। কাঞ্জি ও ব্যাকরণের টেস্ট এখনো নেই।

## মুছে ফেলা হয়েছে
- ফ্ল্যাশকার্ড, SRS (`Srs.kt`), পুরনো ২৪০ শব্দ (`vocab_n5.json`, `VocabEntity`) — Room ডেটাবেজ ভার্সন ৪ (শুধু `kana` টেবিল; ইনস্টল করা অ্যাপে পুরনো ডেটাবেজ নতুন করে তৈরি হবে)
- স্ট্রিক বাড়ে কুইজ শুরু করলে

## প্রোফাইল ও সেটিংস — নতুন
- তৃতীয় ট্যাব এখন **Profile** (`ui/profile/ProfileScreen.kt`): প্রোফাইল ছবি, নাম, স্ট্রিক/কুইজ সংখ্যা ও তিন সেকশনের হাই স্কোর
- ছবি ট্যাপ করলে গ্যালারি (Photo Picker, কোনো পারমিশন লাগে না); ছবি ৫১২px বর্গাকার করে `filesDir/profile.jpg`-এ রাখা হয়
- নামের পাশে ✎ আইকন চাপলে নাম বদলানো যায় (সর্বোচ্চ ২৪ অক্ষর); নাম ও ছবির সংস্করণ `SettingsRepository`-তে থাকে
- প্রোফাইলের উপরে ডানের গিয়ার আইকন → সেটিংস পেজ (দৈনিক লক্ষ্য, থিম, উচ্চারণের গতি); ব্যাক চাপলে প্রোফাইলে ফেরে
- সব ইমোজি আইকনে বদলানো হয়েছে — `ui/components/AppIcons.kt` (ভেক্টর পাথ), ব্যবহার: `AppIcon(AppIcons.Fire, size = 20.dp)`
