package com.cpen321.usermanagement.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cpen321.usermanagement.ui.theme.LocalSpacing

@Composable
fun TicTacToeGame(
    modifier: Modifier = Modifier
) {
    var board by remember { mutableStateOf(Array(3) { Array(3) { "" } }) }
    var currentPlayer by remember { mutableStateOf("X") }
    var gameOver by remember { mutableStateOf(false) }
    var winner by remember { mutableStateOf("") }
    var isDraw by remember { mutableStateOf(false) }
    var isPlayerTurn by remember { mutableStateOf(true) }
    
    val spacing = LocalSpacing.current

    fun checkWinner(): String? {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != "" && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }
        
        // Check columns
        for (j in 0..2) {
            if (board[0][j] != "" && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return board[0][j]
            }
        }
        
        // Check diagonals
        if (board[0][0] != "" && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != "" && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }
        
        return null
    }
    
    fun checkDraw(): Boolean {
        return board.all { row -> row.all { cell -> cell != "" } }
    }
    
    fun getAvailableMoves(): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        for (i in 0..2) {
            for (j in 0..2) {
                if (board[i][j] == "") {
                    moves.add(Pair(i, j))
                }
            }
        }
        return moves
    }
    fun checkWinnerOnBoard(board: Array<Array<String>>): String? {
        // Check rows
        for (i in 0..2) {
            if (board[i][0] != "" && board[i][0] == board[i][1] && board[i][1] == board[i][2]) {
                return board[i][0]
            }
        }

        // Check columns
        for (j in 0..2) {
            if (board[0][j] != "" && board[0][j] == board[1][j] && board[1][j] == board[2][j]) {
                return board[0][j]
            }
        }

        // Check diagonals
        if (board[0][0] != "" && board[0][0] == board[1][1] && board[1][1] == board[2][2]) {
            return board[0][0]
        }
        if (board[0][2] != "" && board[0][2] == board[1][1] && board[1][1] == board[2][0]) {
            return board[0][2]
        }

        return null
    }

    fun makeBotMove() {
        val availableMoves = getAvailableMoves()
        if (availableMoves.isNotEmpty()) {
            // Simple AI: try to win, then block player, then random move
            var bestMove = availableMoves[0]
            
            // Try to win
            for (move in availableMoves) {
                val tempBoard = board.map { it.clone() }.toTypedArray()
                tempBoard[move.first][move.second] = "O"
                if (checkWinnerOnBoard(tempBoard) == "O") {
                    bestMove = move
                    break
                }
            }
            
            // Try to block player
            if (bestMove == availableMoves[0]) {
                for (move in availableMoves) {
                    val tempBoard = board.map { it.clone() }.toTypedArray()
                    tempBoard[move.first][move.second] = "X"
                    if (checkWinnerOnBoard(tempBoard) == "X") {
                        bestMove = move
                        break
                    }
                }
            }
            
            // Make the move
            board = board.mapIndexed { r, rowArray ->
                if (r == bestMove.first) {
                    rowArray.mapIndexed { c, cell ->
                        if (c == bestMove.second) "O" else cell
                    }.toTypedArray()
                } else {
                    rowArray
                }
            }.toTypedArray()
        }
    }
    

    
    fun makeMove(row: Int, col: Int) {
        if (board[row][col] == "" && !gameOver && isPlayerTurn) {
            board = board.mapIndexed { r, rowArray ->
                if (r == row) {
                    rowArray.mapIndexed { c, cell ->
                        if (c == col) "X" else cell
                    }.toTypedArray()
                } else {
                    rowArray
                }
            }.toTypedArray()
            
            val gameWinner = checkWinner()
            if (gameWinner != null) {
                winner = gameWinner
                gameOver = true
            } else if (checkDraw()) {
                isDraw = true
                gameOver = true
            } else {
                isPlayerTurn = false
                // Bot's turn
                makeBotMove()
                
                val gameWinnerAfterBot = checkWinner()
                if (gameWinnerAfterBot != null) {
                    winner = gameWinnerAfterBot
                    gameOver = true
                } else if (checkDraw()) {
                    isDraw = true
                    gameOver = true
                } else {
                    isPlayerTurn = true
                }
            }
        }
    }
    
    fun resetGame() {
        board = Array(3) { Array(3) { "" } }
        currentPlayer = "X"
        gameOver = false
        winner = ""
        isDraw = false
        isPlayerTurn = true
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tic Tac Toe vs Bot",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = spacing.large)
        )
        
        if (!gameOver) {
            val turnText = if (isPlayerTurn) "Your turn (X)" else "Bot's turn (O)"
            Text(
                text = turnText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = spacing.medium)
            )
        } else {
            val resultText = when {
                isDraw -> "It's a draw!"
                winner == "X" -> "You win!"
                winner == "O" -> "Bot wins!"
                else -> "Game over!"
            }
            Text(
                text = resultText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = if (isDraw) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = spacing.medium)
            )
        }
        
        // Game board
        Column(
            modifier = Modifier
                .size(300.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(4.dp)
        ) {
            for (row in 0..2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    for (col in 0..2) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (board[row][col] == "") 
                                        MaterialTheme.colorScheme.surfaceVariant 
                                    else 
                                        MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .clickable(enabled = isPlayerTurn && board[row][col] == "" && !gameOver) { 
                                    makeMove(row, col) 
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = board[row][col],
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (board[row][col] == "X") 
                                    MaterialTheme.colorScheme.primary 
                                else 
                                    MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
        
        Button(
            onClick = { resetGame() },
            modifier = Modifier.padding(top = spacing.large)
        ) {
            Text("New Game")
        }
    }
}
