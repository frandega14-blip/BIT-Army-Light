package com.bit.armylight

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bit.armylight.ui.screens.ArmyLightScreen
import com.bit.armylight.ui.theme.BitArmyLightTheme
import com.bit.armylight.util.HapticController
import com.bit.armylight.viewmodel.LightViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LightViewModel by viewModels()
    private lateinit var hapticController: HapticController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Mantener la pantalla encendida durante todo el concierto
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // 2. Modo inmersivo total (Pantalla completa sin barras que distraigan)
        enableImmersiveFullScreen()

        // 3. Inicializar controlador háptico
        hapticController = HapticController(this)

        setContent {
            BitArmyLightTheme {
                ArmyLightScreen(
                    viewModel = viewModel,
                    hapticController = hapticController
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        enableImmersiveFullScreen()
    }

    private fun enableImmersiveFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { insetsController ->
                insetsController.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                insetsController.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        hapticController.cancel()
    }
}
