package com.example.brainrottracker.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.brainrottracker.ui.theme.*

@Composable
fun PermissionScreen() {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(ErrorRed.copy(alpha = 0.15f))
                    .border(1.dp, ErrorRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = ErrorRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permission Required",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = ForegroundLight
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "To track your scrolling on Instagram and YouTube, we need Accessibility Permission.\n\nPlease find 'Brainrot Tracker Service' in the list and turn it ON.",
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                color = MutedForeground,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            GradientButton(
                text = "Open Settings",
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )
        }
    }
}
