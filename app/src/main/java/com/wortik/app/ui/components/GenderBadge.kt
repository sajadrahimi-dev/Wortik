package com.wortik.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wortik.app.domain.model.Gender
import com.wortik.app.domain.model.PartOfSpeech
import com.wortik.app.ui.theme.LocalGenderColors

@Composable
fun GenderBadge(
    gender: Gender,
    partOfSpeech: PartOfSpeech,
    modifier: Modifier = Modifier
) {
    val label = when (gender) {
        Gender.DER -> "♂ der"
        Gender.DIE -> "♀ die"
        Gender.DAS -> "⚲ das"
        Gender.NONE -> partOfSpeech.shortLabel
    }
    if (label.isEmpty()) return

    val genderColors = LocalGenderColors.current.forGender(gender)
    val containerColor = genderColors?.container ?: MaterialTheme.colorScheme.surfaceContainerHighest
    val contentColor = genderColors?.onContainer ?: MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}
