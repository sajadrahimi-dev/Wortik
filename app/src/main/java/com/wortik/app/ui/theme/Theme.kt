package com.wortik.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import com.wortik.app.data.local.AppPalette
import com.wortik.app.data.local.ThemeMode
import com.wortik.app.domain.model.Gender

@Immutable
data class GenderColorPair(
    val container: Color,
    val onContainer: Color
)

@Immutable
data class HarmonizedGenderColors(
    val der: GenderColorPair,
    val die: GenderColorPair,
    val das: GenderColorPair
) {
    fun forGender(gender: Gender): GenderColorPair? = when (gender) {
        Gender.DER -> der
        Gender.DIE -> die
        Gender.DAS -> das
        Gender.NONE -> null
    }
}

val LocalGenderColors = staticCompositionLocalOf {
    HarmonizedGenderColors(
        der = GenderColorPair(Color(0xFFD6E4FF), Color(0xFF001B3E)),
        die = GenderColorPair(Color(0xFFFFD9DE), Color(0xFF3F0016)),
        das = GenderColorPair(Color(0xFFC8EED4), Color(0xFF002111))
    )
}

fun palettePreviewColor(palette: AppPalette): Color = when (palette) {
    AppPalette.BERLIN_BLUE -> Color(0xFF455E91)
    AppPalette.SCHWARZWALD -> Color(0xFF2E6B4E)
    AppPalette.BAVARIA_GOLD -> Color(0xFF9A6700)
    AppPalette.BERRY_ROSE -> Color(0xFF9C4159)
    AppPalette.LAVENDER -> Color(0xFF6750A4)
    AppPalette.MONOCHROME -> Color(0xFF44474E)
}

private fun lightSchemeFor(palette: AppPalette): ColorScheme = when (palette) {
    AppPalette.BERLIN_BLUE -> lightColorScheme(
        primary = Color(0xFF455E91),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD8E2FF),
        onPrimaryContainer = Color(0xFF001A41),
        secondary = Color(0xFF575E71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFDBE2F9),
        onSecondaryContainer = Color(0xFF141B2C),
        surface = Color(0xFFFAF9FD),
        onSurface = Color(0xFF1A1B20),
        surfaceVariant = Color(0xFFE1E2EC),
        onSurfaceVariant = Color(0xFF44474F)
    )
    AppPalette.SCHWARZWALD -> lightColorScheme(
        primary = Color(0xFF2B6A4D),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFB0F1CC),
        onPrimaryContainer = Color(0xFF002113),
        secondary = Color(0xFF4E6356),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFD0E8D7),
        onSecondaryContainer = Color(0xFF0C1F15),
        surface = Color(0xFFF6FBF6),
        onSurface = Color(0xFF181D19),
        surfaceVariant = Color(0xFFDCE5DD),
        onSurfaceVariant = Color(0xFF414942)
    )
    AppPalette.BAVARIA_GOLD -> lightColorScheme(
        primary = Color(0xFF8C5B00),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFDEAE),
        onPrimaryContainer = Color(0xFF2D1B00),
        secondary = Color(0xFF725B3E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFEDFB9),
        onSecondaryContainer = Color(0xFF281804),
        surface = Color(0xFFFFF8F2),
        onSurface = Color(0xFF201B14),
        surfaceVariant = Color(0xFFF0E0CF),
        onSurfaceVariant = Color(0xFF4F4539)
    )
    AppPalette.BERRY_ROSE -> lightColorScheme(
        primary = Color(0xFF98405A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFFFD9E1),
        onPrimaryContainer = Color(0xFF3E001A),
        secondary = Color(0xFF75565E),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFFFD9E1),
        onSecondaryContainer = Color(0xFF2B151B),
        surface = Color(0xFFFFF8F8),
        onSurface = Color(0xFF211A1C),
        surfaceVariant = Color(0xFFF2DDE1),
        onSurfaceVariant = Color(0xFF514346)
    )
    AppPalette.LAVENDER -> lightColorScheme(
        primary = Color(0xFF6750A4),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE8DEF8),
        onSecondaryContainer = Color(0xFF1D192B),
        surface = Color(0xFFFEF7FF),
        onSurface = Color(0xFF1D1B20),
        surfaceVariant = Color(0xFFE7E0EC),
        onSurfaceVariant = Color(0xFF49454F)
    )
    AppPalette.MONOCHROME -> lightColorScheme(
        primary = Color(0xFF2C2E33),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFE0E2E8),
        onPrimaryContainer = Color(0xFF121316),
        secondary = Color(0xFF5A5D66),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE6E7EC),
        onSecondaryContainer = Color(0xFF1A1B1E),
        surface = Color(0xFFFAFAFB),
        onSurface = Color(0xFF18191C),
        surfaceVariant = Color(0xFFE3E4E8),
        onSurfaceVariant = Color(0xFF46484E)
    )
}

private fun darkSchemeFor(palette: AppPalette): ColorScheme = when (palette) {
    AppPalette.BERLIN_BLUE -> darkColorScheme(
        primary = Color(0xFFADC6FF),
        onPrimary = Color(0xFF102F60),
        primaryContainer = Color(0xFF2C4678),
        onPrimaryContainer = Color(0xFFD8E2FF),
        secondary = Color(0xFFBFC6DC),
        onSecondary = Color(0xFF293041),
        secondaryContainer = Color(0xFF3F4759),
        onSecondaryContainer = Color(0xFFDBE2F9),
        surface = Color(0xFF121318),
        onSurface = Color(0xFFE3E2E9),
        surfaceVariant = Color(0xFF44474F),
        onSurfaceVariant = Color(0xFFC4C6D0)
    )
    AppPalette.SCHWARZWALD -> darkColorScheme(
        primary = Color(0xFF94D5B1),
        onPrimary = Color(0xFF003823),
        primaryContainer = Color(0xFF0C5136),
        onPrimaryContainer = Color(0xFFB0F1CC),
        secondary = Color(0xFFB4CCBC),
        onSecondary = Color(0xFF203529),
        secondaryContainer = Color(0xFF364B3F),
        onSecondaryContainer = Color(0xFFD0E8D7),
        surface = Color(0xFF101511),
        onSurface = Color(0xFFE0E4DF),
        surfaceVariant = Color(0xFF414942),
        onSurfaceVariant = Color(0xFFC0C9C0)
    )
    AppPalette.BAVARIA_GOLD -> darkColorScheme(
        primary = Color(0xFFFFBA4C),
        onPrimary = Color(0xFF492D00),
        primaryContainer = Color(0xFF694300),
        onPrimaryContainer = Color(0xFFFFDEAE),
        secondary = Color(0xFFE0C39E),
        onSecondary = Color(0xFF402D14),
        secondaryContainer = Color(0xFF584328),
        onSecondaryContainer = Color(0xFFFEDFB9),
        surface = Color(0xFF17130D),
        onSurface = Color(0xFFECE1D4),
        surfaceVariant = Color(0xFF4F4539),
        onSurfaceVariant = Color(0xFFD3C4B4)
    )
    AppPalette.BERRY_ROSE -> darkColorScheme(
        primary = Color(0xFFFFB1C5),
        onPrimary = Color(0xFF5E112C),
        primaryContainer = Color(0xFF7B2843),
        onPrimaryContainer = Color(0xFFFFD9E1),
        secondary = Color(0xFFE4BDC6),
        onSecondary = Color(0xFF432930),
        secondaryContainer = Color(0xFF5B3F46),
        onSecondaryContainer = Color(0xFFFFD9E1),
        surface = Color(0xFF181113),
        onSurface = Color(0xFFEDDFE1),
        surfaceVariant = Color(0xFF514346),
        onSurfaceVariant = Color(0xFFD5C2C5)
    )
    AppPalette.LAVENDER -> darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        surface = Color(0xFF141218),
        onSurface = Color(0xFFE6E0E9),
        surfaceVariant = Color(0xFF49454F),
        onSurfaceVariant = Color(0xFFCAC4D0)
    )
    AppPalette.MONOCHROME -> darkColorScheme(
        primary = Color(0xFFD5D7DD),
        onPrimary = Color(0xFF1C1D21),
        primaryContainer = Color(0xFF383A40),
        onPrimaryContainer = Color(0xFFECEEF3),
        secondary = Color(0xFFBFC1C8),
        onSecondary = Color(0xFF282A2F),
        secondaryContainer = Color(0xFF3D4047),
        onSecondaryContainer = Color(0xFFE5E6EB),
        surface = Color(0xFF121315),
        onSurface = Color(0xFFE4E5E9),
        surfaceVariant = Color(0xFF43454B),
        onSurfaceVariant = Color(0xFFC4C6CC)
    )
}

private fun ColorScheme.withAmoledBlack(): ColorScheme = copy(
    background = Color.Black,
    surface = Color.Black,
    surfaceContainerLowest = Color.Black,
    surfaceContainerLow = Color(0xFF0D0E11),
    surfaceContainer = Color(0xFF14161A),
    surfaceContainerHigh = Color(0xFF1C1E24),
    surfaceContainerHighest = Color(0xFF24272F)
)

private fun harmonize(color: Color, primary: Color): Color {
    return Color(MaterialColors.harmonize(color.toArgb(), primary.toArgb()))
}

private fun buildGenderColors(colorScheme: ColorScheme, darkTheme: Boolean): HarmonizedGenderColors {
    val primary = colorScheme.primary
    return if (darkTheme) {
        HarmonizedGenderColors(
            der = GenderColorPair(
                container = harmonize(Color(0xFF1C3B66), primary),
                onContainer = harmonize(Color(0xFFD2E4FF), primary)
            ),
            die = GenderColorPair(
                container = harmonize(Color(0xFF632132), primary),
                onContainer = harmonize(Color(0xFFFFD9DF), primary)
            ),
            das = GenderColorPair(
                container = harmonize(Color(0xFF1B4A32), primary),
                onContainer = harmonize(Color(0xFFC4F0D5), primary)
            )
        )
    } else {
        HarmonizedGenderColors(
            der = GenderColorPair(
                container = harmonize(Color(0xFFD6E4FF), primary),
                onContainer = harmonize(Color(0xFF001C3B), primary)
            ),
            die = GenderColorPair(
                container = harmonize(Color(0xFFFFD9DF), primary),
                onContainer = harmonize(Color(0xFF3E0015), primary)
            ),
            das = GenderColorPair(
                container = harmonize(Color(0xFFC9F0D8), primary),
                onContainer = harmonize(Color(0xFF002112), primary)
            )
        )
    }
}

@Composable
fun WortikTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = true,
    palette: AppPalette = AppPalette.BERLIN_BLUE,
    amoledBlack: Boolean = false,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val context = LocalContext.current
    val baseScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkSchemeFor(palette)
        else -> lightSchemeFor(palette)
    }
    val colorScheme = if (darkTheme && amoledBlack) baseScheme.withAmoledBlack() else baseScheme

    val genderColors = remember(colorScheme, darkTheme) {
        buildGenderColors(colorScheme, darkTheme)
    }

    CompositionLocalProvider(LocalGenderColors provides genderColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WortikTypography,
            content = content
        )
    }
}
