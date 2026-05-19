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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.integerResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.netarchive.R

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
            Toast.makeText(context, R.string.microphone_permission_required, Toast.LENGTH_LONG).show()
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
                .width(dimensionResource(id = R.dimen.voice_dialog_width))
                .height(dimensionResource(id = R.dimen.voice_dialog_height))
                .background(colorResource(id = R.color.voice_dialog_background), shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large)))
                .clickable(onClick = {})
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(id = R.dimen.voice_dialog_padding)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (state.isVoiceRecording) stringResource(R.string.voice_recording_in_progress)
                    else if (state.isVoiceProcessing) stringResource(R.string.voice_processing)
                    else stringResource(R.string.voice_recording_title),
                    fontSize = dimensionResource(id = R.dimen.voice_title_font_size).value.sp,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_xlarge)))

                if(state.isVoiceRecording){
                    Waveform(
                        isRecording = state.isVoiceRecording,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.voice_waveform_height))
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.voice_spacing_small)))
                    AnimatedText()
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))

                if (!state.isVoiceRecordDone && !state.isVoiceProcessing) {
                    IconButton(
                        onClick = {
                            if (vibrator.hasVibrator()) {
                                vibrator.vibrate(VibrationEffect.createOneShot(R.integer.vibration_duration.toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                            }
                            if (state.isVoiceRecording) {
                                viewModel.stopListening()
                            } else {
                                viewModel.startListening()
                            }
                        },
                        modifier = Modifier.size(dimensionResource(id = R.dimen.voice_icon_size)),
                    ) {
                        Icon(
                            imageVector = if (state.isVoiceRecording)
                                Icons.Outlined.Pause
                            else
                                Icons.Outlined.Mic,
                            contentDescription = if (state.isVoiceRecording) stringResource(R.string.stop) else stringResource(R.string.record),
                            modifier = Modifier.size(dimensionResource(id = R.dimen.voice_icon_size)),
                            tint = colorResource(id = R.color.voice_waveform_color)
                        )
                    }

                    Text(
                        text = if (state.isVoiceRecording) stringResource(R.string.press_to_stop)
                        else stringResource(R.string.press_to_start),
                        fontSize = dimensionResource(id = R.dimen.voice_small_text_font_size).value.sp
                    )
                } else if (state.isVoiceProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(dimensionResource(id = R.dimen.voice_progress_indicator_size)),
                        strokeWidth = dimensionResource(id = R.dimen.voice_progress_stroke_width)
                    )
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.voice_spacing_medium)))
                    Text(
                        text = stringResource(R.string.voice_processing),
                        fontSize = dimensionResource(id = R.dimen.voice_processing_text_font_size).value.sp
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.spacing_large)))

                if (state.recognizedText.isNotEmpty() && !state.isVoiceRecording) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(id = R.dimen.voice_card_height)),
                        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(dimensionResource(id = R.dimen.voice_dialog_padding))
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.recognized_text_label),
                                    fontSize = dimensionResource(id = R.dimen.voice_small_text_font_size).value.sp,
                                )
                                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.voice_spacing_medium)))
                                Text(
                                    text = state.recognizedText,
                                    fontSize = dimensionResource(id = R.dimen.voice_text_font_size).value.sp,
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.voice_button_spacing))
                ) {
                    if (state.isVoiceRecordDone) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.voice_button_spacing)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.closeRecordingPage() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(dimensionResource(id = R.dimen.voice_button_corner_radius))
                            ) {
                                Text(stringResource(R.string.cancel))
                            }

                            if(state.recognizedText.isNotBlank() && !state.recognizedText.startsWith(stringResource(R.string.speech_not_recognized))){
                                Button(
                                    onClick = {
                                        viewModel.applyRecognizedText()
                                        viewModel.closeRecordingPage()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.voice_button_corner_radius))
                                ) {
                                    Text(stringResource(R.string.apply))
                                }
                            }else{
                                Button(
                                    onClick = {
                                        viewModel.startListening()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(dimensionResource(id = R.dimen.voice_button_corner_radius))
                                ) {
                                    Text(stringResource(R.string.try_again))
                                }
                            }
                        }
                    }

                    if (!state.isVoiceRecordDone) {
                        Button(
                            onClick = { viewModel.closeRecordingPage() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(dimensionResource(id = R.dimen.voice_button_corner_radius))
                        ) {
                            Text(stringResource(R.string.close))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.voice_spacing_medium)))
            }
        }
    }
}

@Composable
fun Waveform(
    isRecording: Boolean,
    modifier: Modifier = Modifier
) {
    val barCount = integerResource(id = R.integer.voice_waveform_bar_count)

    val infiniteTransition = rememberInfiniteTransition()

    val animatedValues = List(barCount) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.2f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = integerResource(id = R.integer.voice_animation_duration) + index * integerResource(id = R.integer.voice_animation_bar_delay),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(dimensionResource(id = R.dimen.voice_waveform_height)),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
                .padding(dimensionResource(id = R.dimen.voice_waveform_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            animatedValues.forEach { anim ->
                val value = if (isRecording) anim.value else 0.1f

                Box(
                    modifier = Modifier
                        .width(dimensionResource(id = R.dimen.voice_waveform_bar_width))
                        .height((integerResource(id = R.integer.voice_waveform_min_height) + (value * integerResource(id = R.integer.voice_animation_duration_long))).dp)
                        .clip(RoundedCornerShape(dimensionResource(id = R.dimen.voice_waveform_bar_corner)))
                        .background(colorResource(id = R.color.voice_waveform_color))
                )

                Spacer(modifier = Modifier.width(dimensionResource(id = R.dimen.voice_waveform_bar_spacing)))
            }
        }
    }
}

@Composable
fun AnimatedText() {
    val dots by rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = integerResource(id = R.integer.voice_animation_dots_max).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(integerResource(id = R.integer.voice_animation_dots_frame_duration), easing = LinearEasing),
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
        text = stringResource(R.string.speak_now) + dotsText,
        fontSize = dimensionResource(id = R.dimen.voice_text_font_size).value.sp,
        fontWeight = FontWeight.Medium
    )
}