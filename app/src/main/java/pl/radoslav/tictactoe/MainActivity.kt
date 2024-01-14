package pl.radoslav.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.radoslav.tictactoe.feature.findgame.presentation.guest.FindGameScreen
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
                        FindGameScreen()
                    }
                }
            }
        }
    }
}