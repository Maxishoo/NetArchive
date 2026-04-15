package com.example.netarchive.ui.screens.add_note_screen

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun VoiceRecordingScreen(
    viewModel: CreateNoteViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

    fun hasMicrophonePermission(): Boolean {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        return hasPermission
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.initialize(context)
        } else {
            Toast.makeText(context, "Для записи голоса нужно разрешение", Toast.LENGTH_LONG).show()
            viewModel.closeRecordingPage()
        }
    }

    LaunchedEffect(Unit) {
        if (hasMicrophonePermission()) {
            viewModel.initialize(context)
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    Dialog(
        onDismissRequest = { viewModel.closeRecordingPage() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .width(350.dp)
                .height(530.dp)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .clickable(onClick = {})
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Заголовок
                Text(
                    text = if (state.isVoiceRecording) "Запись голоса..."
                    else if (state.isVoiceProcessing) "Обработка..."
                    else "Голосовая заметка",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Waveform
                if(state.isVoiceRecording){
                    Waveform(
                        isRecording = state.isVoiceRecording,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Spacer(modifier = Modifier.height(5.dp))
                    AnimatedText()
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Кнопка записи/паузы под волной
                if (!state.isVoiceRecordDone && !state.isVoiceProcessing) {
                    IconButton(
                        onClick = {
                            if (vibrator.hasVibrator()) {
                                vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            if (state.isVoiceRecording) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        },
                        modifier = Modifier.size(110.dp),
                    ) {
                        Icon(
                            imageVector = if (state.isVoiceRecording)
                                Icons.Outlined.Pause
                            else
                                Icons.Outlined.Mic,
                            contentDescription = if (state.isVoiceRecording) "Остановить" else "Запись",
                            modifier = Modifier.size(110.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = if (state.isVoiceRecording) "Нажмите, чтобы остановить"
                        else "Нажмите, чтобы начать запись",
                        fontSize = 12.sp
                    )
                } else if (state.isVoiceProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Обработка голоса...",
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Распознанный текст
                if (state.recognizedText.isNotEmpty() && !state.isVoiceRecording) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Распознанный текст:",
                                    fontSize = 12.sp,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.recognizedText,
                                    fontSize = 16.sp,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Кнопки внизу
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (state.isVoiceRecordDone) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.closeRecordingPage() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Отмена")
                            }

                            if(state.recognizedText.isNotBlank() && !state.recognizedText.startsWith("Речь не распознана")){
                                Button(
                                    onClick = {
                                        viewModel.applyRecognizedText()
                                        viewModel.closeRecordingPage()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Применить")
                                }
                            }else{
                                Button(
                                    onClick = {
                                        viewModel.startListening()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Ещё раз")
                                }
                            }
                        }
                    }

                    // Кнопка Закрыть
                    if (!state.isVoiceRecordDone) {
                        Button(
                            onClick = { viewModel.closeRecordingPage() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Закрыть")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun Waveform(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = 50

    val infiniteTransition = rememberInfiniteTransition()

    val animatedValues = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + index * 20,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            animatedValues.forEach { anim ->
                val value = if (isRecording) anim.value else 0.1f

                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height((20 + value * 60).dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF4D5D8A))
                )

                Spacer(modifier = Modifier.width(3.dp))
            }
        }
    }
}

@Composable
fun AnimatedText() {
    val dots by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val dotsText = when (dots.toInt()) {
        0 -> "."
        1 -> ".."
        2 -> "..."
        else -> ""
    }

    Text(
        text = "Говорите$dotsText",
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium
    )
}