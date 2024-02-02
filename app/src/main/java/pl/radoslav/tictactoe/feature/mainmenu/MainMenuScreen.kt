package pl.radoslav.tictactoe.feature.mainmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun MainMenuScreen(navController: NavController) {
    val viewModel: MainMenuViewModel = hiltViewModel()
    MainMenuContent(
        onConnectWithPlayer = { navController.navigate("findgame") },
        onHostServer = { navController.navigate("host") },
    )
}

@Composable
fun MainMenuContent(
    onConnectWithPlayer: () -> Unit,
    onHostServer: () -> Unit,
) {
    Column {
        Text(text = "Tic Tac Toe")
        Text(text = "Pro Edition")

        Button(onClick = onConnectWithPlayer) {
            Text(text = "Connect with player")
        }

        Button(onClick = onHostServer) {
            Text(text = "Host game")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainMenuContentPreview() {
    MainMenuContent(
        onConnectWithPlayer = {},
        onHostServer = {},
    )
}
