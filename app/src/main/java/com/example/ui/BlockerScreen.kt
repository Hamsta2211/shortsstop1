package com.example.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.service.AccessibilityUtils
import com.example.service.NotificationHelper
import com.example.ui.components.DailyTimerCard
import com.example.ui.components.PinManagementDialog
import com.example.ui.components.RecentBlocksCard
import com.example.ui.components.ServiceStatusCard
import com.example.ui.components.SettingsCard
import com.example.ui.components.SetupWizardDialog
import com.example.ui.components.StatsOverviewCard
import com.example.ui.components.TestSimulatorCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockerScreen(
    viewModel: BlockerViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var hasNotificationPermission by remember {
        mutableStateOf(NotificationHelper.hasNotificationPermission(context))
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted || NotificationHelper.hasNotificationPermission(context)
    }

    // Refresh accessibility service status and permission status whenever the user returns to the app
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshAccessibilityStatus(context)
                hasNotificationPermission = NotificationHelper.hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Shorts Blocker",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.setShowSetupDialog(true) },
                        modifier = Modifier.testTag("app_bar_info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Anleitung",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    ServiceStatusCard(
                        isServiceEnabled = uiState.isAccessibilityServiceEnabled,
                        isProtectionActive = uiState.settings.isProtectionActive,
                        isPinEnabled = uiState.settings.isPinEnabled,
                        onToggleProtection = { viewModel.toggleProtection(it) },
                        onOpenSettings = { AccessibilityUtils.openAccessibilitySettings(context) },
                        onShowGuide = { viewModel.setShowSetupDialog(true) }
                    )
                }
            }

            // Daily Timer Card
            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    DailyTimerCard(
                        settings = uiState.settings,
                        hasNotificationPermission = hasNotificationPermission,
                        onRequestNotificationPermission = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onToggleTimer = { enable ->
                            if (enable && !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            viewModel.toggleDailyTimer(enable)
                        },
                        onSetLimitMinutes = { viewModel.setDailyLimitMinutes(it) },
                        onResetTimer = { viewModel.resetDailyTimer() },
                        onSimulateNotification = { viewModel.simulateTimerExpiredNotification(context) }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    StatsOverviewCard(
                        stats = uiState.stats
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    SettingsCard(
                        settings = uiState.settings,
                        onYoutubeActionChange = { viewModel.setYoutubeAction(it) },
                        onBrowserActionChange = { viewModel.setBrowserAction(it) },
                        onVibrateChange = { viewModel.setVibrateOnBlock(it) },
                        onToastChange = { viewModel.setShowToastOnBlock(it) },
                        onOpenSetPin = { viewModel.openSetPinDialog() },
                        onOpenChangePin = { viewModel.openChangePinDialog() },
                        onOpenRemovePin = { viewModel.openRemovePinDialog() }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    TestSimulatorCard(
                        onSimulateBlock = { viewModel.simulateShortsDetection(context) }
                    )
                }
            }

            item {
                Box(
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .fillMaxWidth()
                ) {
                    RecentBlocksCard(
                        events = uiState.recentEvents,
                        onClearHistory = { viewModel.clearHistory() }
                    )
                }
            }
        }
    }

    if (uiState.showSetupDialog) {
        SetupWizardDialog(
            isServiceEnabled = uiState.isAccessibilityServiceEnabled,
            settings = uiState.settings,
            hasNotificationPermission = hasNotificationPermission,
            onRequestNotificationPermission = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            },
            onOpenAccessibilitySettings = { AccessibilityUtils.openAccessibilitySettings(context) },
            onOpenAppInfoSettings = { AccessibilityUtils.openAppDetailsSettings(context) },
            onToggleTimer = { viewModel.toggleDailyTimer(it) },
            onSetLimitMinutes = { viewModel.setDailyLimitMinutes(it) },
            onOpenSetPin = { viewModel.openSetPinDialog() },
            onSimulateBlock = { viewModel.simulateShortsDetection(context) },
            onDismiss = { viewModel.setShowSetupDialog(false) }
        )
    }

    uiState.pinDialogAction?.let { pinAction ->
        PinManagementDialog(
            action = pinAction,
            onValidatePin = { viewModel.validatePin(it) },
            onSetPin = { viewModel.setPin(it) },
            onRemovePin = { viewModel.removePin() },
            onDismiss = { viewModel.dismissPinDialog() }
        )
    }
}

