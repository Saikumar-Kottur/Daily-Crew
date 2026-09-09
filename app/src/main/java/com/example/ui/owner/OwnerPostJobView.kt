package com.example.ui.owner

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JobCategory
import com.example.ui.theme.*

@Composable
fun OwnerPostJobView(
    onPublishJob: (
        title: String,
        category: JobCategory,
        wage: Double,
        workersCount: Int,
        dressCode: String,
        instructions: String,
        location: String,
        time: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("Catering Steward & Server") }
    var selectedCategory by remember { mutableStateOf(JobCategory.CATERING) }
    var wageText by remember { mutableStateOf("850") }
    var workersCountText by remember { mutableStateOf("4") }
    var dressCode by remember { mutableStateOf("White Formal Shirt, Black Trousers, Black Shoes") }
    var instructions by remember { mutableStateOf("Report directly to Banquet Captain at Gate 2. Clean groomed appearance.") }
    var location by remember { mutableStateOf("Banjara Hills, Hyderabad") }
    var time by remember { mutableStateOf("Tonight, 6:00 PM - 2:00 AM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("owner_post_job_screen")
    ) {
        Text(
            text = "Create Worker Shift Requirement",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
        Text(
            text = "Your shift will be broadcasted instantly to top rated, verified workers in your radius.",
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark)
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Job Title
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Job Title / Role Required") },
            placeholder = { Text("e.g. Senior Steward, Kitchen Line Helper") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("post_job_title_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Selection
        Text(
            text = "Select Category",
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondaryDark)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            JobCategory.values().filter { it != JobCategory.ALL }.forEach { cat ->
                val isSelected = cat == selectedCategory
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) BrandSecondaryCyan else BrandCardDark,
                    border = if (!isSelected) BorderStroke(1.dp, BrandCardBorderDark) else null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSelected) Color.Black else Color.White,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Wage & Worker Count Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = wageText,
                onValueChange = { wageText = it },
                label = { Text("Wage Per Shift (₹)") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("post_job_wage_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrandSecondaryCyan,
                    unfocusedBorderColor = BrandCardBorderDark
                )
            )

            OutlinedTextField(
                value = workersCountText,
                onValueChange = { workersCountText = it },
                label = { Text("Workers Needed") },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("post_job_workers_count_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = BrandSecondaryCyan,
                    unfocusedBorderColor = BrandCardBorderDark
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Shift Date & Time
        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Shift Timing") },
            placeholder = { Text("e.g. Tonight, 5:00 PM - 1:00 AM") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Location
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Venue / Business Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Dress Code
        OutlinedTextField(
            value = dressCode,
            onValueChange = { dressCode = it },
            label = { Text("Dress Code Requirement") },
            placeholder = { Text("e.g. Black shirt, Black pants, Closed shoes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Instructions
        OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Supervisor / Venue Instructions") },
            placeholder = { Text("Report to Banquet Captain. Food provided.") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = BrandSecondaryCyan,
                unfocusedBorderColor = BrandCardBorderDark
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Publish Button
        Button(
            onClick = {
                val wageAmt = wageText.toDoubleOrNull() ?: 800.0
                val count = workersCountText.toIntOrNull() ?: 2
                onPublishJob(
                    title,
                    selectedCategory,
                    wageAmt,
                    count,
                    dressCode,
                    instructions,
                    location,
                    time
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_job_shift_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandSecondaryCyan,
                contentColor = Color.Black
            )
        ) {
            Icon(imageVector = Icons.Default.Publish, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "PUBLISH JOB SHIFT NOW",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black)
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
