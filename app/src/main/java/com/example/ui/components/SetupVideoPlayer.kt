package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlertAmber
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.RedPrimary
import com.example.ui.theme.SafeGreen
import kotlinx.coroutines.delay

data class VideoScene(
    val timestampSeconds: Int,
    val timeLabel: String,
    val title: String,
    val subtitle: String,
    val screenType: ScreenType,
    val highlightAction: String
)

enum class ScreenType {
    APP_START,
    ACCESSIBILITY_BLOCKED,
    SYSTEM_SETTINGS_APPS,
    APP_INFO_RESTRICTED,
    ACCESSIBILITY_ENABLE,
    APP_SUCCESS
}

val TUTORIAL_SCENES = listOf(
    VideoScene(
        timestampSeconds = 0,
        timeLabel = "0:00",
        title = "1. Start in der App",
        subtitle = "Auf 'In den Android-Einstellungen aktivieren' tippen.",
        screenType = ScreenType.APP_START,
        highlightAction = "Tippe auf den roten Button"
    ),
    VideoScene(
        timestampSeconds = 8,
        timeLabel = "0:08",
        title = "2. 'Zugriff verweigert' Popup",
        subtitle = "Android 13/14/15 blockiert die Eingabehilfe zunächst als eingeschränkte Berechtigung.",
        screenType = ScreenType.ACCESSIBILITY_BLOCKED,
        highlightAction = "Dialog mit 'Schließen' beenden"
    ),
    VideoScene(
        timestampSeconds = 17,
        timeLabel = "0:17",
        title = "3. In Android Einstellungen -> Apps",
        subtitle = "Öffne Einstellungen -> Apps und wähle 'Shorts Blocker'.",
        screenType = ScreenType.SYSTEM_SETTINGS_APPS,
        highlightAction = "'Shorts Blocker' in App-Liste öffnen"
    ),
    VideoScene(
        timestampSeconds = 24,
        timeLabel = "0:24",
        title = "4. 3-Punkte-Menü freischalten",
        subtitle = "Tippe oben rechts auf die 3 Punkte -> 'Eingeschränkte Einstellungen zulassen'.",
        screenType = ScreenType.APP_INFO_RESTRICTED,
        highlightAction = "3 Punkte ⋮ -> Eingeschränkte Einstellungen zulassen"
    ),
    VideoScene(
        timestampSeconds = 34,
        timeLabel = "0:34",
        title = "5. Dienst einschalten",
        subtitle = "Gehe zurück zu Eingabehilfe -> Installierte Apps -> Shorts Blocker -> Schalter auf 'Ein'.",
        screenType = ScreenType.ACCESSIBILITY_ENABLE,
        highlightAction = "Schalter auf 'Ein' schalten & 'Zulassen' bestätigen"
    ),
    VideoScene(
        timestampSeconds = 40,
        timeLabel = "0:40",
        title = "6. Fertig eingerichtet! 🎉",
        subtitle = "Kehre zur App zurück. Der Schutz ist nun aktiv und leitet Shorts zuverlässig um!",
        screenType = ScreenType.APP_SUCCESS,
        highlightAction = "Schutz ist aktiv & bereit"
    )
)

const val TOTAL_VIDEO_DURATION_SECONDS = 42

@Composable
fun SetupVideoPlayer(
    modifier: Modifier = Modifier,
    onOpenAppInfoDirect: () -> Unit = {},
    onOpenAccessibilityDirect: () -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableFloatStateOf(0f) }
    var playbackSpeed by remember { mutableFloatStateOf(1f) }

    // Playback loop
    LaunchedEffect(isPlaying, playbackSpeed) {
        if (!isPlaying) return@LaunchedEffect
        while (isPlaying) {
            delay((50 / playbackSpeed).toLong())
            currentSeconds += 0.05f
            if (currentSeconds >= TOTAL_VIDEO_DURATION_SECONDS) {
                currentSeconds = 0f
            }
        }
    }

    val currentScene = remember(currentSeconds) {
        TUTORIAL_SCENES.lastOrNull { currentSeconds.toInt() >= it.timestampSeconds } ?: TUTORIAL_SCENES.first()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "tap_indicator")
    val tapPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("setup_video_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = DarkSurface
        ),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with Live Video Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) AlertAmber else MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Video-Anleitung (Eingeschränkte Berechtigungen)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = String.format("%02d:%02d / 00:42", currentSeconds.toInt() / 60, currentSeconds.toInt() % 60),
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Video Frame Canvas (Recreating the exact phone screencast from user video)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 11f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(DarkBackground)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .clickable { isPlaying = !isPlaying }
            ) {
                AnimatedContent(
                    targetState = currentScene.screenType,
                    transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                    label = "screen_content"
                ) { screenType ->
                    when (screenType) {
                        ScreenType.APP_START -> VideoSceneAppStart(tapPulse)
                        ScreenType.ACCESSIBILITY_BLOCKED -> VideoSceneAccessibilityBlocked(tapPulse)
                        ScreenType.SYSTEM_SETTINGS_APPS -> VideoSceneSettingsApps(tapPulse)
                        ScreenType.APP_INFO_RESTRICTED -> VideoSceneAppInfoRestricted(tapPulse)
                        ScreenType.ACCESSIBILITY_ENABLE -> VideoSceneAccessibilityEnable(tapPulse)
                        ScreenType.APP_SUCCESS -> VideoSceneAppSuccess()
                    }
                }

                // Overlay Controls Play/Pause Badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausieren" else "Abspielen",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isPlaying) "Tippen zum Pausieren" else "Tippen zum Weiterspielen",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Timeline Slider
            Slider(
                value = currentSeconds,
                onValueChange = {
                    currentSeconds = it
                },
                valueRange = 0f..TOTAL_VIDEO_DURATION_SECONDS.toFloat(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .testTag("video_timeline_slider"),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            // Current Step Description Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentScene.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = currentScene.timeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = currentScene.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = AlertAmber,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentScene.highlightAction,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = AlertAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Scene Quick-Jump Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TUTORIAL_SCENES) { scene ->
                    val isSelected = currentScene.timestampSeconds == scene.timestampSeconds
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            currentSeconds = scene.timestampSeconds.toFloat()
                        },
                        label = { Text("${scene.timeLabel} ${scene.title.substringBefore('.')}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback controls & Speed
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            currentSeconds = 0f
                            isPlaying = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Von vorne abspielen",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { isPlaying = !isPlaying }
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(1f, 1.5f, 2f).forEach { speed ->
                        val isSpeedActive = playbackSpeed == speed
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isSpeedActive) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .clickable { playbackSpeed = speed }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSpeedActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSpeedActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Direct Action Buttons from Tutorial
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenAppInfoDirect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AlertAmber,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(imageVector = Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("1. App-Info öffnen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenAccessibilityDirect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("2. Eingabehilfe öffnen", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/* =========================================================
 * Visual Phone Mockup Scenes replicating the user's video
 * ========================================================= */

@Composable
private fun VideoSceneAppStart(tapPulse: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Shorts Blocker", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant)
                .padding(10.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(RedPrimary))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Dienst inaktiv (Erlaubnis nötig)", color = Color.LightGray, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(tapPulse)
                        .clip(RoundedCornerShape(8.dp))
                        .background(RedPrimary)
                        .padding(vertical = 8.dp, horizontal = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("In den Android-Einstellungen aktivieren ⚙️", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VideoSceneAccessibilityBlocked(tapPulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        // Accessibility background list mockup
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            Text("< Installierte Apps", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text("Shorts Blocker (Aus - Gesteuert durch eingeschränkte Einstellung)", color = Color.Gray, fontSize = 9.sp)
        }

        // The exact security popup from Android 13/14/15 / Samsung One UI
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF222222))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(4.dp))
                Text("App wurde Zugriff verweigert", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Die App funktioniert ohne diese eingeschränkte Berechtigung möglicherweise nicht richtig.",
                    color = Color.LightGray,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .scale(tapPulse)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF333333))
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Schließen", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun VideoSceneSettingsApps(tapPulse: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(12.dp)
    ) {
        Text("< Anwendungen suchen", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF202020))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("🔍 short", color = Color.LightGray, fontSize = 10.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(tapPulse)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
                .padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = RedPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Shorts Blocker", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Installiert • 20 MB", color = Color.Gray, fontSize = 9.sp)
                }
            }
        }
    }
}

@Composable
private fun VideoSceneAppInfoRestricted(tapPulse: Float) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("< App-Info", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(AlertAmber.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text("Shorts Blocker", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Berechtigungen & Einstellungen", color = Color.Gray, fontSize = 9.sp)

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Dots Dropdown menu
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.End)
                    .scale(tapPulse)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF2A2A2A))
                    .border(1.dp, AlertAmber, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = AlertAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Eingeschränkte Einstellungen zulassen",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text("ℹ️ Einmalig mit PIN / Fingerabdruck bestätigen", color = AlertAmber, fontSize = 8.sp)
        }
    }
}

@Composable
private fun VideoSceneAccessibilityEnable(tapPulse: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF141414))
            .padding(12.dp)
    ) {
        Text("< Shorts Blocker", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .scale(tapPulse)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF242424))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Shorts Blocker Dienst", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Ein / Aus Schalter", color = Color.Gray, fontSize = 9.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(SafeGreen)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("EIN", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Popup: 'Shorts Blocker' die vollständige Kontrolle geben? -> [Zulassen]", color = Color.LightGray, fontSize = 9.sp)
    }
}

@Composable
private fun VideoSceneAppSuccess() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
            .padding(12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text("Schutz aktiv & bereit! 🛡️", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text("YouTube Shorts werden jetzt automatisch erkannt und sofort geschlossen.", color = Color.LightGray, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}
