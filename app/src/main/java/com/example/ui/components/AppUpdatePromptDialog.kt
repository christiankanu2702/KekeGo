package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.KekeGoRepository
import com.example.ui.theme.*

@Composable
fun AppUpdatePromptDialog(
    updateInfo: com.example.data.models.AppUpdateInfo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = {
            if (!updateInfo.isMandatory) onDismiss()
        },
        containerColor = AsphaltDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(SafetyAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = SafetyAmber,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "New Update Available!",
                        color = HighContrastWhite,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "v${updateInfo.latestVersionName} (Build #${updateInfo.latestVersionCode})",
                        color = Color(0xFF00D664),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    color = AsphaltCard,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AsphaltDivider),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "What's New in KekeGo:",
                            color = SafetyAmber,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = updateInfo.releaseNotes,
                            color = HighContrastWhite,
                            fontSize = 12.sp
                        )
                    }
                }

                Text(
                    text = if (updateInfo.isMandatory)
                        "This update contains critical transit safety and telemetry fixes required to continue booking rides."
                    else
                        "Install the latest update to enjoy improved 3D maps, faster booking, and live driver chat.",
                    color = MutedSilver,
                    fontSize = 11.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl))
                    context.startActivity(browserIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D664), contentColor = AsphaltDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Update KekeGo Now", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = if (!updateInfo.isMandatory) {
            {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Remind Me Later", color = MutedSilver)
                }
            }
        } else null
    )
}
