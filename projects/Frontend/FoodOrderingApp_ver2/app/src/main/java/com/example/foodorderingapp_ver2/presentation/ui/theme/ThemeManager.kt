// presentation/ui/theme/ThemeManager.kt
package com.example.foodorderingapp_ver2.presentation.ui.theme

import androidx.compose.ui.graphics.Color

object ThemeManager {

    data class AppTheme(
        val primaryColor: Color,
        val backgroundColor: Color,
        val cardColor: Color,
        val successColor: Color,
        val textOnPrimary: Color = Color.White,
        val textColor: Color = Color.Black,
        val companyName: String
    )

    private val orangeTheme = AppTheme(
        primaryColor = Color(0xFFFF8C42),
        backgroundColor = Color(0xFFFFC680),
        cardColor = Color(0xFFFF8C42),
        successColor = Color(0xFF4CAF50),
        textColor = Color(0xFF353836),
        companyName = "Company A"
    )

    private val greenTheme = AppTheme(
        primaryColor = Color(0xFF4CAF50),
        backgroundColor = Color(0xFFC8E6C9),
        cardColor = Color(0xFF66BB6A),
        successColor = Color(0xFF2E7D32),
        textColor = Color(0xFF083A19),
        companyName = "Company B"
    )

    private val purpleTheme = AppTheme(
        primaryColor = Color(0xFF8958B9),
        backgroundColor = Color(0xA9D0B5FF),
        cardColor = Color(0xFFB161E4),
        successColor = Color(0xFF411C81),
        textColor = Color(0xFF353836),
        companyName = "Company C"
    )

    fun getCurrentTheme(): AppTheme {
        return try {
            // Try to get the build config
            val companyTheme = getCompanyTheme()
            when (companyTheme) {
                "ORANGE" -> orangeTheme
                "GREEN" -> greenTheme
                "PURPLE" -> purpleTheme
                else -> orangeTheme // Default fallback
            }
        } catch (e: Exception) {
            // If BuildConfig fails, return default
            orangeTheme
        }
    }

    private fun getCompanyTheme(): String {
        return try {
            com.example.foodorderingapp_ver2.BuildConfig.COMPANY_THEME
        } catch (e: Exception) {
            "ORANGE" // Default
        }
    }
}