package com.example.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobCategory
import com.example.ui.theme.*

@Composable
fun EmergencyHiringDialog(
    onDismiss: () -> Unit,
    onBroadcast: (title: String, category: JobCategory, wage: Double, count: Int) -> Unit
) {
    var title by remember { mutableStateOf("Emergency Banquet Steward") }
    var selectedCategory by remember { mutableStateOf(JobCategory.CATERING) }
    var workerCount by remember { mutableIntStateOf(2) }
    var wageAmount by remember { mutableDoubleStateOf(950.0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BrandSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = BrandAmber,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "⚡ Emergency 30-Min Staffing",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Instant high-priority push broadcast to nearby standby crew",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark, fontSize = 11.sp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandAmberDim),
                    border = BorderStroke(1.dp, BrandAmber.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = BrandAmber, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Workers are required to arrive on-site within 30 minutes. 12 standby crew currently online nearby.",
                            color = BrandAmber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Urgent Role Title") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = BrandAmber,
                        unfocusedBorderColor = BrandCardBorderDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text("Workers Needed: $workerCount", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { count ->
                        FilterChip(
                            selected = workerCount == count,
                            onClick = { workerCount = count },
                            label = { Text("$count Crew", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandAmber,
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Emergency Shift Wage: ₹${wageAmount.toInt()}", color = BrandPrimaryGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Slider(
                    value = wageAmount.toFloat(),
                    onValueChange = { wageAmount = it.toDouble() },
                    valueRange = 800f..1500f,
                    steps = 6,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandAmber,
                        activeTrackColor = BrandAmber
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onBroadcast(title.trim(), selectedCategory, wageAmount, workerCount)
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandAmber,
                    disabledContainerColor = BrandAmber.copy(alpha = 0.3f),
                    disabledContentColor = Color.DarkGray
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("btn_confirm_emergency_broadcast")
            ) {
                Icon(Icons.Default.Sensors, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Broadcast Emergency Dispatch", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondaryDark)
            }
        }
    )
}
