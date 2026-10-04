package com.projectx.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.projectx.app.theme.*

@Composable
fun UnderlineTabRow(
    tabs: List<String>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.Start
    ) {
        tabs.forEachIndexed { index, tabName ->
            val isSelected = index == selectedTabIndex
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clickable { onTabSelected(index) }
            ) {
                Text(
                    text = tabName,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrimaryIndigo else MutedText,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Box(
                    modifier = Modifier
                        .height(3.dp)
                        .width(if (isSelected) 48.dp else 0.dp)
                        .background(if (isSelected) PrimaryIndigo else PageBackground)
                )
            }
        }
    }
}
