package pl.radoslav.tictactoe.feature.mainmenu

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun MainMenuScreen() {
    val viewModel: MainMenuViewModel = hiltViewModel()

    MainMenuContent(
        onConnectWithPlayer = {},
    )
}

@Composable
fun MainMenuContent(onConnectWithPlayer: () -> Unit) {
    Column {
        Text(text = "Tic Tac Toe")
        Text(text = "Pro Edition")

        Button(onClick = onConnectWithPlayer) {
            Text(text = "Connect with player")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun MainMenuContentPreview() {
    MainMenuContent(
        onConnectWithPlayer = {},
    )
}
