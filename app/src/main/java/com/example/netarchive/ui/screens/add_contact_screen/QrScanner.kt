package com.example.netarchive.ui.screens.add_contact_screen

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import qrscanner.CameraLens
import qrscanner.OverlayShape
import qrscanner.QrScanner

@Composable
fun QrScannerDialog(
    onQrUrlChange: (String) -> Unit,
    onCloseClick: () -> Unit
) {
    var flashlightOn by remember { mutableStateOf(false) }
    var openImagePicker by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onCloseClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .background(color = Color.White)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .background(color = Color.Black)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Сканирование QR кода",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(shape = RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1A1A1A),
                                        Color(0xFF0A0A0A)
                                    )
                                )
                            )
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF4CAF50),
                                        Color(0xFF2196F3)
                                    )
                                ),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isScanning) {
                            QrScanner(
                                modifier = Modifier
                                    .padding(top = 20.dp)
                                    .clip(RoundedCornerShape(20.dp)),
                                flashlightOn = flashlightOn,
                                cameraLens = CameraLens.Back,
                                openImagePicker = openImagePicker,
                                onCompletion = { scannedData ->
                                    safeVibrate(context)
                                    isScanning = false
                                    onQrUrlChange(scannedData)
                                    onCloseClick()
                                },
                                imagePickerHandler = { shouldOpen ->
                                    openImagePicker = shouldOpen
                                },
                                onFailure = { exception ->
                                    println("Scanning failed: $exception")
                                    isScanning = true
                                },
                                overlayShape = OverlayShape.Square,
                                overlayColor = Color(0x88000000),
                                overlayBorderColor = Color.White,
                                zoomLevel = 1.5f,
                                maxZoomLevel = 3f
                            )
                        }

                    }
                    Text(
                        text = "Наведите камеру на QR код",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        ),
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .background(
                                color = Color(0xFF1C1C1E),
                                shape = RoundedCornerShape(50.dp)
                            )
                            .height(56.dp)
                            .fillMaxWidth(0.8f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { flashlightOn = !flashlightOn }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (flashlightOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                                contentDescription = "flash",
                                modifier = Modifier.size(24.dp),
                                tint = if (flashlightOn) Color(0xFF4CAF50) else Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (flashlightOn) "Выкл." else "Вкл.",
                                color = if (flashlightOn) Color(0xFF4CAF50) else Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        VerticalDivider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp),
                            thickness = 1.dp,
                            color = Color(0xFF38383A)
                        )
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { openImagePicker = true }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Image,
                                contentDescription = "gallery",
                                modifier = Modifier.size(24.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Галерея",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .padding(top = 40.dp, end = 12.dp)
                    .size(44.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

private fun safeVibrate(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}