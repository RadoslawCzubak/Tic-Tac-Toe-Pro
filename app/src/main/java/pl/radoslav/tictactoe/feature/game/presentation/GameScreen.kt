package pl.radoslav.tictactoe.feature.game.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pl.radoslav.tictactoe.feature.game.domain.Player

@Composable
fun GameScreen() {
    val viewModel: GameViewModel = hiltViewModel()
    val state: GameScreenState by viewModel.state.collectAsStateWithLifecycle()

    GameScreenContent(
        state = state,
        onBoardCellClicked = { rowId, columnId ->
            viewModel.onBoardCellClicked(rowId, columnId)
        },
    )
}

@Composable
fun GameScreenContent(
    state: GameScreenState,
    onBoardCellClicked: (Int, Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CurrentPlayerIndicator(state.currentPlayer)
        TicTacToeBoard(
            state = state,
            onBoardCellClicked,
        )
    }
}

@Composable
fun CurrentPlayerIndicator(player: Player) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Current player: ")
        Box(
            modifier =
                Modifier
                    .size(32.dp)
                    .background(
                        when (player) {
                            Player.O -> Color.Blue
                            Player.X -> Color.Red
                        },
                    ),
        )
    }
}

@Composable
fun TicTacToeBoard(
    state: GameScreenState,
    onBoardCellClicked: (Int, Int) -> Unit,
) {
    Column {
        state.board.cells.forEachIndexed { rowId, row ->
            Row {
                row.forEachIndexed { columnId, cell ->
                    val backgroundColor =
                        when (cell) {
                            Player.O -> Color.Blue
                            Player.X -> Color.Red
                            null -> Color.Gray
                        }
                    Box(
                        modifier =
                            Modifier
                                .size(100.dp)
                                .padding(8.dp)
                                .background(backgroundColor)
                                .clickable {
                                    onBoardCellClicked(rowId, columnId)
                                },
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun GamePreview() {
    GameScreenContent(
        state = GameScreenState(),
        onBoardCellClicked = { _, _ -> },
    )
}
