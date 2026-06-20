package de.piecha.switchwerk.ui.components

import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun SettingsSectionTabs(
    sections: List<String>,
    selectedIndex: Int,
    onSectionSelected: (Int) -> Unit
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        edgePadding = 0.dp
    ) {
        sections.forEachIndexed { index, title ->
            Tab(
                selected = index == selectedIndex,
                onClick = { onSectionSelected(index) },
                text = { Text(title) }
            )
        }
    }
}
