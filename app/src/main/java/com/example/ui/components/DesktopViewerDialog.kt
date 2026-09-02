package com.example.ui.components

import androidx.compose.runtime.Composable

@Composable
fun DesktopViewerDialog(
    onDismiss: () -> Unit,
    activeDistro: String = "Debian 13 (Cinnamon Desktop)"
) {
    InteractiveDesktopViewerDialog(
        onDismiss = onDismiss,
        activeDistro = activeDistro
    )
}
