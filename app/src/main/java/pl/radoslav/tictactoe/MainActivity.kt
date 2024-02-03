package pl.radoslav.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.radoslav.bluetooth.BluetoothUI
import pl.radoslav.core_ui.theme.TicTacToeTheme
import pl.radoslav.game.implementation.findgame.presentation.guest.FindGameClientScreen
import pl.radoslav.game.implementation.findgame.presentation.host.FindGameHostScreen
import pl.radoslav.home.implementation.presentation.MainMenuScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToeTheme {
                val navController = rememberNavController()
                NavHost(navController, startDestination = "mainmenu") {
                    composable("mainmenu") {
                        MainMenuScreen(navController)
                    }
                    composable("findgame") {
                        FindGameClientScreen(navController)
                    }
                    composable("host") {
                        FindGameHostScreen(
                            navController = navController,
                            onMakeDiscoverable = {
                                BluetoothUI.startAdvertising(this@MainActivity)
                            },
                        )
                    }
                }
            }
        }
    }
}
