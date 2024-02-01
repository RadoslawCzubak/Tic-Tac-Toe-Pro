package pl.radoslav.tictactoe.feature.findgame.presentation.host

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun FindGameHostScreen() {
    val viewModel: FindGameHostViewModel = hiltViewModel()
}

@Composable
fun FindGameHostContent() {
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Waiting for second player")
    }
}

@Preview(showSystemUi = true)
@Composable
fun FindGameHostScreenPreview() {
    FindGameHostScreen()
}