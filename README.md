# Nihongo (JFT-Basic · Irodori Edition) — জাপানিজ শেখার অ্যাপ

এই অ্যাপটি জাপান ফাউন্ডেশনের **Irodori: Japanese for Life in Japan (Starter A1, Elementary 1 A2, Elementary 2 A2)** বই এবং **JFT-Basic** পরীক্ষার সিলেবাস অনুযায়ী সাজানো হয়েছে।

## হোমপেজ সেকশনসমূহ
1. **BASIC**: Hiragana, Katakana, Number
2. **IRODORI · STARTER A1**: Vocabulary, Kanji, Grammar, Activities, Can-do, Tips of life in Japan (প্রতিটিতে ১–১৮টি লেসন পেজ)
3. **IRODORI · ELEMENTARY 1 (A2)**: Vocabulary, Kanji, Grammar, Activities, Can-do, Tips of life in Japan (প্রতিটিতে ১–১৮টি লেসন পেজ)
4. **IRODORI · ELEMENTARY 2 (A2)**: Vocabulary, Kanji, Grammar, Activities, Can-do, Tips of life in Japan (প্রতিটিতে ১–১৮টি লেসন পেজ)

## ডেটার ফোল্ডার (`app/src/main/assets/`)
```
hiragana.json                                হিরাগানা
katakana.json                                কাতাকানা
number.json                                  সংখ্যা
<level>/vocabulary/lesson_01..18.json        শব্দভান্ডার (Vocabulary)
<level>/kanji_<level>.json                   কাঞ্জি (Kanji)
<level>/grammar/lesson_01..18.json           ব্যাকরণ (Grammar)
<level>/activities/lesson_01..18.json        অ্যাক্টিভিটি ও কথোপকথন (Activities)
<level>/cando/lesson_01..18.json             Can-do লক্ষ্য ও অভিব্যক্তি (Can-do)
<level>/tips/lesson_01..18.json              জাপানের জীবনযাত্রার টিপস (Tips of life in Japan)
```
এখানে `<level>` হলো `starter_a1`, `elementary1_a2`, এবং `elementary2_a2`।
