package com.wortik.app.ui.components

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.wortik.app.R
import com.wortik.app.domain.model.WordEntry
import java.net.URLEncoder
import java.util.Locale

@Stable
class PronunciationController(
    private val context: Context,
    private val onVolumeMuted: () -> Unit,
    private val onVolumeRestored: () -> Unit
) {

    var ttsReady by mutableStateOf(false)
        private set

    var isPlayingNative by mutableStateOf(false)
        private set

    var isSpeakingTts by mutableStateOf(false)
        private set

    var slowSpeed by mutableStateOf(false)

    private var activePlayer: MediaPlayer? = null
    private var pendingPlayback: (() -> Unit)? = null
    private val tts: TextToSpeech
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val volumeReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context?, intent: Intent?) {
            val action = pendingPlayback ?: return
            val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
            if (vol > 0) {
                pendingPlayback = null
                onVolumeRestored()
                action()
            }
        }
    }

    init {
        val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(volumeReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(volumeReceiver, filter)
            }
        }

        var instance: TextToSpeech? = null
        instance = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val resGermany = instance?.setLanguage(Locale.GERMANY)
                val ready = if (resGermany == TextToSpeech.LANG_MISSING_DATA ||
                    resGermany == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    val resGeneric = instance?.setLanguage(Locale.GERMAN)
                    resGeneric != TextToSpeech.LANG_MISSING_DATA &&
                        resGeneric != TextToSpeech.LANG_NOT_SUPPORTED
                } else {
                    true
                }
                ttsReady = ready
            }
        }
        instance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isSpeakingTts = true
            }

            override fun onDone(utteranceId: String?) {
                isSpeakingTts = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isSpeakingTts = false
            }
        })
        tts = instance
    }

    private fun runOrQueueIfMuted(action: () -> Unit) {
        val vol = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 1
        if (vol == 0) {
            pendingPlayback = action
            onVolumeMuted()
        } else {
            pendingPlayback = null
            action()
        }
    }

    fun toggleSlowSpeed() {
        slowSpeed = !slowSpeed
    }

    fun speakTts(text: String) {
        if (text.isBlank()) return
        runOrQueueIfMuted {
            if (ttsReady) {
                tts.setSpeechRate(if (slowSpeed) 0.72f else 1.0f)
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "wortik_tts_${System.currentTimeMillis()}")
            }
        }
    }

    fun speakEntryTts(entry: WordEntry) {
        val text = if (entry.gender.article.isNotEmpty()) {
            "${entry.gender.article} ${entry.word}"
        } else {
            entry.word
        }
        speakTts(text)
    }

    fun playGermanSentenceOnline(sentence: String) {
        val trimmed = sentence.trim().take(200)
        if (trimmed.isEmpty()) return
        val encoded = URLEncoder.encode(trimmed, "UTF-8")
        val url = "https://translate.google.com/translate_tts?ie=UTF-8&tl=de-DE&client=tw-ob&q=$encoded"
        playNativeAudio(
            audioUrl = url,
            headers = mapOf(
                "User-Agent" to "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36",
                "Referer" to "https://translate.google.com/"
            )
        )
    }

    fun playNativeAudio(
        audioUrl: String,
        headers: Map<String, String> = mapOf("User-Agent" to "WortikAndroid/0.1 (https://github.com/wortik)"),
        onError: (() -> Unit)? = null
    ) {
        if (audioUrl.isBlank()) {
            onError?.invoke()
            return
        }
        runOrQueueIfMuted {
            startMediaPlayer(audioUrl, headers, onError)
        }
    }

    private fun startMediaPlayer(
        audioUrl: String,
        headers: Map<String, String>,
        onError: (() -> Unit)?
    ) {
        runCatching {
            activePlayer?.release()
            isPlayingNative = true
            val speed = if (slowSpeed) 0.75f else 1.0f
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(
                    context,
                    Uri.parse(audioUrl),
                    headers
                )
                setOnPreparedListener { mp ->
                    runCatching {
                        mp.playbackParams = PlaybackParams().setSpeed(speed)
                    }
                    mp.start()
                }
                setOnCompletionListener {
                    it.release()
                    if (activePlayer === it) {
                        activePlayer = null
                        isPlayingNative = false
                    }
                }
                setOnErrorListener { mp, _, _ ->
                    mp.release()
                    if (activePlayer === mp) {
                        activePlayer = null
                        isPlayingNative = false
                    }
                    onError?.invoke()
                    true
                }
                prepareAsync()
            }
            activePlayer = player
        }.onFailure {
            isPlayingNative = false
            onError?.invoke()
        }
    }

    fun release() {
        pendingPlayback = null
        runCatching { context.unregisterReceiver(volumeReceiver) }
        activePlayer?.release()
        activePlayer = null
        isPlayingNative = false
        isSpeakingTts = false
        tts.stop()
        tts.shutdown()
    }
}

@Composable
fun rememberPronunciationController(
    onVolumeMuted: () -> Unit = {},
    onVolumeRestored: () -> Unit = {}
): PronunciationController {
    val context = LocalContext.current
    val controller = remember(context) {
        PronunciationController(context, onVolumeMuted, onVolumeRestored)
    }
    DisposableEffect(controller) {
        onDispose { controller.release() }
    }
    return controller
}

@Composable
fun GermanFlagIcon(
    modifier: Modifier = Modifier,
    width: Dp = 20.dp,
    height: Dp = 14.dp
) {
    Canvas(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(3.dp))
    ) {
        val stripeHeight = size.height / 3f
        drawRect(
            color = Color(0xFF1A1A1A),
            topLeft = Offset(0f, 0f),
            size = Size(size.width, stripeHeight)
        )
        drawRect(
            color = Color(0xFFDD0000),
            topLeft = Offset(0f, stripeHeight),
            size = Size(size.width, stripeHeight)
        )
        drawRect(
            color = Color(0xFFFFCE00),
            topLeft = Offset(0f, stripeHeight * 2f),
            size = Size(size.width, stripeHeight + 0.5f)
        )
    }
}

@Composable
fun PronunciationButtons(
    hasNativeAudio: Boolean,
    onPlayNative: () -> Unit,
    onPlayTts: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    isPlayingNative: Boolean = false,
    isSpeakingTts: Boolean = false,
    slowSpeed: Boolean? = null,
    onToggleSlowSpeed: (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val haptic = LocalHapticFeedback.current
    val nativeCd = stringResource(R.string.pronounce_native_cd)
    val ttsCd = stringResource(R.string.pronounce_tts_cd)
    val speedCd = stringResource(R.string.pronounce_speed_cd)
    val buttonHeight = if (compact) 34.dp else 40.dp
    val contentPadding = if (compact) {
        PaddingValues(horizontal = 10.dp)
    } else {
        PaddingValues(horizontal = 12.dp)
    }

    val pulseTransition = rememberInfiniteTransition(label = "audioPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "audioPulseScale"
    )

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (hasNativeAudio) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onPlayNative()
                },
                shape = CircleShape,
                color = containerColor,
                border = BorderStroke(
                    width = if (isPlayingNative) 1.5.dp else 1.dp,
                    color = if (isPlayingNative) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                    }
                ),
                modifier = Modifier
                    .height(buttonHeight)
                    .graphicsLayer {
                        if (isPlayingNative) {
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                    }
                    .semantics {
                        contentDescription = nativeCd
                        role = Role.Button
                    }
            ) {
                Row(
                    modifier = Modifier.padding(contentPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    GermanFlagIcon(
                        width = if (compact) 18.dp else 20.dp,
                        height = if (compact) 12.dp else 14.dp
                    )
                    if (!compact) {
                        Text(
                            text = stringResource(R.string.pronounce_native_label),
                            style = MaterialTheme.typography.labelMedium,
                            color = contentColor
                        )
                    }
                }
            }
        }

        Surface(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onPlayTts()
            },
            shape = CircleShape,
            color = containerColor,
            border = BorderStroke(
                width = if (isSpeakingTts) 1.5.dp else 1.dp,
                color = if (isSpeakingTts) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                }
            ),
            modifier = Modifier
                .height(buttonHeight)
                .graphicsLayer {
                    if (isSpeakingTts) {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                }
                .semantics {
                    contentDescription = ttsCd
                    role = Role.Button
                }
        ) {
            Row(
                modifier = Modifier.padding(contentPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.RecordVoiceOver,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(if (compact) 17.dp else 18.dp)
                )
                if (!compact) {
                    Text(
                        text = stringResource(R.string.pronounce_tts_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                }
            }
        }

        if (slowSpeed != null && onToggleSlowSpeed != null) {
            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onToggleSlowSpeed()
                },
                shape = CircleShape,
                color = if (slowSpeed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .height(buttonHeight)
                    .semantics {
                        contentDescription = speedCd
                        role = Role.Button
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Speed,
                        contentDescription = null,
                        tint = if (slowSpeed) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (slowSpeed) "0.75×" else "1×",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (slowSpeed) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}
