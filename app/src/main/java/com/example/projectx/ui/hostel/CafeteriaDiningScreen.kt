package com.projectx.app.ui.hostel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.projectx.app.components.*
import com.projectx.app.model.hostel.DiningMeal
import com.projectx.app.theme.*
import com.projectx.app.util.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CafeteriaDiningScreen(
    onMenuClick: () -> Unit,
    hostelViewModel: HostelViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val mealsState by hostelViewModel.mealsState.collectAsState()
    val qrToken by hostelViewModel.diningQrTokenState.collectAsState()

    var selectedMealFilter by remember { mutableStateOf("All") }
    var showQrDialog by remember { mutableStateOf(false) }

    val filterOptions = listOf("All", "Breakfast", "Lunch", "Snacks", "Dinner")

    AppScaffold(
        title = "Cafeteria & Dining",
        onMenuClick = onMenuClick,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Demo Label Banner
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚡ DEMO DINING PASSPORT — FOR DEVELOPMENT PREVIEW ONLY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Header Context Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                border = BorderStroke(1.dp, SurfaceBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Restaurant, contentDescription = null, tint = PrimaryIndigo, modifier = Modifier.size(22.dp))
                            Text("Central Mess — Ground Floor", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)
                        }

                        StatusPill(text = "Active Mess Pass", tone = StatusTone.SUCCESS)
                    }

                    Text(
                        text = "Scan your reusable dining QR code at the entrance scanner to record meal entry.",
                        fontSize = 12.sp,
                        color = BodyText
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Button(
                        onClick = { showQrDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Show My Reusable Dining QR Code", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Meal Filter Selector
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                filterOptions.forEach { filter ->
                    FilterChip(
                        selected = selectedMealFilter == filter,
                        onClick = { selectedMealFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Text("Today's Dining Schedule & Menu", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = HeadingNavy)

            when (val resource = mealsState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                }
                is Resource.Error -> {
                    EmptyStateCard(title = resource.message)
                }
                is Resource.Success -> {
                    val filteredMeals = resource.data.filter {
                        selectedMealFilter == "All" || it.mealType.equals(selectedMealFilter, ignoreCase = true)
                    }

                    if (filteredMeals.isEmpty()) {
                        EmptyStateCard(title = "No menu available for $selectedMealFilter.")
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 32.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredMeals, key = { it.mealId }) { meal ->
                                MealCard(meal = meal)
                            }
                        }
                    }
                }
                else -> EmptyStateCard(title = "No dining menu available today.")
            }
        }

        // Reusable QR Code Dialog
        if (showQrDialog) {
            AlertDialog(
                onDismissRequest = { showQrDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = PrimaryIndigo)
                        Text("Reusable Dining QR Pass", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Vector Canvas QR Representation
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = BorderStroke(2.dp, PrimaryIndigo),
                            modifier = Modifier.size(200.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val cellSize = size.width / 7f
                                    val darkColor = Color(0xFF0F172A)

                                    // Render 3 Corner Position Detection Patterns
                                    drawRect(darkColor, Offset(0f, 0f), Size(cellSize * 2, cellSize * 2))
                                    drawRect(darkColor, Offset(size.width - cellSize * 2, 0f), Size(cellSize * 2, cellSize * 2))
                                    drawRect(darkColor, Offset(0f, size.height - cellSize * 2), Size(cellSize * 2, cellSize * 2))

                                    // Render Inner Data Grid Pattern derived from qrToken hash
                                    val tokenHash = qrToken.hashCode()
                                    for (r in 0..6) {
                                        for (c in 0..6) {
                                            if ((r < 2 && c < 2) || (r < 2 && c > 4) || (r > 4 && c < 2)) continue
                                            val isBitSet = ((tokenHash shr (r * 7 + c)) and 1) == 1
                                            if (isBitSet) {
                                                drawRect(
                                                    color = darkColor,
                                                    topLeft = Offset(c * cellSize, r * cellSize),
                                                    size = Size(cellSize * 0.9f, cellSize * 0.9f)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Text(
                            text = "Token: $qrToken",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )

                        Text(
                            text = "Scan this QR pass at the mess scanner counter to claim your meal.",
                            fontSize = 11.sp,
                            color = MutedText
                        )

                        OutlinedButton(
                            onClick = { hostelViewModel.regenerateDiningQr() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Regenerate My Reusable QR Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showQrDialog = false }) {
                        Text("Close", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
fun MealCard(meal: DiningMeal) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, SurfaceBorder)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = meal.mealType,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = HeadingNavy
                )

                meal.calories?.let { cal ->
                    Surface(
                        shape = PillShape,
                        color = SecondaryEmeraldBg
                    ) {
                        Text(
                            text = cal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryEmerald,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Text(
                text = "⏰ ${meal.timeRange} • ${meal.location}",
                fontSize = 11.sp,
                color = MutedText,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider(color = SurfaceBorder, thickness = 1.dp)

            Text(
                text = "Menu: ${meal.menuItems.joinToString(", ")}",
                fontSize = 12.sp,
                color = BodyText
            )
        }
    }
}
