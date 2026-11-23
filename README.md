# Nihongo (JFT-Basic · Irodori Edition) — জাপানিজ শেখার অ্যাপ

এই অ্যাপটি জাপান ফাউন্ডেশনের **Irodori: Japanese for Life in Japan (Starter A1, Elementary 1 A2, Elementary 2 A2)** বই এবং **JFT-Basic** পরীক্ষার সিলেবাস অনুযায়ী সাজানো হয়েছে।

## GitHub Actions দিয়ে বিল্ড
1. GitHub-এ নতুন repo বানান।
2. এই ফোল্ডারের সব ফাইল (`.github` ও `.gitignore` সহ) push করুন:
   ```bash
   git init && git add . && git commit -m "Irodori JFT-Basic edition"
   git branch -M main
   git remote add origin https://github.com/<user>/<repo>.git
   git push -u origin main
   ```
3. repo-র **Actions** ট্যাবে "Build APK" চলবে। শেষ হলে **Artifacts → nihongo-debug-apk** ডাউনলোড করে ইনস্টল করুন।

## ডেটার ফোল্ডার (`app/src/main/assets/`)
```
number.json                                  সংখ্যা (Basic → Number)
sentences.json                               দৈনন্দিন বাক্য (Basic → Sentences)
hiragana.json                                ১০৪টি হিরাগানা
katakana.json                                ১০৪টি কাতাকানা
starter_a1/vocabulary/lesson_01..18.json     Irodori Starter A1 (লেসন ১–১৮) শব্দ
starter_a1/grammar/lesson_01..18.json        Irodori Starter A1 (লেসন ১–১৮) ব্যাকরণ
starter_a1/conversation/lesson_01..18.json   Irodori Starter A1 (লেসন ১–১৮) কথোপকথন
starter_a1/kanji_starter_a1.json             Starter A1 কাঞ্জি
elementary1_a2/...                           Irodori Elementary 1 (A2) লেসন ১–১৮ ও কাঞ্জি
elementary2_a2/...                           Irodori Elementary 2 (A2) লেসন ১–১৮ ও কাঞ্জি
```
- প্রতিটি বইয়ের ১৮টি লেসনের JSON টেমপ্লেট ও মৌলিক কন্টেন্ট তৈরি করা আছে। আপনি চাইলে আরও শব্দ, ব্যাকরণ বা ডায়ালগ সরাসরি সংশ্লিষ্ট `lesson_NN.json` ফাইলে যুক্ত করতে পারবেন।
