package com.example.supabasedemo.compose.screens

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.supabasedemo.compose.viewModels.MainViewModel
import com.example.supabasedemo.compose.views.QRCodeScanner
import com.example.supabasedemo.data.model.Game
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.ui.theme.MyOutlinedButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.delay
import java.util.UUID

@Composable
fun CreateGameScreen(
    getState: () -> MutableState<UserState>,
    setState: (state: UserState) -> Unit
) {
    val viewModel = MainViewModel(LocalContext.current, setState = { setState(it) })

    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var scannedQRCode by remember { mutableStateOf<String?>(null) }
    var gameDetails by remember { mutableStateOf<Game?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var gameSubscription by remember { mutableStateOf<Game?>(null) }

    var round by remember { mutableStateOf(1) }
    var user1Score by remember { mutableStateOf(0) }
    var user2Score by remember { mutableStateOf(0) }
    var currentPlayer by remember { mutableStateOf(2) }

    LaunchedEffect(Unit) {
        setState(UserState.InGameCreation)
        viewModel.supabaseAuth.isUserLoggedIn()
    }

    if (gameDetails != null) {
        LaunchedEffect(gameDetails!!.uuid) {
            viewModel.supabaseRealtime.subscribeToGame(
                uuid = gameDetails!!.uuid,
                onGameUpdate = { updatedGame ->
                    gameSubscription = updatedGame
                }
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (getState().value != UserState.InMiniGame) {
            gameSubscription?.let { game ->
                when (game.round_no) {
                    0 -> Text("Waiting for players to join...")
                    1 -> Text("Round 1: Game Started!")
                    2 -> Text("Round 2: Continue the game!")
                    3 -> Text("Round 3: Final round!")
                }
            } ?: Text("No game subscription yet.")


            Spacer(modifier = Modifier.padding(16.dp))
            MyOutlinedButton(
                onClick = {
                    val gameUuid = UUID.randomUUID().toString()
                    gameDetails = viewModel.supabaseDb.createGameInSupabase(
                        gameUuid,
                        onGameCreated = {
                            qrCodeBitmap = generateQRCode(gameUuid) ?: run {
                                errorMessage = "Error generating QR code"
                                return@createGameInSupabase
                            }
                            setState(UserState.GameCreated)
                        },
                        onError = { errorMessage = it },
                        currentUser = viewModel.supabaseAuth.getCurrentUser()
                    )
                }
            ) {
                Text("Generate QR Code aka Create Game")
            }
            Spacer(modifier = Modifier.padding(16.dp))
            MyOutlinedButton(
                onClick = {
                    setState(UserState.CameraOpened)
                }
            ) {
                Text("Join Game")
            }
            Spacer(modifier = Modifier.padding(16.dp))
        }
        val userState = getState().value
        when (userState) {
            is UserState.GameCreated -> {
                Image(
                    painter = BitmapPainter(qrCodeBitmap!!.asImageBitmap()),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
                if (gameDetails != null && gameDetails!!.user1 != null && gameDetails!!.user2 != null) {
                    setState(UserState.InMiniGame)
                }
            }

            is UserState.CameraOpened -> {
                QRCodeScanner(
                    onScanSuccess = {
                        scannedQRCode = it
                        scannedQRCode?.let { gameUuid ->
                            viewModel.supabaseDb.joinGameInSupabase(
                                gameUuid,
                                onGameJoined = { game ->
                                    gameDetails = game
                                    Log.d("QRCodeScanner", "Joined game: $gameUuid")
                                },
                                onError = { errorMessage = it },
                                currentUser = viewModel.supabaseAuth.getCurrentUser()
                            )
                        }
                        setState(UserState.QrScanned)
                    },
                    onScanError = {
                        setState(UserState.QrScanFailed)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            is UserState.QrScanned -> {
                var isGameReady by remember { mutableStateOf(false) }

                LaunchedEffect(gameDetails) {
                    if (gameDetails?.user1 != null && gameDetails?.user2 != null) {
                        delay(1000) // Give time for UI to update
                        isGameReady = true
                        setState(UserState.InMiniGame)
                    }
                }
                Text("Scanned QR Code: $scannedQRCode")
                Spacer(modifier = Modifier.padding(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Game Details")
                    Text("Game UUID: ${gameDetails!!.uuid}")
                    Text("User 1: ${gameDetails!!.user1}")
                    Text("User 2: ${gameDetails!!.user2 ?: "Waiting for player"}")
                    Text("Round: ${gameDetails!!.round_no}")
                    Text("Start Time: ${gameDetails!!.startTime ?: "Not started yet"}")
                    Text("End Time: ${gameDetails!!.endTime ?: "Not ended yet"}")
                    Text("Winner: ${gameDetails!!.won?.let { if (it) "User 1" else "User 2" } ?: "TBD"}")
                }
            }

            is UserState.QrScanFailed -> {
                Text("Error: $errorMessage", color = Color.Red)
            }
            is UserState.InMiniGame -> {
                MinigameScreen(
                    onNavigateToMainMenu = {

                    },
                    getState = getState,
                    setState = setState,
                    round = round,
                    onScoreCalculated = { score ->
                        if (currentPlayer == 2) {
                            user2Score += score
                            currentPlayer = 1
                            setState(UserState.ShowScore)
                        } else {
                            user1Score += score
                            currentPlayer = 2
                            round++
                            if (round <= 3) {
                                setState(UserState.ShowScore)
                            } else {
                                setState(UserState.ShowFinalScores)
                            }
                        }
                    }
                )
            }
            is UserState.ShowScore -> {
                Text("User ${if (currentPlayer == 2) "1" else "2"} scored: ${if (currentPlayer == 2) user1Score else user2Score}")
                LaunchedEffect(Unit) {
                    delay(3000)
                    if (round <= 3) {
                        setState(UserState.InMiniGame)
                    } else {
                        setState(UserState.ShowFinalScores)
                    }
                }
            }
            is UserState.ShowFinalScores -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Final Scores:")
                    Text("User 1: $user1Score")
                    Text("User 2: $user2Score")
                }
            } else -> {

            }
        }
    }
    LaunchedEffect(gameDetails?.uuid) {
        gameDetails?.let { game ->
            viewModel.supabaseRealtime.subscribeToGame(
                uuid = game.uuid,
                onGameUpdate = { updatedGame ->
                    gameSubscription = updatedGame
                    if (updatedGame.user1 != null && updatedGame.user2 != null) {
                        gameDetails = updatedGame
                    }
                }
            )
        }
    }
}


private fun generateQRCode(text: String): Bitmap? {
    val writer = QRCodeWriter()
    return try {
        Log.d("QRCode", "Attempting to encode QR code for text: $text")

        val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        Log.d("QRCode", "QR code BitMatrix created with dimensions: $width x $height")

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(
                    x,
                    y,
                    if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        Log.d("QRCode", "QR code Bitmap successfully generated")

        bmp
    } catch (e: WriterException) {
        Log.e("QRCode", "Failed to generate QR code: ${e.message}")
        e.printStackTrace()
        null
    } catch (e: Exception) {
        Log.e("QRCode", "Unexpected error: ${e.message}")
        e.printStackTrace()
        null
    }
}