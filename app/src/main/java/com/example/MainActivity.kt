package com.example

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.DesktopContentRenderer
import com.example.ui.components.FloatingDesktopOverlay
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.DesktopWindowMode
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {

    private var viewModelInstance: ContainerViewModel? = null
    private var isSystemPipMode by mutableStateOf(false)

    fun triggerSystemPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val aspectRatio = Rational(16, 9)
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()
                enterPictureInPictureMode(params)
            } catch (_: Exception) {
                // Device or activity state does not support PiP at this moment
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        val mode = viewModelInstance?.desktopWindowMode?.value
        if (mode != null && mode != DesktopWindowMode.HIDDEN) {
            triggerSystemPictureInPicture()
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        isSystemPipMode = isInPictureInPictureMode
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ContainerViewModel = viewModel()
                viewModelInstance = viewModel
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

                if (isSystemPipMode) {
                    // Native Android PiP View: renders live distro desktop stream directly
                    val activeApp by viewModel.desktopActiveWindow.collectAsStateWithLifecycle()
                    val allSystems by viewModel.allSystems.collectAsStateWithLifecycle()
                    val activeId by viewModel.activeContainerId.collectAsStateWithLifecycle()
                    val distroName = allSystems.find { it.id == activeId }?.name ?: "Debian 13 (Trixie)"
                    val selectedWm by viewModel.desktopSelectedWm.collectAsStateWithLifecycle()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF0F172A))
                    ) {
                        DesktopContentRenderer(
                            activeApp = activeApp,
                            activeDistro = distroName,
                            selectedWm = selectedWm,
                            isCompact = true,
                            onLaunchApp = { viewModel.setDesktopActiveWindow(it) }
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = UDroidBg,
                            contentWindowInsets = WindowInsets.safeDrawing,
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            bottomBar = {
                                NavigationBar(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.navigationBars),
                                    containerColor = UDroidCardSurface,
                                    tonalElevation = 8.dp
                                ) {
                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.HOME,
                                        onClick = { viewModel.selectTab(MainTab.HOME) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                                contentDescription = "Home"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Home",
                                                fontWeight = if (selectedTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.OS,
                                        onClick = { viewModel.selectTab(MainTab.OS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.OS) Icons.Filled.Dns else Icons.Outlined.Dns,
                                                contentDescription = "OS"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "OS",
                                                fontWeight = if (selectedTab == MainTab.OS) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.TERMINAL,
                                        onClick = { viewModel.selectTab(MainTab.TERMINAL) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.TERMINAL) Icons.Filled.Terminal else Icons.Outlined.Terminal,
                                                contentDescription = "Terminal"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Terminal",
                                                fontWeight = if (selectedTab == MainTab.TERMINAL) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.APPS,
                                        onClick = { viewModel.selectTab(MainTab.APPS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.APPS) Icons.Filled.GridView else Icons.Outlined.GridView,
                                                contentDescription = "Apps"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Apps",
                                                fontWeight = if (selectedTab == MainTab.APPS) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.TOOLS,
                                        onClick = { viewModel.selectTab(MainTab.TOOLS) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.TOOLS) Icons.Filled.Build else Icons.Outlined.Build,
                                                contentDescription = "Tools"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "Tools",
                                                fontWeight = if (selectedTab == MainTab.TOOLS) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )

                                    NavigationBarItem(
                                        selected = selectedTab == MainTab.ABOUT,
                                        onClick = { viewModel.selectTab(MainTab.ABOUT) },
                                        icon = {
                                            Icon(
                                                imageVector = if (selectedTab == MainTab.ABOUT) Icons.Filled.Info else Icons.Outlined.Info,
                                                contentDescription = "About"
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = "About",
                                                fontWeight = if (selectedTab == MainTab.ABOUT) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = UDroidGreen,
                                            selectedTextColor = UDroidGreen,
                                            indicatorColor = Color(0xFFDCFCE7),
                                            unselectedIconColor = UDroidTextSecondary,
                                            unselectedTextColor = UDroidTextSecondary
                                        )
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                                    when (tab) {
                                        MainTab.HOME -> HomeScreen(viewModel = viewModel)
                                        MainTab.OS -> ContainersScreen(viewModel = viewModel)
                                        MainTab.TERMINAL -> TerminalScreen(viewModel = viewModel)
                                        MainTab.APPS -> AppsScreen(viewModel = viewModel)
                                        MainTab.TOOLS -> ToolsScreen(viewModel = viewModel)
                                        MainTab.ABOUT -> AboutJournalScreen(viewModel = viewModel)
                                    }
                                }
                            }
                        }

                        // Floating Desktop & App Window Overlay (Freeform, PiP, Fullscreen)
                        FloatingDesktopOverlay(
                            viewModel = viewModel,
                            onEnterSystemPip = { triggerSystemPictureInPicture() }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModelInstance = null
    }
}
