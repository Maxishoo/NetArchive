package com.example.netarchive.ui.components

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.netarchive.R
import qrscanner.CameraLens
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
                    .background(colorResource(id = R.color.black))
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.qr_scanner_title),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = colorResource(id = R.color.white),
                        fontWeight = FontWeight.Bold,
                        fontSize = dimensionResource(id = R.dimen.qr_scanner_title_font_size).value.sp
                    ),
                    modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.qr_scanner_title_padding_bottom))
                )
                Box(
                    modifier = Modifier
                        .size(dimensionResource(id = R.dimen.qr_scanner_view_size))
                        .clip(shape = RoundedCornerShape(dimensionResource(id = R.dimen.qr_scanner_corner_radius)))
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorResource(id = R.color.qr_scanner_bg_start),
                                    colorResource(id = R.color.qr_scanner_bg_end)
                                )
                            )
                        )
                        .border(
                            width = dimensionResource(id = R.dimen.qr_scanner_border_width),
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    colorResource(id = R.color.qr_scanner_border_green),
                                    colorResource(id = R.color.qr_scanner_border_blue)
                                )
                            ),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.qr_scanner_corner_radius))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isScanning) {
                        QrScanner(
                            modifier = Modifier
                                .clip(RoundedCornerShape(dimensionResource(id = R.dimen.qr_scanner_inner_clip_radius))),
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
                            zoomLevel = 1.5f,
                            maxZoomLevel = 3f
                        )
                    }
                }
                Text(
                    text = stringResource(R.string.qr_scanner_instruction),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = colorResource(id = R.color.white).copy(alpha = 0.8f),
                        fontSize = dimensionResource(id = R.dimen.qr_scanner_text_font_size).value.sp
                    ),
                    modifier = Modifier.padding(
                        top = dimensionResource(id = R.dimen.qr_scanner_instruction_padding_top),
                        bottom = dimensionResource(id = R.dimen.qr_scanner_instruction_padding_bottom)
                    )
                )
                Row(
                    modifier = Modifier
                        .padding(
                            horizontal = dimensionResource(id = R.dimen.qr_scanner_controls_padding_horizontal),
                            vertical = dimensionResource(id = R.dimen.qr_scanner_controls_padding_vertical)
                        )
                        .background(
                            color = colorResource(id = R.color.qr_scanner_controls_bg),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.qr_scanner_controls_corner_radius))
                        )
                        .height(dimensionResource(id = R.dimen.qr_scanner_controls_height))
                        .fillMaxWidth(0.8f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { flashlightOn = !flashlightOn }
                            .padding(vertical = dimensionResource(id = R.dimen.qr_scanner_controls_padding_vertical)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (flashlightOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                            contentDescription = stringResource(R.string.qr_scanner_flashlight),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.qr_scanner_icon_size)),
                            tint = if (flashlightOn) colorResource(id = R.color.qr_scanner_border_green) else colorResource(id = R.color.white)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.qr_scanner_spacer_width)))
                        Text(
                            text = stringResource(if (flashlightOn) R.string.qr_scanner_flash_off else R.string.qr_scanner_flash_on),
                            color = if (flashlightOn) colorResource(id = R.color.qr_scanner_border_green) else colorResource(id = R.color.white),
                            fontSize = dimensionResource(id = R.dimen.qr_scanner_text_font_size).value.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    VerticalDivider(
                        modifier = Modifier
                            .height(dimensionResource(id = R.dimen.qr_scanner_divider_height))
                            .width(dimensionResource(id = R.dimen.qr_scanner_divider_thickness)),
                        color = colorResource(id = R.color.qr_scanner_divider)
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { openImagePicker = true }
                            .padding(vertical = dimensionResource(id = R.dimen.qr_scanner_controls_padding_vertical)),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = stringResource(R.string.qr_scanner_gallery),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.qr_scanner_icon_size)),
                            tint = colorResource(id = R.color.white)
                        )
                        Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.qr_scanner_spacer_width)))
                        Text(
                            text = stringResource(R.string.qr_scanner_gallery),
                            color = colorResource(id = R.color.white),
                            fontSize = dimensionResource(id = R.dimen.qr_scanner_text_font_size).value.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            IconButton(
                onClick = onCloseClick,
                modifier = Modifier
                    .padding(
                        top = dimensionResource(id = R.dimen.qr_scanner_close_top_padding),
                        end = dimensionResource(id = R.dimen.qr_scanner_close_end_padding)
                    )
                    .size(dimensionResource(id = R.dimen.qr_scanner_close_button_size))
                    .background(
                        color = colorResource(id = R.color.white).copy(alpha = 0.2f),
                        shape = CircleShape
                    )
                    .align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = colorResource(id = R.color.white),
                    modifier = Modifier.size(dimensionResource(id = R.dimen.qr_scanner_icon_size))
                )
            }
        }
    }
}

private fun safeVibrate(context: Context) {
    try {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        if (vibrator?.hasVibrator() == true) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    R.integer.qr_scanner_vibration_duration.toLong(),
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}