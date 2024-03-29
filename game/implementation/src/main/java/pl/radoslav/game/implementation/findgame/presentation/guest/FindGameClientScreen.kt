package pl.radoslav.game.implementation.findgame.presentation.guest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import pl.radoslav.game.implementation.domain.GameServer

@Composable
fun FindGameClientScreen(navController: NavController) {
    val viewModel: FindGameClientViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = state) {
        if (state.navigateToGame) {
            navController.navigate("game")
        }
    }

    FindGameContent(
        state = state,
        onSearchDevicesClick = {
            if (state.isSearching) {
                viewModel.stopDiscoveringDevices()
            } else {
                viewModel.discoverDevices()
            }
        },
        onClick = {
            viewModel.connectToDevice(it)
        },
    )
}

@Composable
fun FindGameContent(
    state: FindGameClientState,
    onSearchDevicesClick: () -> Unit,
    onClick: (GameServer) -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize(),
    ) {
        Text("Find your opponent")
        Text(if (state.isSearching) "Searching..." else "Not searching")
        LazyColumn(
            modifier =
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color.Gray),
        ) {
            items(state.devicesFound) { device ->
                BluetoothDeviceItem(
                    device,
                    isConnecting = false,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onClick,
                )
            }
        }
        Button(onClick = onSearchDevicesClick) {
            Text("Find devices")
        }
    }
}

@Composable
fun BluetoothDeviceItem(
    server: GameServer,
    isConnecting: Boolean,
    onClick: (GameServer) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .clickable {
                onClick(server)
            },
    ) {
        Column(Modifier.background(Color.White)) {
            Text(server.name, color = Color.Black)
            Text(server.address)
        }
        if (isConnecting) CircularProgressIndicator()
    }
}

@Preview(showSystemUi = true)
@Composable
fun FindGameScreenPreview() {
    FindGameContent(
        state =
            FindGameClientState(
                devicesFound =
                    listOf(
                        GameServer("Test device", "AA:AA:AA:AA:AA:AA"),
                        GameServer("Test device 2", "BB:BB:BB:BB:BB:BB"),
                        GameServer("Test device 3", "CC:CC:CC:CC:CC:CC"),
                    ),
                isSearching = true,
            ),
        onSearchDevicesClick = {},
        onClick = {},
    )
}

@Preview(showBackground = true)
@Composable
fun BluetoothDeviceItemPreview() {
    BluetoothDeviceItem(
        GameServer("Test device", "AA:AA:AA:AA:AA:AA"),
        isConnecting = true,
        onClick = {},
    )
}
