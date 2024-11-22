package com.example.supabasedemo

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import android.Manifest
import android.app.Activity
import com.example.supabasedemo.data.network.SupabaseClient.client


import androidx.camera.view.PreviewView
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabasedemo.data.model.UserState
import com.example.supabasedemo.data.network.SupabaseClient
import com.example.supabasedemo.ui.theme.SupabaseDemoTheme
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import java.util.UUID
import java.util.concurrent.Executors
import kotlin.math.log


class MainActivity : ComponentActivity() {
    private val CAMERA_PERMISSION_REQUEST_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SupabaseDemoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        }
    }

    @Composable
    fun LoggedInScreen(onCreateGameClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Welcome to the Game!")
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onCreateGameClick) {
                Text("Create a Game")
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
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
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

    @Composable
    fun QRCodeScanner(
        onScanSuccess: (String) -> Unit,
        onScanError: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current
        val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
        val scanner = BarcodeScanning.getClient()

        val permissionGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (!permissionGranted) {
            LaunchedEffect(context) {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST_CODE
                )
            }
        } else {
            AndroidView(
                modifier = modifier.fillMaxSize(),
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraExecutor = Executors.newSingleThreadExecutor()

                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = androidx.camera.core.Preview.Builder()
                            .build()
                            .apply {
                                surfaceProvider = previewView.surfaceProvider
                            }

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()

                        imageAnalysis.setAnalyzer(cameraExecutor, { imageProxy ->
                            processImageProxy(scanner, imageProxy, onScanSuccess, onScanError)
                        })

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            Log.e("QRCodeScanner", "Camera binding failed: ${e.message}")
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                }
            )
        }
    }

    @OptIn(ExperimentalGetImage::class)
    public fun processImageProxy(
        scanner: BarcodeScanner,
        imageProxy: ImageProxy,
        onScanSuccess: (String) -> Unit,
        onScanError: () -> Unit
    ) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            scanner.process(inputImage as InputImage)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes) {
                        barcode.rawValue?.let { qrCode ->
                            onScanSuccess(qrCode)
                        }
                    }
                }
                .addOnFailureListener {
                    Log.e("QRCodeScanner", "QR code scanning failed: ${it.message}")
                    onScanError()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun getCurrentUser(): JsonObject? {
        val user = client.auth.currentUserOrNull()
        val metadata = user?.userMetadata
        return metadata
    }

    @Serializable
    data class Game(
        val id: Long? = null,
        val uuid: String,
        val start_time: String? = null,
        val end_time: String? = null,
        val round_no: Short = 0,
        val user1: String?,
        val user2: String? = null,
        val won: Boolean? = null
    )

    private fun createGameInSupabase(gameUuid: String, onGameCreated: () -> Unit, onError: (String) -> Unit){
        val loggedInUser = getCurrentUser()

        val gameData = Game(
            uuid = gameUuid,
            user1 = loggedInUser?.get("sub").toString().trim().replace("\"", ""),
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val createdGame = client.from("games")
                    .insert(gameData){
                        select()
                    }.decodeSingle<Game>()

                    Log.d("Supabase", "Game created: $createdGame")

                    withContext(Dispatchers.Main) {
                        onGameCreated()
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase", "catch (e: Exception: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }
    }

    private fun joinGameInSupabase(gameUuid: String, onGameJoined: (Game) -> Unit, onError: (String) -> Unit) {
        Log.d("Supabase-Join-Game", "Joining game: $gameUuid")
        val loggedInUser = getCurrentUser()
        val user2Uuid = loggedInUser?.get("sub").toString().trim().replace("\"", "")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val updatedGame = client.from("games")
                    .update({
                        Game::user2 setTo user2Uuid
                    }) {
                        select()
                        filter{
                            Game::uuid eq gameUuid
                        }
                    }.decodeSingle<Game>()

                Log.d("Supabase-Join-Game", "Game joined: $updatedGame")

                withContext(Dispatchers.Main) {
                    onGameJoined(updatedGame)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.d("Supabase-Join-Game", "Error joining game: $e")
                    onError(e.message ?: "Unexpected error occurred.")
                }
            }
        }
    }

    @Composable
    fun CreateGameScreen(onGameCreated: (String) -> Unit) {
        var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }
        var showScanner by remember { mutableStateOf(false) }
        var scannedQRCode by remember { mutableStateOf<String?>(null) }
        var gameDetails by remember { mutableStateOf<Game?>(null) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Button(onClick = {
                val gameUuid = UUID.randomUUID().toString()
                coroutineScope.launch{
                    createGameInSupabase(gameUuid, onGameCreated = {
                        qrCodeBitmap = generateQRCode(gameUuid) ?: run {
                            errorMessage = "Error generating QR code"
                            return@createGameInSupabase
                        }
                    }, onError = {
                        errorMessage = it
                    })
                }
            }) {
                Text("Generate QR Code aka Create Game")
            }
            Spacer(modifier = Modifier.padding(16.dp))

            qrCodeBitmap?.let { bitmap ->
                Image(
                    painter = BitmapPainter(bitmap.asImageBitmap()),
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )
            } ?: Text("Click the button to generate a QR code.")

            Spacer(modifier = Modifier.padding(16.dp))

            Button(onClick = {
                showScanner = true
            }) {
                Text("Join Game")
            }

            if (showScanner) {
                QRCodeScanner(onScanSuccess = { qrCode ->
                    showScanner = false
                    scannedQRCode = qrCode
                    scannedQRCode?.let { gameUuid ->
                        coroutineScope.launch {
                            joinGameInSupabase(gameUuid, onGameJoined = { game ->
                                gameDetails = game
                                Log.d("QRCodeScanner", "Joined game: $gameUuid")
                            }, onError = { error ->
                                errorMessage = error
                            })
                        }
                    }
                }, onScanError = {
                    showScanner = false
                    Log.e("QRCode", "Error scanning QR code")
                }, modifier = Modifier.fillMaxSize()
                )
            }

            scannedQRCode?.let {
                Spacer(modifier = Modifier.padding(16.dp))
                Text("Scanned QR Code: $it")
            }

            gameDetails?.let { game ->
                Spacer(modifier = Modifier.padding(16.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Game Details")
                    Text("Game UUID: ${game.uuid}")
                    Text("User 1: ${game.user1}")
                    Text("User 2: ${game.user2 ?: "Waiting for player"}")
                    Text("Round: ${game.round_no}")
                    Text("Start Time: ${game.start_time ?: "Not started yet"}")
                    Text("End Time: ${game.end_time ?: "Not ended yet"}")
                    Text("Winner: ${game.won?.let { if (it) "User 1" else "User 2" } ?: "TBD"}")
                }
            }

            errorMessage?.let {
                Spacer(modifier = Modifier.padding(16.dp))
                Text("Error: $it", color = Color.Red)
            }
        }
    }

    @Composable
    fun MainScreen(viewModel: SupabaseAuthViewModel = viewModel()) {
        val context = LocalContext.current
        val userState by viewModel.userState

        var userEmail by remember { mutableStateOf("") }
        var userPassword by remember { mutableStateOf("") }
        var username by remember { mutableStateOf("") }
        var macAddress by remember { mutableStateOf("") }

        var currentUserState by remember { mutableStateOf("") }

        var navigateToCreateGame by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            viewModel.isUserLoggedIn(
                context,
            )
        }

        if (navigateToCreateGame) {
            CreateGameScreen(onGameCreated = {
                navigateToCreateGame = false
            })
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TextField(
                    value = userEmail,
                    placeholder = {
                        Text(text = "Enter email")
                    },
                    onValueChange = {
                        userEmail = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = username,
                    placeholder = {
                        Text(text = "Enter username")
                    },
                    onValueChange = {
                        username = it
                    })
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = macAddress,
                    placeholder = {
                        Text(text = "mac address (will remove tomorrow)")
                    },
                    onValueChange = {
                        macAddress = it
                    }
                )
                Spacer(modifier = Modifier.padding(8.dp))
                TextField(
                    value = userPassword,
                    placeholder = {
                        Text(text = "Enter password")
                    },
                    onValueChange = {
                        userPassword = it
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                )
                Spacer(modifier = Modifier.padding(8.dp))
                Button(onClick = {
                    viewModel.signUp(
                        context,
                        userEmail,
                        userPassword,
                        username,
                        macAddress
                    )
                }) {
                    Text(text = "Sign Up")
                }

                Button(onClick = {
                    viewModel.login(
                        context,
                        userEmail,
                        userPassword,
                    )
                }) {
                    Text(text = "Login")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    onClick = {
                        viewModel.logout(context)
                    }) {
                    Text(text = "Logout")
                }

                when (userState) {
                    is UserState.Loading -> {
                        LoadingComponent()
                    }

                    is UserState.Success -> {
                        val message = (userState as UserState.Success).message
                        currentUserState = message
                    }

                    is UserState.LoggedIn -> {
                        LoggedInScreen(onCreateGameClick = {
                            navigateToCreateGame = true
                        })
                    }

                    is UserState.Error -> {
                        val message = (userState as UserState.Error).message
                        currentUserState = message
                    }
                }

                if (currentUserState.isNotEmpty()) {
                    Text(text = currentUserState)
                }
            }


        }
    }
}