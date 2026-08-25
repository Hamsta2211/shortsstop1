package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

sealed interface PinDialogAction {
    data class VerifyToDisableProtection(val onVerified: () -> Unit) : PinDialogAction
    data class VerifyToChangeSettings(val description: String, val onVerified: () -> Unit) : PinDialogAction
    data object SetNewPin : PinDialogAction
    data object ChangePin : PinDialogAction
    data object RemovePin : PinDialogAction
}

@Composable
fun PinManagementDialog(
    action: PinDialogAction,
    onValidatePin: (String) -> Boolean,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onDismiss: () -> Unit
) {
    when (action) {
        is PinDialogAction.VerifyToDisableProtection -> {
            VerifyPinDialog(
                title = "Schutz deaktivieren",
                description = "Gib deinen PIN ein, um die automatische Shorts-Blockierung zu pausieren.",
                icon = Icons.Default.Lock,
                onValidatePin = onValidatePin,
                onSuccess = {
                    action.onVerified()
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
        is PinDialogAction.VerifyToChangeSettings -> {
            VerifyPinDialog(
                title = "Einstellungen entsperren",
                description = action.description.ifEmpty { "Gib deinen PIN ein, um Einstellungen anzupassen." },
                icon = Icons.Default.Lock,
                onValidatePin = onValidatePin,
                onSuccess = {
                    action.onVerified()
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
        is PinDialogAction.SetNewPin -> {
            SetPinDialog(
                title = "PIN-Schutz einrichten",
                description = "Erstelle einen 4-stelligen Zahlencode, um Schutz- und Einstellungsänderungen zu sichern.",
                onSetPin = { pin ->
                    onSetPin(pin)
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
        is PinDialogAction.ChangePin -> {
            ChangePinDialog(
                onValidatePin = onValidatePin,
                onSetPin = { newPin ->
                    onSetPin(newPin)
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
        is PinDialogAction.RemovePin -> {
            VerifyPinDialog(
                title = "PIN-Schutz entfernen",
                description = "Gib deinen aktuellen PIN ein, um den Schutz zu deaktivieren.",
                icon = Icons.Default.LockReset,
                onValidatePin = onValidatePin,
                onSuccess = {
                    onRemovePin()
                    onDismiss()
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun VerifyPinDialog(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onValidatePin: (String) -> Boolean,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit
) {
    var pinText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pin_verify_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                PinInputField(
                    value = pinText,
                    onValueChange = {
                        if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                            pinText = it
                            errorMessage = null
                            if (it.length == 4) {
                                if (onValidatePin(it)) {
                                    onSuccess()
                                } else {
                                    errorMessage = "Falscher PIN. Bitte erneut versuchen."
                                }
                            }
                        }
                    },
                    focusRequester = focusRequester,
                    onDone = {
                        if (onValidatePin(pinText)) {
                            onSuccess()
                        } else {
                            errorMessage = "Falscher PIN. Bitte erneut versuchen."
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_cancel_button")
                    ) {
                        Text("Abbrechen")
                    }

                    Button(
                        onClick = {
                            if (onValidatePin(pinText)) {
                                onSuccess()
                            } else {
                                errorMessage = "Falscher PIN. Bitte erneut versuchen."
                            }
                        },
                        enabled = pinText.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_confirm_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Bestätigen")
                    }
                }
            }
        }
    }
}

@Composable
private fun SetPinDialog(
    title: String,
    description: String,
    onSetPin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(step) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pin_set_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Password,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (step == 1) title else "PIN wiederholen",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (step == 1) description else "Gib denselben PIN zur Bestätigung noch einmal ein.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                PinInputField(
                    value = if (step == 1) firstPin else confirmPin,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            errorMessage = null
                            if (step == 1) {
                                firstPin = input
                                if (input.length == 4) {
                                    step = 2
                                }
                            } else {
                                confirmPin = input
                                if (input.length == 4) {
                                    if (input == firstPin) {
                                        onSetPin(input)
                                    } else {
                                        errorMessage = "PINs stimmen nicht überein. Bitte von vorn beginnen."
                                        step = 1
                                        firstPin = ""
                                        confirmPin = ""
                                    }
                                }
                            }
                        }
                    },
                    focusRequester = focusRequester,
                    onDone = {
                        if (step == 1 && firstPin.length >= 4) {
                            step = 2
                        } else if (step == 2) {
                            if (confirmPin == firstPin) {
                                onSetPin(confirmPin)
                            } else {
                                errorMessage = "PINs stimmen nicht überein."
                            }
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_set_cancel_button")
                    ) {
                        Text("Abbrechen")
                    }

                    Button(
                        onClick = {
                            if (step == 1) {
                                if (firstPin.length >= 4) step = 2
                            } else {
                                if (confirmPin == firstPin) {
                                    onSetPin(confirmPin)
                                } else {
                                    errorMessage = "PINs stimmen nicht überein."
                                }
                            }
                        },
                        enabled = if (step == 1) firstPin.length >= 4 else confirmPin.length >= 4,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pin_set_next_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (step == 1) "Weiter" else "Speichern")
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangePinDialog(
    onValidatePin: (String) -> Boolean,
    onSetPin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var stage by remember { mutableStateOf(1) } // 1 = Old PIN, 2 = New PIN, 3 = Confirm New PIN
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmNewPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(stage) {
        focusRequester.requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("pin_change_dialog"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LockReset,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = when (stage) {
                        1 -> "Bisherigen PIN eingeben"
                        2 -> "Neuen PIN festlegen"
                        else -> "Neuen PIN wiederholen"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = when (stage) {
                        1 -> "Bitte bestätige zuerst deinen aktuellen PIN-Code."
                        2 -> "Wähle einen neuen 4-stelligen PIN-Code."
                        else -> "Gib den neuen PIN zur Bestätigung noch einmal ein."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                val currentText = when (stage) {
                    1 -> oldPin
                    2 -> newPin
                    else -> confirmNewPin
                }

                PinInputField(
                    value = currentText,
                    onValueChange = { input ->
                        if (input.length <= 6 && input.all { it.isDigit() }) {
                            errorMessage = null
                            when (stage) {
                                1 -> {
                                    oldPin = input
                                    if (input.length == 4) {
                                        if (onValidatePin(input)) {
                                            stage = 2
                                        } else {
                                            errorMessage = "Aktueller PIN ist falsch."
                                        }
                                    }
                                }
                                2 -> {
                                    newPin = input
                                    if (input.length == 4) {
                                        stage = 3
                                    }
                                }
                                3 -> {
                                    confirmNewPin = input
                                    if (input.length == 4) {
                                        if (input == newPin) {
                                            onSetPin(input)
                                        } else {
                                            errorMessage = "PINs stimmen nicht überein. Bitte neuen PIN erneut eingeben."
                                            stage = 2
                                            newPin = ""
                                            confirmNewPin = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    focusRequester = focusRequester,
                    onDone = {
                        when (stage) {
                            1 -> {
                                if (onValidatePin(oldPin)) stage = 2
                                else errorMessage = "Aktueller PIN ist falsch."
                            }
                            2 -> {
                                if (newPin.length >= 4) stage = 3
                            }
                            3 -> {
                                if (confirmNewPin == newPin) onSetPin(confirmNewPin)
                                else errorMessage = "PINs stimmen nicht überein."
                            }
                        }
                    }
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Abbrechen")
                    }

                    Button(
                        onClick = {
                            when (stage) {
                                1 -> {
                                    if (onValidatePin(oldPin)) stage = 2
                                    else errorMessage = "Aktueller PIN ist falsch."
                                }
                                2 -> {
                                    if (newPin.length >= 4) stage = 3
                                }
                                3 -> {
                                    if (confirmNewPin == newPin) onSetPin(confirmNewPin)
                                    else errorMessage = "PINs stimmen nicht überein."
                                }
                            }
                        },
                        enabled = when (stage) {
                            1 -> oldPin.length >= 4
                            2 -> newPin.length >= 4
                            else -> confirmNewPin.length >= 4
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (stage < 3) "Weiter" else "Speichern")
                    }
                }
            }
        }
    }
}

@Composable
private fun PinInputField(
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onDone: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Visual indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            repeat(4) { index ->
                val isFilled = index < value.length
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(
                            if (isFilled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }

        // Hidden / formatted numeric input field
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("pin_text_input"),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.NumberPassword,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onDone() }),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            placeholder = {
                Text(
                    text = "4-stelligen PIN eingeben",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}
