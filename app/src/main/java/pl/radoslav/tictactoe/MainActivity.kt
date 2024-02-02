package pl.radoslav.tictactoe

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.radoslav.tictactoe.feature.findgame.presentation.guest.FindGameScreen
import pl.radoslav.tictactoe.feature.findgame.presentation.host.FindGameHostScreen
import pl.radoslav.tictactoe.feature.game.presentation.GameScreen
import pl.radoslav.tictactoe.feature.mainmenu.MainMenuScreen
import pl.radoslav.tictactoe.ui.theme.TicTacToeTheme

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
                        FindGameScreen(navController)
                    }
                    composable("host") {
                        FindGameHostScreen(
                            navController = navController,
                            onMakeDiscoverable = {
                                val requestCode = 1
                                val discoverableIntent: Intent =
                                    Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                                    }
                                startActivityForResult(discoverableIntent, requestCode)
                            },
                        )
                    }
                    composable("game") {
                        GameScreen()
                    }
                }
            }
        }
    }
}
