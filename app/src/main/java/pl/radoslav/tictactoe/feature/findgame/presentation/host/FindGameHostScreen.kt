package pl.radoslav.tictactoe.feature.findgame.presentation.host

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

@Composable
fun FindGameHostScreen(navController: NavController, onMakeDiscoverable: () -> Unit) {
    val viewModel: FindGameHostViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(key1 = state) {
        if (state.navigateToGame) {
            navController.navigate("game")
        }
    }

    LaunchedEffect(key1 = Unit) {
        onMakeDiscoverable()
    }

    FindGameHostContent()
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
    FindGameHostScreen(rememberNavController(), {})
}