package com.example.nihongo.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/** অ্যাপ এখন ইংরেজিতে দেখানো হচ্ছে কি না (MainActivity সেট করে) */
val LocalEnglish = staticCompositionLocalOf { false }

/**
 * ভাষা অনুযায়ী লেখা বাছে। ইংরেজি মোডে en খালি থাকলে বাংলাতেই ফিরে যায়,
 * তাই কোনো কন্টেন্টে ইংরেজি না থাকলেও কিছু ফাঁকা দেখায় না।
 */
@Composable
@ReadOnlyComposable
fun tr(bn: String, en: String): String = if (LocalEnglish.current && en.isNotBlank()) en else bn
