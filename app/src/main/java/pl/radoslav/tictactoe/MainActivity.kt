package pl.radoslav.tictactoe

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.radoslav.game.implementation.findgame.presentation.guest.FindGameScreen
import pl.radoslav.game.implementation.findgame.presentation.host.FindGameHostScreen
import pl.radoslav.game.implementation.game.presentation.GameScreen
import pl.radoslav.home.implementation.presentation.MainMenuScreen
import pl.radoslav.core_ui.theme.TicTacToeTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val discoverabilityResultContract = registerForActivityResult(EnableBluetoothDiscoverability()) {}

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
                                discoverabilityResultContract.launch(120)
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

class EnableBluetoothDiscoverability : ActivityResultContract<Int, Unit>() {
    override fun createIntent(context: Context, input: Int): Intent {
        return Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
            putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, input)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?) = Unit
}
