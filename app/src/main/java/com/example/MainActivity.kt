package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.screens.DocumentPreviewScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.FormScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsState()

            val isDarkTheme = when (themeMode) {
                "DARK" -> true
                "LIGHT" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppContent(viewModel = mainViewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request POST_NOTIFICATIONS permission for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.showMessage("Izin notifikasi aktif.")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Collect user snackbar messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val currentUser by viewModel.currentUser.collectAsState()

    val showBottomBar = currentUser.isLoggedIn && currentScreen in listOf(
        MainViewModel.SCREEN_HOME,
        MainViewModel.SCREEN_EXPORT,
        MainViewModel.SCREEN_SETTINGS
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.testTag("main_bottom_nav")
                ) {
                    NavigationBarItem(
                        selected = currentScreen == MainViewModel.SCREEN_HOME,
                        onClick = { viewModel.navigateTo(MainViewModel.SCREEN_HOME) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == MainViewModel.SCREEN_HOME) Icons.Filled.People else Icons.Outlined.People,
                                contentDescription = "Warga Kos"
                            )
                        },
                        label = { Text("Warga Kos") },
                        modifier = Modifier.testTag("nav_item_home")
                    )

                    NavigationBarItem(
                        selected = currentScreen == MainViewModel.SCREEN_EXPORT,
                        onClick = { viewModel.navigateTo(MainViewModel.SCREEN_EXPORT) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == MainViewModel.SCREEN_EXPORT) Icons.Filled.Description else Icons.Outlined.Description,
                                contentDescription = "Laporan & Dokumen"
                            )
                        },
                        label = { Text("Laporan & Dokumen") },
                        modifier = Modifier.testTag("nav_item_export")
                    )

                    NavigationBarItem(
                        selected = currentScreen == MainViewModel.SCREEN_SETTINGS,
                        onClick = { viewModel.navigateTo(MainViewModel.SCREEN_SETTINGS) },
                        icon = {
                            Icon(
                                imageVector = if (currentScreen == MainViewModel.SCREEN_SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                contentDescription = "Pengaturan"
                            )
                        },
                        label = { Text("Pengaturan & Sync") },
                        modifier = Modifier.testTag("nav_item_settings")
                    )
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                MainViewModel.SCREEN_HOME -> HomeScreen(viewModel = viewModel)
                MainViewModel.SCREEN_FORM -> FormScreen(viewModel = viewModel)
                MainViewModel.SCREEN_EXPORT -> ExportScreen(viewModel = viewModel)
                MainViewModel.SCREEN_PREVIEW -> DocumentPreviewScreen(viewModel = viewModel)
                MainViewModel.SCREEN_SETTINGS -> SettingsScreen(viewModel = viewModel)
                MainViewModel.SCREEN_LOGIN -> LoginScreen(viewModel = viewModel)
            }
        }
    }
}
