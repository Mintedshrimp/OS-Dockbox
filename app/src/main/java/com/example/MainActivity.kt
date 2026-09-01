package com.example

import android.os.Bundle
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
import com.example.ui.components.DesktopViewerDialog
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ContainerViewModel
import com.example.ui.viewmodel.MainTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ContainerViewModel = viewModel()
                val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
                val showDesktop by viewModel.showDesktopViewer.collectAsStateWithLifecycle()
                val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(userMessage) {
                    userMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearUserMessage()
                    }
                }

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
                                selected = selectedTab == MainTab.LINUX,
                                onClick = { viewModel.selectTab(MainTab.LINUX) },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == MainTab.LINUX) Icons.Filled.FormatListBulleted else Icons.Outlined.FormatListBulleted,
                                        contentDescription = "Linux"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Linux",
                                        fontWeight = if (selectedTab == MainTab.LINUX) FontWeight.Bold else FontWeight.Normal,
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
                                selected = selectedTab == MainTab.CONVERT,
                                onClick = { viewModel.selectTab(MainTab.CONVERT) },
                                icon = {
                                    Icon(
                                        imageVector = if (selectedTab == MainTab.CONVERT) Icons.Filled.Transform else Icons.Outlined.Transform,
                                        contentDescription = "Convert"
                                    )
                                },
                                label = {
                                    Text(
                                        text = "Convert",
                                        fontWeight = if (selectedTab == MainTab.CONVERT) FontWeight.Bold else FontWeight.Normal,
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
                                MainTab.LINUX -> ContainersScreen(viewModel = viewModel)
                                MainTab.TERMINAL -> TerminalScreen(viewModel = viewModel)
                                MainTab.APPS -> AppsScreen(viewModel = viewModel)
                                MainTab.CONVERT -> ConverterScreen(viewModel = viewModel)
                                MainTab.ABOUT -> AboutJournalScreen(viewModel = viewModel)
                            }
                        }
                    }
                }

                if (showDesktop) {
                    DesktopViewerDialog(
                        onDismiss = { viewModel.showDesktop(false) }
                    )
                }
            }
        }
    }
}
