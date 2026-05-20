package com.example.netarchive.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.netarchive.R
import com.example.netarchive.ui.theme.AppTheme
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties


@Composable
fun QrDialog(qrCode: ImageBitmap?, onCloseClick: () -> Unit) {
    Dialog(
        onDismissRequest = onCloseClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .height(450.dp)
                .background(AppTheme.colors.cardBackground, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
                .clickable(onClick = {})
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (qrCode != null) {
                    Image(
                        bitmap = qrCode,
                        contentDescription = "QR Code",
                        modifier = Modifier.size(300.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Text(stringResource(R.string.qr_load_failed))
                }
                Text(
                    text = stringResource(R.string.qr_hint),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onCloseClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.close),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

    }
}